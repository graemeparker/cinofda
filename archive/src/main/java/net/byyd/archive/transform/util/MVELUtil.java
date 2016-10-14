package net.byyd.archive.transform.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

public class MVELUtil {

	public static DateProvider dateProvider = new DateProvider() {
		@Override
		public Date date() {
			return new Date();
		}
	};

	private static final MVELUtil INSTANCE = new MVELUtil();

	private ParserContext context;

	private MVELUtil() {
		try {
			context = new ParserContext();
			context.addImport("date", getClass()
					.getMethod("date", String.class));
			context.addImport("env", getClass().getMethod("env", String.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Unable to transform template: "
					+ e.getMessage(), e);
		}
	}

	public static String resolve(String input, Object... args) {
		try {
			CompiledTemplate t = TemplateCompiler.compileTemplate(input,
					INSTANCE.getContext());

			Map<String, Object> m = new HashMap<>();
			if (args != null) {
				int i = 0;
				for (Object o : args) {
					m.put("arg" + i++, o);
				}
			}

			String result = (String) TemplateRuntime.execute(t, m);
			return result;
		} catch (SecurityException e) {
			throw new RuntimeException("Unable to transform template: "
					+ e.getMessage(), e);
		}
	}

	public static String date(String pattern) {
		return new SimpleDateFormat(pattern).format(new Date());
	}

	public static String env(String name) {
		return System.getenv(name);
	}

	public interface DateProvider {
		Date date();
	}

	public ParserContext getContext() {
		return context;
	}
}
