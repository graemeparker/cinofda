package net.byyd.archive.transform.util;

public class TransformUtil {

	public static String oneLineJson(String json) {
		return json.replaceAll("[\\r\\n\\t]", "");
	}
}
