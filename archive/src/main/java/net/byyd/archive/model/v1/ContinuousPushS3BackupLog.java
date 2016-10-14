package net.byyd.archive.model.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import net.byyd.archive.transform.ManualFinishFeed;
import net.byyd.archive.transform.OneItemLineFileSink;
import net.byyd.archive.transform.OneItemLineFileSink.ItemsInfo;
import net.byyd.archive.transform.OneItemLineFileSink.UpdateStream;
import net.byyd.archive.transform.S3PostOutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class ContinuousPushS3BackupLog {
	private static final Logger LOG = LoggerFactory
			.getLogger(ContinuousPushS3BackupLog.class);

	public static final ThreadLocal<S3PostOutputStream> post = new ThreadLocal<>();

	private static AmazonSQSClient sqsClient;

	private static ObjectMapper mapper = new ObjectMapper();
	private static String notificationQueue = "https://sqs.us-east-1.amazonaws.com/189908807348/aepq";
	private static String lastFile = "/usr/local/adfonic/adfonic-archive/last";

	private static LoggingPush logPush = new LoggingPush() {
		public Logger getLog() {return LOG;}
		public String getMessage() {
			return "S3 pushes: " + post.get().getPushes(); 
		}
	};
	
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out
					.println("Usage: ContinuousPushS3BackupLog <backup-log-file> <accKey> <secKey>");
			System.exit(1);
		}

		String source = args[0];
		String accKey = args[1];
		String secKey = args[2];

		long lastSize = 0;
		long lastItems = 0;
		int unchanged = 0;

		setupAWSClients(accKey, secKey);
		logPush.start();

		InputStream is = null;
		SingleLineLogReader reader = setupReader();
		post.set(new S3PostOutputStream(
				"byydarchive",
				"events/@{date('yyyy')}/@{date('MM')}/@{date('dd')}/@{env('HOSTNAME')}/"
						+ "adevent-v1-@{env('HOSTNAME')}-@{date('yyyy-MM-dd-HH-mm')}-@{arg0}.json.gz",
				accKey, secKey, true));

		boolean firstRun = true;
		OneItemLineFileSink<String> sink = setupSink(post.get());
		ManualFinishFeed<String> mfSink = new ManualFinishFeed<>(sink);
		LOG.info("Start reading: " + source);
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
						if (source.toLowerCase().endsWith(".gz")) {
							is = new GZIPInputStream(is);
						}
						unchanged = 0;
					} else if (lastItems == sink.getItemsProcessed()) {
						unchanged++;
					}
					lastItems = sink.getItemsProcessed();
					lastSize = curSize;
					if (firstRun) {
						skipToLastLineAndHash(reader);
					}
					reader.feed(is, mfSink);

					firstRun = false;
					Thread.sleep(1000);
				} catch (FileNotFoundException fnfe) {
					LOG.warn("File not found: " + fnfe.getMessage());
				} catch (InterruptedException fnfe) {
					// skip excptions
				} catch (RuntimeException e) {
					LOG.warn("Unable to process file: ", e);
				}
			} while (true);
		} catch (IOException e) {
			LOG.warn("Unable to open/read source: " + e.getMessage());
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException io) {
			}
		}
	}

	private static void setupAWSClients(String accKey, String secKey) {
		LOG.info("Setting up AWS Clients.");
		BasicAWSCredentials awsCred = new BasicAWSCredentials(accKey, secKey);
		sqsClient = new AmazonSQSClient(awsCred);
	}

	private static SingleLineLogReader setupReader() {
		return new SingleLineLogReader(
				new SingleLineLogReader.ProgressNotifier() {

					@Override
					public void notify(String hash, int lineNr) {
						setLastLineAndHash(hash, lineNr);
					}
				});
	}

	private static void setLastLineAndHash(String lastHash, int lineNr) {
		writeFile(lastHash, lineNr, 0);
	}

	private static void skipToLastLineAndHash(SingleLineLogReader reader) {
		String[] last = readFile();
		
		if (last != null && last.length == 3) {
			String lastLine = last[1];
			String lastLineHash = last[0];
			reader.skipToLine(Integer.parseInt(lastLine), lastLineHash);
		}
	}

	private static String[] readFile() {
		String[] ret = null;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(lastFile)));
			String line = br.readLine();
			
			if (line != null) {
				ret = line.split(";");
			}
			br.close();
		} catch (Throwable t) {
			LOG.warn("Unable to read initial position: " + t, t);
		}
				
		return ret;
	}

	private static void writeFile(String hash, long items, long pushes) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(lastFile);
			fos.write((hash + ";" + items + ";" + pushes).getBytes());
			fos.close();
		} catch( Throwable t) {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static OneItemLineFileSink<String> setupSink(
			final S3PostOutputStream post) {
		OneItemLineFileSink<String> sink = new OneItemLineFileSink<>(null, post);

		sink.setRenewOnItemCount(100_000);
		sink.setInfoOnItemCount(100_000);

		sink.setInfo(new ItemsInfo() {
			@Override
			public void info(long itemsProcessed, boolean finish) {
				String text = (finish ? "Finished: " : "Processed: ")
						+ itemsProcessed;
				LOG.info(text);
			}
		});
		sink.setUpdate(new UpdateStream() {

			@Override
			public OutputStream update(OutputStream os, long itemsProcessed) {
				if (os instanceof S3PostOutputStream) {
					S3PostOutputStream http = (S3PostOutputStream) os;
					try {
						String name = http.getLastFileName();
						http.post();
						sendCompletionNotification(name, itemsProcessed);
					} catch (IOException e) {
						LOG.warn("Unable to post: " + e.getMessage());
					}
				}
				return null;
			}

			private void sendCompletionNotification(String name,
					long itemsProcessed) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Sending notification for " + name);
				}
				CompletionMessage cm = new CompletionMessage();
				cm.setTime(new Date().getTime());
				cm.setType("completion");
				cm.setName(name);
				try {
					sqsClient.sendMessage(notificationQueue,
							mapper.writeValueAsString(cm));
				} catch (IOException e) {
					LOG.warn("Unable to write json: " + e);
				}
			}
		});
		return sink;
	}
}
