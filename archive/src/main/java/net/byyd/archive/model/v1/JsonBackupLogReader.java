package net.byyd.archive.model.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.byyd.archive.mapping.AnnotationNamePropertyNamingStrategy;
import net.byyd.archive.transform.EventFeed;

import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonBackupLogReader {
	private static final Logger LOG = LoggerFactory
			.getLogger(JsonBackupLogReader.class);

	private ObjectMapper om;

	private ProgressNotifier notifier;
	private int notificationIntervalLines = 1000;
	
	private long items;
	
	private String hostname;
	private String shard;
	
	public interface ProgressNotifier {
		void notify(String line, int lineNr);
	}

	public JsonBackupLogReader() {
		this(null);
	}
	
	public JsonBackupLogReader(ProgressNotifier notifier) {
		om = new ObjectMapper();
		om.setPropertyNamingStrategy(new AnnotationNamePropertyNamingStrategy());
		this.notifier = notifier;
	}

	public void feed(InputStream is, EventFeed<AdEvent> feed)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line; int nr = 0;
		while ((line = br.readLine()) != null) {
			try {
				AdEvent ae = om.readValue(line.replaceAll("\\\\0", "\\u0"), AdEvent.class);
				ae.setMessageHash(hashMD5(line.trim()));
				ae.setServerName(hostname);
				ae.setShard(shard);
				ae.setRawMessage(line);
				feed.onEvent(ae);
				items++;
				if (nr % notificationIntervalLines == 0 && notifier != null) {
					notifier.notify(line, nr);
				}
			} catch (Exception e) {
				LOG.warn("Unable to read adevent log:" + e.getMessage() + " / "
						+ line);
			}
		}

		feed.finish();
	}
	
	private static String hashMD5(String key) throws NoSuchAlgorithmException {
		String retval;
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(key.getBytes());
		retval = new String(Hex.encodeHex(md.digest()));
		return retval;
	}

	public long getItems() {
		return items;
	}

	public void setItems(long items) {
		this.items = items;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getShard() {
		return shard;
	}

	public void setShard(String shard) {
		this.shard = shard;
	}
}
