package net.byyd.archive.transform;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import net.byyd.archive.model.v1.AdAction;
import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.transform.util.MVELUtil;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MVELTransformer<T> implements Transformer<T, String> {
	private static final Logger LOG = LoggerFactory
			.getLogger(MVELTransformer.class);

	private Map<AdAction, CompiledTemplate> templates = new HashMap<>();
	private String templateAdName = "/net/byyd/archive/transform/map@{arg0}@{arg1}.json";
	private List<AdAction> actions = Arrays.asList(new AdAction[] {
			AdAction.AD_SERVED, AdAction.CLICK, AdAction.IMPRESSION,
			AdAction.UNFILLED_REQUEST, AdAction.RTB_FAILED, AdAction.RTB_LOST });
	private String version;
	private static AtomicLong count = new AtomicLong();
	public static Date TEST_DATE;
	private ObjectMapper om = new ObjectMapper();

	private ThreadLocal<DateFormat> formatIso = new ThreadLocal<DateFormat>() {
		public DateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:SS.sss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};

	private ThreadLocal<DateFormat> formatYear = new ThreadLocal<DateFormat>() {
		public DateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};

	public MVELTransformer(String version) {
		try {
			this.version = version;
			for (AdAction a : actions) {
				String resolvedName = MVELUtil.resolve(templateAdName, a.name(), version);
                InputStream res = getClass().getResourceAsStream(resolvedName);
				if (res != null) {
					String t = IOUtils.toString(res);
					ParserContext context = new ParserContext();
					context.addImport("dateFmt",
							getClass().getMethod("dateFmt", Date.class));
					context.addImport("keySet",
							getClass().getMethod("keySet", Map.class));
					context.addImport("hashFirstDeviceId", getClass()
							.getMethod("hashFirstDeviceId", Map.class));
					context.addImport("dateFmtYear",
							getClass().getMethod("dateFmtYear", Date.class));
					context.addImport("randomHash",
							getClass().getMethod("randomHash"));
					templates.put(a,
							TemplateCompiler.compileTemplate(t, context));
				} else {
					LOG.warn("Unable to locate template " + a + " in version " + version);
				}
			}
		} catch (IOException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Unable to load template for AdEvents",
					e);
		}
	}

	@Override
	public String transform(T model) {
		Map<String, Object> vars = new HashMap<>();
		vars.put("adEvent", model);

		AdEvent ae = (AdEvent) model;
		CompiledTemplate template = templates.get(ae.getAdAction());
		String result = "";
		if (template != null) {
			result = (String) TemplateRuntime.execute(template, vars);
			try {
				om.readTree(result);
			} catch (IOException e) {
				LOG.warn("Unparsable result: " + result);
				result = "";
			}
		}

		return result;
	}

	public static String dateFmt(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	public static String dateFmtYear(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return date != null ? sdf.format(date) : "";
	}

	public static Object lookup(Map<String, Object> map, String key) {
		return new SimpleDateFormat("yyyy").format(key);
	}

	public static String keySet(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();

		if (map != null) {
			for (Object o : map.keySet()) {
				String s = o != null ? o.toString() : null;
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(s);
			}
		}

		return sb.toString();
	}

	public static String hashFirstDeviceId(Map<Long, String> map) {
		String retval = "";

		if (map != null && !map.isEmpty()) {
			SortedMap<Long, String> sorted = new TreeMap<Long, String>(map);
			String key = sorted.get(sorted.firstKey());
			try {
				retval = hashMD5(key);
			} catch (NoSuchAlgorithmException e) {
			}
		}

		return retval;
	}

	public static String randomHash() throws NoSuchAlgorithmException {
		return hashMD5((TEST_DATE == null ? new Date() : TEST_DATE) + "-"
				+ count.incrementAndGet());
	}

	public static String hashMD5(String key) throws NoSuchAlgorithmException {
		String retval;
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(key.getBytes());
		retval = new String(Hex.encodeHex(md.digest()));
		return retval;
	}
}
