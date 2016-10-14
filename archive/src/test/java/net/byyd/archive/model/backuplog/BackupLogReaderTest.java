package net.byyd.archive.model.backuplog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.model.v1.BackupLogReader;
import net.byyd.archive.transform.EventFeed;
import net.byyd.archive.transform.MVELTransformer;
import net.byyd.archive.transform.util.TransformUtil;

public class BackupLogReaderTest {
	private static final Logger LOG = LoggerFactory.getLogger(BackupLogReader.class);

	private MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(AdEvent.VERSION);
	private PrintWriter pw;
	private AtomicInteger c;

//	@Test
	public void test() throws IOException {
		BackupLogReader reader = new BackupLogReader();
		reader.feed(
				getClass().getResourceAsStream(
						"net/byyd/archive/model/backuplog/input.txt"),
				new EventFeed<AdEvent>() {
					@Override
					public void onEvent(AdEvent adEvent) {
						System.out.println(TransformUtil.oneLineJson(tr
								.transform(adEvent)));
					}

					@Override
					public void finish() {
					}
				});
		
		
	}
	
	public void testToFile() throws IOException {
		BackupLogReader reader = new BackupLogReader();
		pw = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("backup-json.out.gz"))));
		c = new AtomicInteger();

		reader.feed(new FileInputStream("/Users/bijanfathi/adserver-backup2014-08-20-15.1"),
				new EventFeed<AdEvent>() {
					@Override
					public void onEvent(AdEvent adEvent) {
						pw.println(TransformUtil.oneLineJson(tr
								.transform(adEvent)));
						if (c.incrementAndGet() % 100000 == 0) {
							LOG.info("Done: " + c);
						}
					}

					@Override
					public void finish() {
					}
				});
		
		pw.close();
		LOG.info("Finished: " + c);
	}
}
