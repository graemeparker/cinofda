package com.adfonic.adserver;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import com.adfonic.util.Range;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.BigDecimalSerializer;
import com.esotericsoftware.kryo.serialize.EnumSerializer;
import com.esotericsoftware.kryo.serialize.IntSerializer;
import com.esotericsoftware.kryo.serialize.LongSerializer;
import com.esotericsoftware.kryo.serialize.MapSerializer;
import com.esotericsoftware.kryo.serialize.StringSerializer;

/**
 * Kryo serialization utilities
 */
public final class KryoUtils {
    private static final BigDecimalSerializer BIG_DECIMAL_SERIALIZER = new BigDecimalSerializer();
    
    private KryoUtils() {}
    
    public static BigDecimal getBigDecimal(ByteBuffer buffer) {
        return BIG_DECIMAL_SERIALIZER.readObjectData(buffer, BigDecimal.class);
    }

    public static void putBigDecimal(ByteBuffer buffer, BigDecimal value) {
        BIG_DECIMAL_SERIALIZER.writeObjectData(buffer, value);
    }

    public static BigDecimal getNullableBigDecimal(ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return null;
        } else {
            return BIG_DECIMAL_SERIALIZER.readObjectData(buffer, BigDecimal.class);
        }
    }

    public static void putNullableBigDecimal(ByteBuffer buffer, BigDecimal value) {
        buffer.put(value == null ? (byte)0 : (byte)1);
        if (value != null) {
            BIG_DECIMAL_SERIALIZER.writeObjectData(buffer, value);
        }
    }

    public static boolean getBoolean(ByteBuffer buffer) {
        return buffer.get() == 1;
    }

    public static void putBoolean(ByteBuffer buffer, boolean value) {
        buffer.put(value ? (byte)1 : (byte)0);
    }

    public static byte[] getByteArray(ByteBuffer buffer) {
        int length = getInt(buffer);
        byte[] array = new byte[length];
        buffer.get(array);
        return array;
    }
    
    public static void putByteArray(ByteBuffer buffer, byte[] array) {
        putInt(buffer, array.length);
        buffer.put(array);
    }

    public static Date getDate(ByteBuffer buffer) {
        return new Date(LongSerializer.get(buffer, true));
    }

    public static void putDate(ByteBuffer buffer, Date value) {
        LongSerializer.put(buffer, value.getTime(), true);
    }

    public static Date getNullableDate(ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return null;
        } else {
            return new Date(LongSerializer.get(buffer, true));
        }
    }

    public static void putNullableDate(ByteBuffer buffer, Date value) {
        buffer.put(value == null ? (byte)0 : (byte)1);
        if (value != null) {
            LongSerializer.put(buffer, value.getTime(), true);
        }
    }
    
    public static Double getDouble(ByteBuffer buffer) {
        return buffer.getDouble();
    }
    
    public static void putDouble(ByteBuffer buffer, Double value) {
        buffer.putDouble(value);
    }

    public static Double getNullableDouble(ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return null;
        } else {
            return buffer.getDouble();
        }
    }
    
    public static void putNullableDouble(ByteBuffer buffer, Double value) {
        buffer.put(value == null ? (byte)0 : (byte)1);
        if (value != null) {
            buffer.putDouble(value);
        }
    }

    public static <T extends Enum> T getEnum(ByteBuffer buffer, Class<T> clazz) {
        return EnumSerializer.get(buffer, clazz);
    }

    public static <T extends Enum> void putEnum(ByteBuffer buffer, T value) {
        EnumSerializer.put(buffer, value);
    }

    public static <T extends Enum> T getNullableEnum(ByteBuffer buffer, Class<T> clazz) {
        if (buffer.get() == 0) {
            return null;
        } else {
            return EnumSerializer.get(buffer, clazz);
        }
    }

    public static <T extends Enum> void putNullableEnum(ByteBuffer buffer, T value) {
        buffer.put(value == null ? (byte)0 : (byte)1);
        if (value != null) {
            EnumSerializer.put(buffer, value);
        }
    }

    public static Range<Integer> getIntRange(ByteBuffer buffer) {
        return new Range<Integer>(IntSerializer.get(buffer, true), IntSerializer.get(buffer, true), buffer.get() == 1);
    }

    public static void putIntRange(ByteBuffer buffer, Range<Integer> value) {
        IntSerializer.put(buffer, value.getStart(), true);
        IntSerializer.put(buffer, value.getEnd(), true);
        buffer.put(value.isIntegral() ? (byte)1 : (byte)0);
    }
    
    public static Range<Integer> getNullableIntRange(ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return null;
        } else {
            return new Range<Integer>(IntSerializer.get(buffer, true), IntSerializer.get(buffer, true), buffer.get() == 1);
        }
    }

    public static void putNullableIntRange(ByteBuffer buffer, Range<Integer> value) {
        buffer.put(value == null ? (byte)0 : (byte)1);
        if (value != null) {
            IntSerializer.put(buffer, value.getStart(), true);
            IntSerializer.put(buffer, value.getEnd(), true);
            buffer.put(value.isIntegral() ? (byte)1 : (byte)0);
        }
    }

    public static int getInt(ByteBuffer buffer) {
        return IntSerializer.get(buffer, true);
    }

    public static void putInt(ByteBuffer buffer, int value) {
        IntSerializer.put(buffer, value, true);
    }
    
    public static long getLong(ByteBuffer buffer) {
        return LongSerializer.get(buffer, true);
    }

    public static void putLong(ByteBuffer buffer, long value) {
        LongSerializer.put(buffer, value, true);
    }
    
    public static Long getNullableLong(ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return null;
        } else {
            return LongSerializer.get(buffer, true);
        }
    }

    public static void putNullableLong(ByteBuffer buffer, Long value) {
        buffer.put(value == null ? (byte)0 : (byte)1);
        if (value != null) {
            LongSerializer.put(buffer, value, true);
        }
    }
    
    public static String getString(ByteBuffer buffer) {
        return StringSerializer.get(buffer);
    }

    public static void putString(ByteBuffer buffer, String value) {
        StringSerializer.put(buffer, value);
    }
    
    public static String getNullableString(ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return null;
        } else {
            return StringSerializer.get(buffer);
        }
    }

    public static void putNullableString(ByteBuffer buffer, String value) {
        buffer.put(value == null ? (byte)0 : (byte)1);
        if (value != null) {
            StringSerializer.put(buffer, value);
        }
    }

    public static <T extends Map,K,V> T getMap(Kryo kryo, ByteBuffer buffer, Class<T> mapClass, Class<K> keyClass, Class<V> valueClass) {
        MapSerializer mapSerializer = new MapSerializer(kryo);
        mapSerializer.setKeysCanBeNull(false);
        mapSerializer.setKeyClass(keyClass);
        mapSerializer.setValuesCanBeNull(false);
        mapSerializer.setValueClass(valueClass);
        return mapSerializer.readObjectData(buffer, mapClass);
    }

    public static <K,V> void putMap(Kryo kryo, ByteBuffer buffer, Map<K,V> map, Class<K> keyClass, Class<V> valueClass) {
        MapSerializer mapSerializer = new MapSerializer(kryo);
        mapSerializer.setKeysCanBeNull(false);
        mapSerializer.setKeyClass(keyClass);
        mapSerializer.setValuesCanBeNull(false);
        mapSerializer.setValueClass(valueClass);
        mapSerializer.writeObjectData(buffer, map);
    }
}
