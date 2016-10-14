package com.byyd.celtra;

import java.io.IOException;
import java.util.List;

import com.byyd.celtra.CeltraAnalyticsRequest.Filter;
import com.byyd.celtra.CeltraAnalyticsRequest.Operator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 
 * @author mvanek
 *
 */
public class FilterSerializer extends JsonSerializer<Filter> {

    @Override
    public void serialize(Filter value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("field", String.valueOf(value.getField()));
        // operator can be null
        Operator operator = value.getOperator();
        if (operator != null) {
            gen.writeStringField("operator", String.valueOf(value.getOperator()));
        }
        List<String> operands = value.getOperand();
        if (operands.size() == 1) {
            gen.writeStringField("operand", operands.get(0));
        } else {
            gen.writeArrayFieldStart("operand");
            for (String item : operands) {
                gen.writeString(item);
            }
            gen.writeEndArray();
        }

        gen.writeEndObject();

    }
}
