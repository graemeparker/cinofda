package net.byyd.archive.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.ByteArrayInput;

public class HTTPPostOutputStream extends OutputStream {

	private static final Logger LOG = LoggerFactory
			.getLogger(HTTPPostOutputStream.class);

	private String url;
	private String credentials;
	private String contentType;
	private Executor exec;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
	private boolean gzip;

	private long posts;
	private long postData;

	public HTTPPostOutputStream(String url, String credentials,
			String contentType, boolean gzip) {
		this.url = url;
		this.credentials = credentials;
		this.contentType = contentType;
		this.gzip = gzip;

		exec = Executor.newInstance();
	}

	@Override
	public void write(int b) throws IOException {
		baos.write(b);
	}

	public void post() throws IOException {
		Response resp = null;
		byte[] body = null;
		if (baos.size() != 0) {
			body = prepareBody();

			String authCode = "Basic "
					+ new String(Base64.encodeBase64(this.credentials
							.getBytes()));
			Request req = Request.Post(url).bodyByteArray(body)
					.addHeader("Content-Type", contentType)
					.addHeader("Authorization", authCode).userAgent("byyd");

			if (gzip) {
				req.addHeader("Content-Encoding", "gzip");
			}

			resp = exec.execute(req);
			posts += 1;
			postData += body.length;
		}
		baos.reset();

		if (resp != null) {
			StatusLine statusLine = resp.returnResponse().getStatusLine();
			if (statusLine.getStatusCode() > 204) {
				GZIPInputStream is = new GZIPInputStream(
						new ByteArrayInputStream(body));
				String content = IOUtils.toString(is);
				LOG.warn("Unable to post: " + content);
				throw new IOException("Error on post: " + statusLine);
			}
		}
	}

	private byte[] prepareBody() throws IOException {
		byte[] body = baos.toByteArray();

		if (gzip) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			GZIPOutputStream gso = new GZIPOutputStream(os);
			gso.write(body);
			gso.finish();
			gso.flush();
			gso.close();
			os.flush(); 
			os.close();
			body = os.toByteArray();
		}

		return body;
	}

	public long getPosts() {
		return posts;
	}

	public long getPostData() {
		return postData;
	}
}
