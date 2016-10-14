package com.adfonic.adserver.simulation;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.type.JavaType;

public class SimulationDeserializerProvider extends StdDeserializerProvider {
	@Override
	protected KeyDeserializer _handleUnknownKeyDeserializer(JavaType type)
			throws JsonMappingException {
		return new KeyDeserializer() {
			@Override
			public Object deserializeKey(String key, DeserializationContext ctxt)
					throws IOException, JsonProcessingException {
				return null;
			}
		};
	}
}
