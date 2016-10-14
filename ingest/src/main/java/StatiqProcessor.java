import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class StatiqProcessor {
    public static void main(String[] args) throws Exception {
	BufferedReader br = new BufferedReader(new FileReader(args[0]));
	PrintWriter devices = new PrintWriter(new BufferedWriter(new FileWriter("device.out")));
	PrintWriter segments = new PrintWriter(new BufferedWriter(new FileWriter("segments.out")));
	String line;
	String[] splitLine;
	String did;
	String isoCode;
	int didType = 0;
	StringBuffer sb;
	String[] splitSegments;
	int count = 0;
	if (args.length > 1) count = Integer.parseInt(args[1]);

	while ((line = br.readLine()) != null) {
	    splitLine = line.split(",");
	    did = splitLine[0];
	    if (did.length() == 36) {
		// Valid raw IDFA or ADID
		// TODO - map country code to ID - for now it's all GB=150
		didType = Integer.parseInt(splitLine[1]);
		switch (didType) {
		case 1: didType = 6; break;
		case 2: didType = 9; break;
		default: continue;
		}
		++count;
		isoCode = splitLine[2];
		if (!"GB".equals(isoCode)) {
		    throw new RuntimeException("Only know how to deal with GB");
		}
		devices.println(count + "\t" + did + "\t150\t" + didType);
		splitSegments = splitLine[3].split("\\|");
		for (int i = 0; i < splitSegments.length; i++) {
		    segments.println(splitSegments[i] + "\t" + count);
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
