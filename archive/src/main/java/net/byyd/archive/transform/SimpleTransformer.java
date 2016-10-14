package net.byyd.archive.transform;

import java.io.IOException;

import net.byyd.archive.mapping.AnnotationNamePropertyNamingStrategy;

import org.codehaus.jackson.map.ObjectMapper;

public class SimpleTransformer<T> implements Transformer<T, String> {

	private ObjectMapper mapper;

	public SimpleTransformer() {
		mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(new AnnotationNamePropertyNamingStrategy());
	}

	@Override
	public String transform(T model) {
		try {
			return mapper.writeValueAsString(model);
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Unable to map object: " + model, e);
		}
	}
}
