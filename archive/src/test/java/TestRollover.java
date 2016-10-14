import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.junit.Test;

public class TestRollover {

	public void test() throws IOException, InterruptedException {
		String in = "bin/test.txt";
		pushCont(in, System.out);
	}

	public void push(String path, OutputStream os) throws IOException {
		BufferedReader br = getReader(path);
		PrintWriter pw = new PrintWriter(os);

		String line = null;
		while ((line = br.readLine()) != null) {
			pw.println("L: " + line);
			pw.flush();
		}
	}

	public void pushCont(String path, OutputStream os) throws IOException,
			InterruptedException {
		long lastSize = 0;

		PrintWriter pw = new PrintWriter(os);
		BufferedReader br = null;
		do {
			try {
				File f = new File(path);

				if (f.length() < lastSize || br == null) {
					br = getReader(path);
				}

				String line = null;
				while ((line = br.readLine()) != null) {
					pw.println("L: " + line);
					pw.flush();
				}
				lastSize = f.length();
				Thread.sleep(100);
			} catch (FileNotFoundException fnfe) {
				// skip fnfe excptions
			}
		} while (true);

	}

	private BufferedReader getReader(String path) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(path)));
		return br;
	}

}

