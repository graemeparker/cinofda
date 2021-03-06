package net.byyd.archive.model.v1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import net.byyd.archive.transform.OneItemLineFileSink;
import net.byyd.archive.transform.OneItemLineFileSink.ItemsInfo;
import net.byyd.archive.transform.OneItemLineFileSink.UpdateStream;
import net.byyd.archive.transform.S3PostOutputStream;
import net.byyd.archive.transform.SimpleTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushS3BackupLog {
	private static final Logger LOG = LoggerFactory
			.getLogger(PushS3BackupLog.class);

	public static final ThreadLocal<S3PostOutputStream> post = new ThreadLocal<>();

	public static void main(String[] args) {

		if (args.length != 3) {
			System.out
					.println("Usage: PushMMXBackupLog <backup-log-file> <accKey> <secKey>");
			System.exit(1);
		}

		String source = args[0];
		String accKey = args[1];
		String secKey = args[2];

		InputStream is;
		try {
			is = new FileInputStream(source);
			if (source.toLowerCase().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}

			JsonBackupLogReader reader = new JsonBackupLogReader();
			post.set(new S3PostOutputStream(
					"byydarchive",
					"events/@{date('yyyy')}/@{date('MM')}/@{date('dd')}/@{env('HOSTNAME')}/"
							+ "adevent-v1-@{env('HOSTNAME')}-@{date('yyyy-MM-dd-HH-mm')}-@{arg0}.json.gz",
					accKey, secKey, true));
			OneItemLineFileSink<AdEvent> sink = setupSink(post.get());

			reader.feed(is, sink);
		} catch (IOException e) {
			System.err.println("Unable to open/read source: " + e.getMessage());
		}
	}

	private static OneItemLineFileSink<AdEvent> setupSink(
			S3PostOutputStream post) {
		OneItemLineFileSink<AdEvent> sink = new OneItemLineFileSink<>(
				new SimpleTransformer<AdEvent>(), post);

		sink.setRenewOnItemCount(50_000);
		sink.setInfoOnItemCount(50_000);

		sink.setInfo(new ItemsInfo() {
			@Override
			public void info(long itemsProcessed, boolean finish) {
				String text = (finish ? "Finished: " : "Processed: ")
						+ itemsProcessed;
				System.out.println(text);
				LOG.info(text);
				if (finish) {
					try {
						PushS3BackupLog.post.get().post();
					} catch (IOException e) {
						System.err.println("Unable to post: " + e.getMessage());
					}
				}
			}
		});
		sink.setUpdate(new UpdateStream() {
			@Override
			public OutputStream update(OutputStream os, long itemsProcessed) {
				if (os instanceof S3PostOutputStream) {
					S3PostOutputStream http = (S3PostOutputStream) os;
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
