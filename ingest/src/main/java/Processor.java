import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Processor {
    public static void main(String[] args) throws Exception {
	BufferedReader br = new BufferedReader(new FileReader(args[0]));
	PrintWriter devices = new PrintWriter(new BufferedWriter(new FileWriter("device.out")));
	PrintWriter segments = new PrintWriter(new BufferedWriter(new FileWriter("segments.out")));
	String line;
	String[] splitLine;
	String did;
	StringBuffer sb;
	String[] splitSegments;
	int count = 0;
	if (args.length > 1) count = Integer.parseInt(args[1]);

	while ((line = br.readLine()) != null) {
	    splitLine = line.split("\t");
	    did = splitLine[0];
	    if (did.length() == 36) {
		++count;
		// Valid raw IDFA or ADID
		devices.println(count + "\t" + did.toLowerCase());
		splitSegments = splitLine[1].split(",");
		for (int i = 0; i < splitSegments.length; i++) {
		    segments.println(splitSegments[i].substring(1) + "\t" + count);
		}
	    }
	}
	System.out.println(count);
	devices.flush();
	devices.close();
	segments.flush();
	segments.close();
    }
}
