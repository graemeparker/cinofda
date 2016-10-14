package net.byyd.archive.model.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import net.byyd.archive.transform.HTTPPostOutputStream;
import net.byyd.archive.transform.MVELTransformer;
import net.byyd.archive.transform.OneItemLineFileSink;
import net.byyd.archive.transform.OneItemLineFileSink.ItemsInfo;
import net.byyd.archive.transform.OneItemLineFileSink.UpdateStream;
import net.byyd.archive.transform.PublishEventFeed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContinuousPushMMXBackupLog {
	private static final Logger LOG = LoggerFactory
			.getLogger(ContinuousPushMMXBackupLog.class);

	public static final ThreadLocal<HTTPPostOutputStream> postAd = new ThreadLocal<>();
	public static final ThreadLocal<HTTPPostOutputStream> postImp = new ThreadLocal<>();

	private static LoggingPush logPush = new LoggingPush() {
		public Logger getLog() {return LOG;}
		public String getMessage() {
			return "ad served/failed/win pushes: " + postAd.get().getPosts() + 
					" ; impression pushes: " + postImp.get().getPosts(); 
		}
	};
	
	public static void main(String[] args) {

		if (args.length != 4) {
			System.out
					.println("Usage: ContinuousPushMMXBackupLog <backup-log-file> "
							+ "<user:password> <destination-ad-url> <destination-imp-url>");
			System.exit(1);
		}

		String source = args[0];
		String userPw = args[1];
		String destAdUrl = args[2];
		String destImpUrl = args[3];

		long lastSize = 0;
		long lastBids = 0;
		int unchanged = 0;
		logPush.start();
		
		InputStream is = null;
		JsonBackupLogReader reader = new JsonBackupLogReader();
		postAd.set(new HTTPPostOutputStream(destAdUrl, userPw,
				"application/json", true));
		postImp.set(new HTTPPostOutputStream(destImpUrl, userPw,
				"application/json", true));
		OneItemLineFileSink<AdEvent> sinkImp = setupSink(postImp.get());
		OneItemLineFileSink<AdEvent> sinkAd = setupSink(postAd.get());

		try {
			do {
				try {
					File f = new File(source);

					long curSize = f.length();
					if (curSize < lastSize || is == null || unchanged > 10) {
						if (is != null) {
							is.close();
						}
						is = new FileInputStream(source);
						unchanged = 0;
					} else if (lastBids == sinkAd.getItemsProcessed()) {
						unchanged++;
					}
					lastBids = sinkAd.getItemsProcessed();
					lastSize = curSize;

					if (source.toLowerCase().endsWith(".gz")) {
						is = new GZIPInputStream(is);
					}

					reader.feed(is, new PublishEventFeed<>(new EventFilterSink(
							sinkImp, AdAction.IMPRESSION, AdAction.CLICK),
							new EventFilterSink(sinkAd, AdAction.AD_SERVED,
									AdAction.UNFILLED_REQUEST,
									AdAction.RTB_FAILED)));

					Thread.sleep(1000);
				} catch (FileNotFoundException | InterruptedException fnfe) {
					// skip fnfe excptions
				}
			} while (true);
		} catch (IOException e) {
			System.err.println("Unable to open/read source: " + e.getMessage());
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException io) {
			}
		}
	}

	private static OneItemLineFileSink<AdEvent> setupSink(
			HTTPPostOutputStream post) {
		OneItemLineFileSink<AdEvent> sink = new OneItemLineFileSink<>(
				new MVELTransformer<AdEvent>(AdEvent.VERSION), post);
		sink.setRenewOnItemCount(400);
		sink.setInfoOnItemCount(10000);
		sink.setInfo(new ItemsInfo() {
			@Override
			public void info(long itemsProcessed, boolean finish) {
				String text = (finish ? "Finished: " : "Processed: ")
						+ itemsProcessed;
				System.out.println(text);
				LOG.info(text);
				if (finish) {
					try {
						ContinuousPushMMXBackupLog.postAd.get().post();
					} catch (IOException e) {
						System.err.println("Unable to post: " + e.getMessage());
					}
				}
			}
		});
		sink.setUpdate(new UpdateStream() {
			@Override
			public OutputStream update(OutputStream os, long itemsProcessed) {
				if (os instanceof HTTPPostOutputStream) {
					HTTPPostOutputStream http = (HTTPPostOutputStream) os;
					try {
						http.post();
					} catch (IOException e) {
						System.err.println("Unable to post: " + e.getMessage());
					}
				}
				return null;
			}
		});
		return sink;
	}

}
