import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;

public class LotameOnePass {
    public static final HashMap<String,HashMap<String,String>> countrySegmentMap
	= new HashMap<>();
    

    static {
	HashMap<String,String> gb = new HashMap<>();
	gb.put("M29882", "700546");
	gb.put("M58631","700547");
	gb.put("M29886","700548");
	gb.put("M29894","700549");
	gb.put("M29903","700550");
	gb.put("M44171","700551");
	gb.put("M8936","700552");
	gb.put("M29895","700553");
	gb.put("M29899","700554");
	gb.put("M29905","700555");
	gb.put("M44280","700556");
	gb.put("M44205","700557");
	gb.put("M44192","700558");
	gb.put("M44193","700559");
	gb.put("M44204","700560");
	gb.put("M5800","700561");
	gb.put("M44223","700562");
	gb.put("M5799","700563");
	gb.put("M44231","700564");
	gb.put("M44232","700565");
	gb.put("M44241","700566");
	gb.put("M44242","700567");
	gb.put("M44251","700568");
	gb.put("M44252","700569");
	gb.put("M5803","700570");
	gb.put("M5798","700571");
	gb.put("M5802","700572");
	gb.put("M5801","700573");
	gb.put("M44270","700574");
	gb.put("M44271","700575");
	// Age segments
	gb.put("M5327","700604");
	gb.put("M5328","700605");
	gb.put("M5329","700606");
	gb.put("M7402","700607");
	gb.put("M5332","700608");
	gb.put("M3690","700609");
	
	countrySegmentMap.put("M34792", gb);

	HashMap<String,String> au = new HashMap<>();
	au.put("M40439","1081684");
	au.put("M40462","1081685");
	au.put("M40463","1081686");
	au.put("M40486","1081687");
	au.put("M40487","1081688");
	au.put("M40510","1081689");
	au.put("M40511","1081690");
	au.put("M40533","1081691");
	au.put("M40534","1081692");
	au.put("M40557","1081693");
	au.put("M40558","1081694");
	au.put("M40662","1081695");
	au.put("M40735","1081696");
	au.put("M40758","1081697");
	au.put("M40776","1081698");
	au.put("M40800","1081699");
	au.put("M40801","1081700");
	au.put("M40824","1081701");
	au.put("M40831","1081702");
	au.put("M40862","1081703");
	au.put("M40863","1081704");
	au.put("M41246","1081705");
	au.put("M41326","1081706");
	au.put("M41375","1081707");
	au.put("M41391","1081708");
	au.put("M41434","1081709");
	au.put("M41435","1081710");
	
	countrySegmentMap.put("M37979", au);
    }
    
    public static void main(String[] args) throws Exception {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String line;
	String[] splitLine;
	String did;
	StringBuffer sb;
	String[] splitSegments;
	boolean isAndroid;
	MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
	
	while ((line = br.readLine()) != null) {
	    splitLine = line.split("\t");
	    did = splitLine[0];
	    if (did.length() == 36) {
		// Valid raw IDFA or ADID
		splitSegments = splitLine[1].split(",");
		List<String> listSegments = Arrays.asList(splitSegments);
		isAndroid = listSegments.contains("M15661");

		for (String countrySegment : countrySegmentMap.keySet()) {
		    if (listSegments.contains(countrySegment)) {
			HashMap<String,String> segmentMap = countrySegmentMap.get(countrySegment);
			for (String lotameSegment : segmentMap.keySet()) {
			    if (listSegments.contains(lotameSegment)) {
				if (isAndroid) {
				    System.out.println(did.toLowerCase() + "\t9\t" + segmentMap.get(lotameSegment));
				} else {
				    sha1.reset();
				    sha1.update(did.toUpperCase().getBytes());
				    System.out.println(DatatypeConverter.printHexBinary(sha1.digest()).toLowerCase() + "\t7\t" + segmentMap.get(lotameSegment));
				}
			    }
			}
		    }
		}
	    }
	}
    }
}
