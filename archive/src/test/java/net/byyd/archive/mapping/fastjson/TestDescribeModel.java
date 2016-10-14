package net.byyd.archive.mapping.fastjson;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import net.byyd.archive.model.v1.AdEvent;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestDescribeModel {

	@Test
	public void test() {
		Map<String, Field> fields = new TreeMap<String, Field>();

		for (Field f : AdEvent.class.getDeclaredFields()) {
			JsonProperty jp = f.getAnnotation(JsonProperty.class);
			String name = f.getName();

			if (jp != null) {
				name = jp.value();
			}

			if (fields.put(name, f) != null) {
				System.err.println("Invalid duplicate field: " + name + " for "
						+ f.getName());
			}
		}

		StringBuilder line = new StringBuilder();
		for (Field f : fields.values()) {
			JsonProperty jp = f.getAnnotation(JsonProperty.class);
			String name = f.getName();

			if (jp != null) {
				name = jp.value();
			}

			String methPost = f.getName().substring(0, 1).toUpperCase()
					+ f.getName().substring(1) + "()";

			Class<?> clazz = f.getType();
			
			String methName = null;
			if (String.class.equals(clazz)) {
				line.append("writeString");
				methName = "get" + methPost;
			} else if (clazz.isEnum()) {
				line.append("writeEnum");
				methName = "get" + methPost;
			} else if (Date.class.equals(clazz)) {
				line.append("writeDate");
				methName = "get" + methPost;
			} else if (Number.class.isAssignableFrom(clazz)
					|| Long.TYPE.equals(clazz) || Integer.TYPE.equals(clazz)
					|| Short.TYPE.equals(clazz)) {
				line.append("write");
				methName = "get" + methPost;
			} else if (Boolean.class.isAssignableFrom(clazz)
					|| Boolean.TYPE.equals(clazz)) {
				line.append("write");
				methName = "is" + methPost;
			} else if (Map.class.isAssignableFrom(clazz)) {
				line.append("writeMap");
				methName = "get" + methPost;
			} else {
				System.err.println("Unsupported class: " + clazz + " of field "
						+ f.getName());
			}
			line.append("(\"").append(name).append("\", o.").append(methName).append(", d.").append(methName).append(", sb);");
			System.out.println(line);
			line.setLength(0);
		}

	}
}
