package net.byyd.archive.model.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggingPush {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingPush.class);
	
	private final LogThread lt;
	private static final int TIME = 60_000;
	
	public LoggingPush() {
		lt = new LogThread();
	}
	
	public void start() {
		lt.start();
	}

	public class LogThread extends Thread {
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(TIME);
					getLog().info("Update: " + getMessage());
				} catch (Exception e) {
				}
			}
		}
	}
	
	public Logger getLog() {
		return LOG;
	}
	
	public String getMessage() {
		return "";
	}
}
