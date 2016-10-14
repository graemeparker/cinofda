package net.byyd.archive.mapping.fastjson;

import java.util.Date;
import java.util.Map;

public abstract class AbstractFastJsonWriter<T> {
	protected String quote = "\"";
	protected String colon = ":";
	protected String backslash = "\\";
	protected String comma = ",";
	protected String openBracket = "{";
	protected String closeBracket = "}";

	protected boolean writeEmpty = false;
	protected boolean writeDefault = false;

	public boolean writeString(String name, String value, String defaultValue,
			StringBuilder builder) {
		boolean retval = false;

		if (verifyEmptyOrDefault(value, defaultValue,
				value != null && value.isEmpty())) {
			writeSeparator(builder);
			builder.append(quote).append(name).append(quote).append(colon)
					.append(quote);
			writeJsonEscaped(value, builder);
			builder.append(quote);
			retval = true;
		}

		return retval;
	}

	public boolean writeEnum(String name, Enum<?> value, Enum<?> defaultValue,
			StringBuilder builder) {
		return writeString(name, value != null ? value.toString() : null,
				defaultValue != null ? defaultValue.toString() : null, builder);
	}

	public boolean writeDate(String name, Date value, Date defaultValue,
			StringBuilder builder) {
		return write(name, value != null ? value.getTime() : null,
				defaultValue != null ? defaultValue.getTime() : null, builder);
	}

	public boolean write(String name, Object value, Object defaultValue,
			StringBuilder builder) {
		boolean retval = false;

		if (verifyEmptyOrDefault(value, defaultValue)) {
			writeSeparator(builder);
			builder.append(quote).append(name).append(quote).append(colon);
			writeJsonEscaped(value != null ? value.toString() : "", builder);
			retval = true;
		}

		return retval;
	}

	public boolean writeMap(String name,
			Map<? extends Object, ? extends Object> value,
			Map<? extends Object, ? extends Object> defaultValue,
			StringBuilder builder) {
		boolean retval = false;

		if (verifyEmptyOrDefault(value, defaultValue,
				value != null && value.isEmpty())) {
			writeSeparator(builder);
			builder.append(quote).append(name).append(quote).append(colon)
					.append(openBracket);
			boolean first = true;
			for (Map.Entry<? extends Object, ? extends Object> e : value
					.entrySet()) {
				if (!first) {
					builder.append(comma);
				}
				builder.append(quote);
				writeJsonEscaped(e.getKey().toString(), builder);
				builder.append(quote).append(colon).append(quote);
				writeJsonEscaped(e.getValue().toString(), builder);
				builder.append(quote);
				first = false;
			}
			builder.append(closeBracket);
			retval = true;
		}

		return retval;
	}

	public boolean writeMapBigDecimal(String name,
			Map<? extends Object, ? extends Object> value,
			Map<? extends Object, ? extends Object> defaultValue,
			StringBuilder builder) {
		boolean retval = false;

		if (verifyEmptyOrDefault(value, defaultValue,
				value != null && value.isEmpty())) {
			writeSeparator(builder);
			builder.append(quote).append(name).append(quote).append(colon)
					.append(openBracket);
			boolean first = true;
			for (Map.Entry<? extends Object, ? extends Object> e : value
					.entrySet()) {
				if (!first) {
					builder.append(comma);
				}
				builder.append(quote);
				writeJsonEscaped(e.getKey().toString(), builder);
				builder.append(quote).append(colon);
				writeJsonEscaped(e.getValue().toString(), builder);
				first = false;
			}
			builder.append(closeBracket);
			retval = true;
		}

		return retval;
	}

	private void writeJsonEscaped(String value, StringBuilder builder) {
		if (value != null) {
			for (char c : value.toCharArray()) {
				switch (c) {
				case '"':
				case '\\':
				case '/':
				case '\f':
					builder.append(backslash);
				default:
					if (c >= 32 && c < 256) {
						builder.append(c);
					} else {
						builder.append("\\u").append(String.format("%04d", (int)c));
					}
				}
			}
		}
	}

	protected void writeSeparator(StringBuilder builder) {
		if (builder.length() > 1) {
			builder.append(comma);
		}
	}
	protected boolean verifyEmptyOrDefault(Object value, Object defaultValue) {
		return verifyEmptyOrDefault(value, defaultValue, false);
	}

	protected boolean verifyEmptyOrDefault(Object value, Object defaultValue,
			boolean additionalEmptyCheck) {
		boolean isEmpty = value == null || additionalEmptyCheck;
		boolean isDefault = value == defaultValue
				|| (value != null && defaultValue != null && value
						.equals(defaultValue));

		return (!isEmpty || writeEmpty) && (!isDefault || writeDefault);
	}

	public abstract void write(T o, StringBuilder sb);
}
