package com.adfonic.retargeting.redis;

import java.io.IOException;
import java.util.Map;

import org.joda.time.Instant;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dmp.cache.DeviceDataCacheReader;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.newrelic.api.agent.Trace;

public class DeviceDataRedisReader extends AbstractRedis implements DeviceDataCacheReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDataRedisReader.class);

    private DeviceDataBinaryParser ddParser;
    private ThreadLocalClientFactory factory;

    final private ObjectMapper mapper = new ObjectMapper();
  
    public DeviceDataRedisReader(ThreadLocalClientFactory factory) {
        this(factory, new DeviceDataBinaryParser());
    }

    public DeviceDataRedisReader(ThreadLocalClientFactory factory, DeviceDataBinaryParser ddParser) {
        this.factory = factory;
        this.ddParser = ddParser;
        mapper.registerModule(new MyDateTimeModule());
    }

    public DeviceData getData(byte[] keyBytes) {

        // when redis restarts it time out with java.net.SocketTimeoutException, which is java.io.IOException
        try {
            Map<byte[], byte[]> bytes = factory.getJedis().hgetAll(keyBytes);
            if (bytes == null) {
                LOGGER.debug("no value for key: {}", keyBytes);
                return null;
            }
            return ddParser.parse(bytes);
        } catch (Exception re) {
            LOGGER.error("invalidating connection and rethrowing: ");
            factory.invalidateConnection(re);
            throw re;
        }
    }
    

    @Trace(dispatcher = true)
    @Override
    public DeviceData getData(String deviceIdKey) {
        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());

        return getData(keyBytes);
    }

    public String getDataAsJson(String deviceIdKey) throws JsonProcessingException {
        DeviceData dd = getData(deviceIdKey);
        String jsonOut = mapper.writeValueAsString(dd);
        
        return jsonOut;
    }
    
    static public class MyDateTimeModule extends SimpleModule {

        private static final long serialVersionUID = 1L;

        public MyDateTimeModule() {
            super();
            addSerializer(Instant.class, new MyDateTimeSerializer());
        }
    }
    
    static public class MyDateTimeSerializer extends StdScalarSerializer<Instant> {

        public MyDateTimeSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant instant,
                              JsonGenerator jsonGenerator,
                              SerializerProvider provider) throws IOException, JsonGenerationException {
            String dateTimeAsString = ISODateTimeFormat.dateTime().print(instant);
            jsonGenerator.writeString(dateTimeAsString);
        }
    }
    
}
