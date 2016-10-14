package net.byyd.archive.model.v1;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.byyd.archive.transform.HTTPPostOutputStream;
import net.byyd.archive.transform.MVELTransformer;
import net.byyd.archive.transform.OneItemLineFileSink;
import net.byyd.archive.transform.OneItemLineFileSink.ItemsInfo;
import net.byyd.archive.transform.OneItemLineFileSink.UpdateStream;

public class PushMMXBackupLog {
	private static final Logger LOG = LoggerFactory.getLogger(PushMMXBackupLog.class);
	
	public static final ThreadLocal<HTTPPostOutputStream> post = new ThreadLocal<>();

	public static void main(String[] args) {

		if (args.length != 3) {
			System.out
					.println("Usage: PushMMXBackupLog <backup-log-file> <user:password> <destination-url>");
			System.exit(1);
		}

		String source = args[0];
		String userPw = args[1];
		String destUrl = args[2];

		InputStream is;
		try {
			is = new FileInputStream(source);
			if (source.toLowerCase().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
	
			JsonBackupLogReader reader = new JsonBackupLogReader();
			post.set(new HTTPPostOutputStream(destUrl, userPw, "application/json", true));
			OneItemLineFileSink<AdEvent> sink = setupSink(post.get());

			reader.feed(is,	new EventFilterSink(sink, AdAction.AD_SERVED, AdAction.UNFILLED_REQUEST));
		} catch (IOException e) {
			System.err.println("Unable to open/read source: " + e.getMessage());
		}
	}

	private static OneItemLineFileSink<AdEvent> setupSink(
			HTTPPostOutputStream post) {
		OneItemLineFileSink<AdEvent> sink = new OneItemLineFileSink<>(new MVELTransformer<AdEvent>(AdEvent.VERSION), post);
		sink.setRenewOnItemCount(400);
		sink.setInfoOnItemCount(10000);
		sink.setInfo(new ItemsInfo() {
			@Override
			public void info(long itemsProcessed, boolean finish) {
				String text = (finish ? "Finished: " : "Processed: ") + itemsProcessed;
				System.out.println(text);
				LOG.info(text);
				if (finish) {
					try {
						PushMMXBackupLog.post.get().post();
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
