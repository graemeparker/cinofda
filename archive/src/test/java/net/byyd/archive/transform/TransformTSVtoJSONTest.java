package net.byyd.archive.transform;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformTSVtoJSONTest {
	private static final Logger LOG = LoggerFactory
			.getLogger(TransformTSVtoJSONTest.class);

	private String[] files = new String[] { "campaign",
			"creative", "format", "model", "operator", "publication",
			"publisher", "device_identifier_type", "rtb_config",
			// new
			"vendor", "platform",

			"bidder",
			// "sales_office",
			"owner", "category", "advertiser"};

	private String[][] cols = new String[][] {
			{ "id", "name", "external_id", "advertiser_id", "category_id",
					"reference" },
			{ "id", "campaign_id", "external_id", "format_id", "name" },
			{ "id", "name", "system_name" },
			{ "id", "name", "external_id", "vendor_id", "platform_id" },
			{ "id", "name", "country_id", "operator_group_id" },
			{ "id", "name", "publication_type_id ", "category_id",
					"publisher_id", "external_id" },
			{ "id", "name", "rtb_enabled", "rtb_config_id", "company_id" },
			{ "id", "name", "system_name" },
			{ "id", "win_notice_mode", "auction_type" },

			// new
			{ "id", "name" }, { "id", "name" }, { "id", "name", "email" },
			// { "id", "name" },
			{ "id", "name", "email" }, { "id", "name", "iab_id" }, 
			{ "id", "name", "ad_ops_owner_id", "sales_owner_id", "bidder_id", "sales_office" },};

	Map<String, List<Map<String, String>>> contents = new HashMap<>();
	Map<String, Map<String, Map<String, String>>> contentMap = new HashMap<>();

//	@Test
	public void test() throws IOException {
		String path = "";
		int n = 0;

		for (String f : files) {
			System.out.println("Converting: " + f);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getClass().getClassLoader().getResourceAsStream(
							path + f + ".tsv")));

			List<Map<String, String>> l = new ArrayList<>();
			contents.put(f, l);
			Map<String, Map<String, String>> cfm = new HashMap<>();
			contentMap.put(f, cfm);

			String line;
			Map<String, Integer> headers = null;
			while ((line = br.readLine()) != null) {
				String[] sp = line.split("\t");
				if (headers == null) {
					headers = new HashMap<>();
					for (int i = 0; i < sp.length; i++) {
						headers.put(sp[i].toLowerCase().trim(), i);
					}
					continue;
				}

				Map<String, String> m = new HashMap<>();
				l.add(m);

				for (int i = 0; i < cols[n].length; i++) {
					String key = cols[n][i];
					Integer ix = headers.get(key.trim());

					if (ix != null && ix < sp.length) {
						String value = sp[ix];
						m.put(key, escape(value.trim()));
					} else {
						LOG.warn("Invalid line: " + line + " / " + ix + "/ "
								+ key);
					}
				}
				if (cfm.put(m.get(cols[n][0]), m) != null) {
					LOG.warn("Collision detected: " + m.get(cols[n][0])
							+ " in " + f);
				}
			}

			PrintWriter pw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(f + ".json")));

			l = contents.get(f);
			for (Map<String, String> m : l) {
				pw.print("{");
				boolean advFirst = true;
				for (int i = 0; i < cols[n].length; i++) {
					String key = cols[n][i];
					String keyN = key;
					if ("reference".equals(key)) {
						keyN = "io_reference";
					}
					if ("sales_office".equals(key)) {
						String value = "1";
						String name = "byyd UK";
						String sales_office = m.get(cols[0][3]);
						if ("83".equals(sales_office)) {
							value = "3";
							name = "byyd FR";
						} else if ("94".equals(sales_office)
								|| "136".equals(sales_office)) {
							value = "2";
							name = "byyd DE";
						}
						pw.print("\"" + keyN + "\": \"" + value + "\", ");
						pw.print("\"" + keyN + "_name" + "\": \"" + name + "\"");
					} else {
						pw.print("\"" + keyN + "\": \"" + m.get(key) + "\"");
					}
					
					if ("advertiser".equals(f) && advFirst) {
						List<Map<String, String>> bidder = contents.get("bidder");
						List<Map<String, String>> owner = contents.get("owner");
						pw.print(", \"bidder_name\": \"" + find(bidder, m.get("bidder_id"), "name") + "\", ");
						pw.print("\"bidder_email\": \"" + find(bidder, m.get("bidder_id"), "email") + "\", ");
						pw.print("\"ad_ops_owner_name\": \"" + find(owner, m.get("ad_ops_owner_id"), "name") + "\", ");
						pw.print("\"ad_ops_owner_email\": \"" + find(owner, m.get("ad_ops_owner_id"), "email") + "\", ");
						pw.print("\"sales_owner_name\": \"" + find(owner, m.get("sales_owner_id"), "name") + "\", ");
						pw.print("\"sales_owner_email\": \"" + find(owner, m.get("sales_owner_id"), "email") + "\" ");
						advFirst = false;
					}

					if (i < cols[n].length - 1) {
						pw.print(", ");
					}
				}
				pw.println("}");
			}
			pw.close();
			n++;
			if (!"owner".equals(f) && !"bidder".equals(f)) {
				contents.remove(f);
			}
		}
	}

	private String find(List<Map<String, String>> entries, String key, String name) {
		String retval = null;
		
		for(Map<String,String> m : entries) {
			String keyV = m.get("id");
			if (keyV != null && keyV.equals(key)) {
				retval = m.get(name);
			}
		}
		
		return retval;
	}

	private String escape(String value) {
		StringBuilder builder = new StringBuilder();

		if (value != null) {
			for (char c : value.toCharArray()) {
				switch (c) {
				case '"':
				case '\\':
				case '/':
				case '\f':
					builder.append("\\");
				default:
					if (c >= 32 && c < 256) {
						builder.append(c);
					} else {
						if (c > 0) {
							builder.append("\\u").append(
									String.format("%04d", (int) c));
						}
					}
				}
			}
		}

		return builder.toString();
	}
}
