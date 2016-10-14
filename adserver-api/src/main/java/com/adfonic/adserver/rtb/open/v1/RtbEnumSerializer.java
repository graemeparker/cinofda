package com.adfonic.adserver.rtb.open.v1;


/**
 * 
 * @author mvanek
 *
 * SerializationConfig.Feature.WRITE_ENUMS_USING_INDEX works globally 
 * We want only few Enums to be serialized as ordinal
 */
public class RtbEnumSerializer/*<T extends Enum<T>> extends ScalarSerializerBase<T>*/{

    private static final long serialVersionUID = 1L;
    /*
        public RtbEnumSerializer(Class<T> enumClass) {
            super(enumClass);
        }

        @Override
        public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            if (value != null) {
                jgen.writeNumber(value.ordinal());
            }
        }
    */
}
