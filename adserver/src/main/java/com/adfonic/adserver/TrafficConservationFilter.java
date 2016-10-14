package com.adfonic.adserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.byyd.archive.transform.util.MVELUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.SnappyFramedOutputStream;

import ch.qos.logback.access.AccessConstants;
import ch.qos.logback.access.servlet.TeeFilter;

import com.adfonic.adserver.Traffic.Capture;
import com.adfonic.adserver.Traffic.Request;
import com.adfonic.adserver.Traffic.Response;
import com.adfonic.adserver.Traffic.TrafficCapture;
import com.adfonic.adserver.controller.rtb.TrafficConservationSwitchController;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.protobuf.ByteString;

/**
 * Filter that takes care of flushing the backup logger at the end of
 * each request, along with timing each request.
 */
public class TrafficConservationFilter extends TeeFilter {
    
	private static final Logger LOG = LoggerFactory
			.getLogger(TrafficConservationFilter.class);

	public static volatile boolean FILTER_HIT = false;
	private ThreadLocal<TrafficCapture.Builder> capture = new ThreadLocal<TrafficCapture.Builder>();

	private int minCaptures = 2000;
	private int maxCaptures = 10000;

	private int logCount = 1;

	private String bucketName = "byyd.vir.adserverqa.traffic";
	private String accessKey = "AKIAJRQDOV4DMY47DKPQ";
	private String secretKey = "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";
	private String namePattern = "traffic/@{date('yyyy')}/@{date('MM')}/@{date('dd')}/@{env('HOSTNAME')}/"
			+ "traffic-v1-@{env('HOSTNAME')}-@{date('yyyy-MM-dd-HH-mm')}-@{arg0}-@{arg1}.proto.snappy";

	private AtomicReference<TrafficCapture.Builder> current = new AtomicReference<TrafficCapture.Builder>();
	private ThreadLocal<PushThread> localPush = new ThreadLocal<PushThread>();
	private ThreadLocal<AmazonS3Client> localClient = new ThreadLocal<AmazonS3Client>() {
		protected AmazonS3Client initialValue() {
			return new AmazonS3Client(new BasicAWSCredentials(accessKey,
					secretKey));
		};
	};
	private ThreadLocal<AtomicLong> localCount = new ThreadLocal<AtomicLong>() {
		protected AtomicLong initialValue() {
			return new AtomicLong();
		};
	};

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.info("Initializing traffic filter.");
		super.init(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		try {
			if (TrafficConservationSwitchController.ENABLED) {
				super.doFilter(request, response, filterChain);
				FILTER_HIT = true;
			} else {
				filterChain.doFilter(request, response);
			}
		} finally {
			if (TrafficConservationSwitchController.ENABLED) {
				TrafficCapture.Builder ct = capture.get();
				if (ct == null) {
					ct = TrafficCapture.newBuilder().setTimestampFrom(
							System.currentTimeMillis());
					ct.setDroppedRequests(0);
					capture.set(ct);
				}

				if (localPush.get() == null) {
					PushThread push = new PushThread();
					localPush.set(push);
					push.start();
				}

				byte[] reqBody = (byte[]) request
						.getAttribute(AccessConstants.LB_INPUT_BUFFER);
				byte[] respBody = (byte[]) request
						.getAttribute(AccessConstants.LB_OUTPUT_BUFFER);

				if (ct.getCaptureCount() < maxCaptures) {
					Capture.Builder cp = Capture.newBuilder();
					cp.setTimestamp(System.currentTimeMillis());
					Traffic.Request.Builder req = Request
							.newBuilder();
					HttpServletRequest htreq = (HttpServletRequest) request;
					req.setMethod(htreq.getMethod());
					if (htreq.getRequestURI() != null) {
						req.setUri(htreq.getRequestURI());
					}
					if (htreq.getQueryString() != null) {
						req.setQueryString(htreq.getQueryString());
					}
					if (htreq.getRemoteHost() != null) {
						req.setRemoteHost(htreq.getRemoteHost());
					}
					if (htreq.getRemoteAddr() != null) {
						req.setRemoteAddr(htreq.getRemoteAddr());
					}
					req.setRemotePort(htreq.getRemotePort());
					if (reqBody != null) {
						req.setBody(ByteString.copyFrom(reqBody));
						Enumeration<String> enN = htreq.getHeaderNames();
						while(enN.hasMoreElements()) {
							String hn = enN.nextElement();
							String v = htreq.getHeader(hn);
							req.addHeadersBuilder().setName(hn).setValue(v);
						}
					}
					Traffic.Response.Builder resp = Response
							.newBuilder();
					HttpServletResponse htResp = (HttpServletResponse) response;
					if (respBody != null) {
						resp.setBody(ByteString.copyFrom(respBody));
						for(String hn : htResp.getHeaderNames()) {
							String v = htResp.getHeader(hn);
							resp.addHeadersBuilder().setName(hn).setValue(v);
						}
					}
					resp.setCode(htResp.getStatus());
					cp.setRequest(req);
					cp.setRepsonse(resp);
					ct.addCapture(cp);

					if (ct.getCaptureCount() > minCaptures
							&& ct.getCaptureCount() > minCaptures
									+ (maxCaptures - minCaptures)
									/ 2
									/ (11 - (Thread.currentThread().getId() % 10))) {
						ct.setTimestampTo(System.currentTimeMillis());
						if (current.compareAndSet(null, ct)) {
							ct = TrafficCapture.newBuilder().setTimestampFrom(
									System.currentTimeMillis());
							ct.setDroppedRequests(0);
							capture.set(ct);
						}
					}
				} else {
					ct.setDroppedRequests(ct.getDroppedRequests() + 1);
				}
			}
		}
	}

	public class PushThread extends Thread {
		@Override
		public void run() {
			long timeSpent = 0;
			long runs = 0;
			while (true) {
				try {
					TrafficCapture.Builder builder = current.get();
					if (builder != null && current.compareAndSet(builder, null)) {
						long begin = System.currentTimeMillis();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						String name = MVELUtil.resolve(namePattern,
								String.format("%03d", Thread.currentThread()
										.getId()), String.format("%05d",
										localCount.get().incrementAndGet()));

						OutputStream snout = new SnappyFramedOutputStream(baos);
						builder.build().writeTo(snout);
						snout.close();

						ObjectMetadata om = new ObjectMetadata();
						byte[] arr = baos.toByteArray();
						om.setContentLength(arr.length);
						localClient.get().putObject(bucketName, name,
								new ByteArrayInputStream(arr), om);
						runs++;
						timeSpent += System.currentTimeMillis() - begin;

						if (runs >= logCount) {
							runs = 0;
							LOG.info("Performed " + runs
									+ " capture pushes in " + timeSpent);
						}
					}
				} catch (Exception t) {
					LOG.warn("Unable to push data", t);
				}

				try {
					Thread.sleep(1);
				} catch (Exception t) {
					LOG.warn("Push unable to sleep: " + t.getMessage());
				}
			}
		}
	}
}
