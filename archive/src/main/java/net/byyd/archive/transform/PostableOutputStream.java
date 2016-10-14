package net.byyd.archive.transform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class PostableOutputStream extends OutputStream {
	
	protected ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
	protected OutputStream out = baos;
	protected boolean gzip;

	public PostableOutputStream(boolean gzip) {
		super();
		this.gzip = gzip;
		if (gzip) {
			setupGzip();
		}
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	protected void setupGzip() {
		try {
			out = new GZIPOutputStream(baos);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create gzip stream");
		}
	}
	
	@Override
	public void flush() throws IOException {
		out.flush();
	}
	
	@Override
	public void close() throws IOException {
		out.close();
	}
}
