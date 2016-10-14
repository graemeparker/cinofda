package com.adfonic.adserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;

/**
 * Kryo management logic.  This object provides centralized, simplified
 * interfaces for reading and writing objects in Kryo serialized form.
 * This object is intended to be used as a singleton bean.
 */
public class KryoManager {
    private static final transient Logger logger = Logger.getLogger(KryoManager.class.getName());
    
    private final Kryo kryo;

    public KryoManager() {
        logger.info("Setting up Kryo");
        kryo = new Kryo();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Registering classes");
        }

        // From http://code.google.com/p/kryo/
        //
        // "When a class is registered, it is assigned an ordinal number. This integer is
        // used in the serialized bytes to identify what class to instantiate when the
        // object is deserialized. By using an integer, a class can be represented very
        // efficiently, usually with just a byte. The downside is that the ordinals must
        // be identical when the class is deserialized. To do this, the exact same classes
        // must be registered in the exact same order when the object is deserialized."
        
        // DO NOT CHANGE THE ORDER IN WHICH CLASSES ARE REGISTERED.
        // Or if you do, you'd better ensure that both read & write ends are
        // using the same version of this class.  Since we use this utility
        // on JMS producers and consumers, a mismatch can be a very bad thing.
        // It's no different than having a new serialized version of a class.
        // We're paying this potential penalty in exchange for mad efficiency.

        kryo.register(com.adfonic.adserver.AdEvent.class);
        kryo.register(com.adfonic.adserver.Impression.class);
        kryo.register(com.adfonic.adserver.Click.class);
        kryo.register(java.util.LinkedHashMap.class);
    }

    /**
     * Read an object from a Kryo object graph representation
     */
    public <T> T readObject(byte[] objectBytes, Class<T> clazz) {
        return new ObjectBuffer(kryo).readObject(objectBytes, clazz);
    }
    
    /**
     * Read an object from a Kryo object graph representation
     */
    public <T> T readObject(InputStream inputStream, Class<T> clazz) {
        return new ObjectBuffer(kryo).readObject(inputStream, clazz);
    }
    
    /**
     * Write an object to a Kryo object graph representation
     */
    public byte[] writeObject(Object obj) {
        return new ObjectBuffer(kryo).writeObject(obj);
    }
    
    /**
     * Write an object to a Kryo object graph representation
     */
    public void writeObject(Object obj, OutputStream outputStream) {
        new ObjectBuffer(kryo).writeObject(outputStream, obj);
    }
}
