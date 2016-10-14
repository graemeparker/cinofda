package net.byyd.archive.model.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.byyd.archive.transform.EventFeed;
import net.byyd.archive.transform.MVELTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleLineLogReader {
	private static final Logger LOG = LoggerFactory
			.getLogger(SingleLineLogReader.class);

	private ProgressNotifier notifier;
	private int notificationIntervalLines = 1000;
	private int skipLines = 0;
	private String firstLineHash;
	private long lastKnownLine = 0;

	public interface ProgressNotifier {
		void notify(String line, int lineNr);
	}

	public SingleLineLogReader() {
		this(null);
	}

	public SingleLineLogReader(ProgressNotifier notifier) {
		this.notifier = notifier;
	}

	public void feed(InputStream is, EventFeed<String> feed) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String firstLine = null, curFirstLineHash = null;
		String line;
		while ((line = br.readLine()) != null) {
			try {
				if (firstLine == null) {
					firstLine = line;
					curFirstLineHash = MVELTransformer.hashMD5(firstLine);
				}
				if (firstLineHash != null) {
					if (curFirstLineHash.equals(firstLineHash)) {
						for (int i = 0; i < skipLines; i++) {
							line = br.readLine();
						}
						lastKnownLine += skipLines;
					}
				}

				feed.onEvent(line);
				lastKnownLine++;

				if (lastKnownLine % notificationIntervalLines == 0 && notifier != null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Notifiying " + lastKnownLine);
					}
					notifier.notify(curFirstLineHash, (int) lastKnownLine);
				}
			} catch (Exception e) {
				LOG.warn("Unable to read adevent log:" + e.getMessage() + " / "
						+ line);
			}
		}

		feed.finish();
	}

	public void skipToLine(int skipLines, String firstHash) {
		this.skipLines = skipLines;
		this.firstLineHash = firstHash;
	}
}
