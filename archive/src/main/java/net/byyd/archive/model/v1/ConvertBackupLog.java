package net.byyd.archive.model.v1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import net.byyd.archive.transform.MVELTransformer;
import net.byyd.archive.transform.OneItemLineFileSink;
import net.byyd.archive.transform.OneItemLineFileSink.ItemsInfo;
import net.byyd.archive.transform.SimpleTransformer;

public class ConvertBackupLog {

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out
					.println("Usage: ConvertBackupLog <backup-log-file> <destination-file>");
			System.exit(1);
		}

		String source = args[0];
		String dest = args[1];

		InputStream is;
		try {
			is = new FileInputStream(source);
			OutputStream os = new FileOutputStream(dest);
			if (source.toLowerCase().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
	
			BackupLogReader reader = new BackupLogReader();
			OneItemLineFileSink<AdEvent> sink = new OneItemLineFileSink<>(new SimpleTransformer<AdEvent>(), os);
			sink.setInfoOnItemCount(10000);
			sink.setInfo(new ItemsInfo() {
				@Override
				public void info(long itemsProcessed, boolean finish) {
					System.out.println((finish ? "Finished: " : "Processed: ") + itemsProcessed);
				}
			});

			reader.feed(is,	sink);
		} catch (IOException e) {
			System.err.println("Unable to open/read source: " + e.getMessage());
		}
	}
	
}
