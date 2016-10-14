package net.byyd.archive.transform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import net.byyd.archive.transform.util.MVELUtil;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class S3PostOutputStream extends PostableOutputStream {

	private static final String DATE_PATTERN = "@{date('yyyy-MM-dd-HH-mm')}";

	private String namePattern;
	private AmazonS3Client s3Client;
	private String bucket;

	private String lastDate;
	private int fileNumber;
	private String endpoint = "https://s3.amazonaws.com";
	private String lastFileName;
	private long pushes = 0;

	public S3PostOutputStream(String bucket, String namePattern,
			String accessKey, String secretKey, boolean gzip) {
		super(gzip);

		this.bucket = bucket;
		this.namePattern = namePattern;
		this.s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey,
				secretKey));
		this.s3Client.setEndpoint(endpoint);
	}

	public void post() throws IOException {
		String curDate = MVELUtil.resolve(DATE_PATTERN);
		if (lastDate == null || !lastDate.equals(curDate)) {
			lastDate = curDate;
			fileNumber = 0;
		}

		String name = MVELUtil.resolve(namePattern, String.format("%05d", ++fileNumber));
		ObjectMetadata meta = new ObjectMetadata();

		if (out instanceof GZIPOutputStream) {
			((GZIPOutputStream) out).finish();
		}
		out.flush(); 
		byte[] arr = baos.toByteArray();
		meta.setContentLength(arr.length);

		s3Client.putObject(bucket, name, new ByteArrayInputStream(arr), meta);
		pushes++;

		baos.reset();
		lastFileName = name;

		if (gzip) {
			setupGzip();
		}
	}
	
	public String getLastFileName() {
		return lastFileName;
	}
	
	public long getPushes() {
		return pushes;
	}
}
