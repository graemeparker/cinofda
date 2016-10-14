package net.byyd.archive.mapping;

import java.lang.reflect.Field;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.PropertyNamingStrategy.PropertyNamingStrategyBase;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnnotationNamePropertyNamingStrategy extends
		PropertyNamingStrategyBase {

	@Override
	public String nameForGetterMethod(MapperConfig<?> config,
			AnnotatedMethod method, String defaultName) {
		String ret = super.nameForGetterMethod(config, method, defaultName);
		ret = translateWithField(method.getDeclaringClass(), ret);

		return ret;
	}

	@Override
	public String nameForSetterMethod(MapperConfig<?> config,
			AnnotatedMethod method, String defaultName) {
		String ret = super.nameForSetterMethod(config, method, defaultName);
		ret = translateWithField(method.getDeclaringClass(), ret);

		return ret;
	}

	private String translateWithField(Class<?> clazz, String name) {
		String retName = name;
		try {
			Field field = clazz.getDeclaredField(name);
			JsonProperty jsonProp = field.getAnnotation(JsonProperty.class);

			if (jsonProp != null) {
				retName = jsonProp.value();
			}
		} catch (NoSuchFieldException | SecurityException e) {
		}

		return retName;
	}

	@Override
	public String nameForField(MapperConfig<?> config, AnnotatedField field,
			String defaultName) {
		String name = super.nameForField(config, field, defaultName);
		JsonProperty jsonProp = field.getAnnotation(JsonProperty.class);

		if (jsonProp != null) {
			name = jsonProp.value();
		}

		return name;
	}

	@Override
	public String translate(String propertyName) {
		return propertyName;
	}
}
