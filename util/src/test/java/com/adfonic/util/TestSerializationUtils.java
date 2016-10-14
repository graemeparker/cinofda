package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

public class TestSerializationUtils {
    public static final class Impl implements java.io.Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String value;

        private Impl(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj == this) {
                return true;
            } else if (obj instanceof Impl) {
                return this.value.equals(((Impl) obj).value);
            } else {
                return false;
            }
        }
    }

    @Test
    public void testIsGzip() {
        assertTrue(SerializationUtils.isGzip(new File("blah.gz")));
        assertTrue(SerializationUtils.isGzip(new File("blah.dat.gz")));
        assertTrue(SerializationUtils.isGzip(new File("blah.tar.gz")));
        assertTrue(SerializationUtils.isGzip(new File("blah-20120304151859.tar.gz")));
        assertFalse(SerializationUtils.isGzip(new File("blah")));
        assertFalse(SerializationUtils.isGzip(new File("blah.dat")));
        assertFalse(SerializationUtils.isGzip(new File("blah.tar")));
        assertFalse(SerializationUtils.isGzip(new File("blah-20120304151859.tar")));
    }

    @Test
    public void testSerialize01_nonGzip_useMemory_enabled() throws java.io.IOException {
        File file = new File("TestSerializationUtils01-" + System.currentTimeMillis());
        file.delete();
        assertFalse(file.exists());
        try {
            SerializationUtils.serialize(new Impl("foo bar baz"), file, true);
            assertTrue(file.exists());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testSerialize02_nonGzip_useMemory_disabled() throws java.io.IOException {
        File file = File.createTempFile("TestSerializationUtils02-", ".dat");
        file.delete();
        assertFalse(file.exists());
        try {
            SerializationUtils.serialize(new Impl("foo bar baz"), file, false);
            assertTrue(file.exists());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testSerialize03_gzip_useMemory_enabled() throws java.io.IOException {
        File file = File.createTempFile("TestSerializationUtils03-", ".gz");
        file.delete();
        assertFalse(file.exists());
        try {
            SerializationUtils.serialize(new Impl("foo bar baz"), file, true);
            assertTrue(file.exists());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testSerialize04_gzip_useMemory_disabled() throws java.io.IOException {
        File file = File.createTempFile("TestSerializationUtils04-", ".gz");
        file.delete();
        assertFalse(file.exists());
        try {
            SerializationUtils.serialize(new Impl("foo bar baz"), file, false);
            assertTrue(file.exists());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testDeserialize05_useMemory_enabled() throws Exception {
        String value = "Hey there, this is a test.  Time=" + System.currentTimeMillis();
        Impl impl = new Impl(value);
        File file = File.createTempFile("TestSerializationUtils05-", ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            new ObjectOutputStream(fos).writeObject(impl);
            fos.close();

            assertEquals(impl, SerializationUtils.deserialize(Impl.class, file, true));
        } finally {
            file.delete();
        }
    }

    @Test
    public void testDeserialize06_useMemory_disabled() throws Exception {
        String value = "Hey there, this is a test.  Time=" + System.currentTimeMillis();
        Impl impl = new Impl(value);
        File file = File.createTempFile("TestSerializationUtils06-", ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            new ObjectOutputStream(fos).writeObject(impl);
            fos.close();

            assertEquals(impl, SerializationUtils.deserialize(Impl.class, file, false));
        } finally {
            file.delete();
        }
    }

    @Test
    public void testDeserialize07_gzip_useMemory_enabled() throws Exception {
        String value = "Hey there, this is a test.  Time=" + System.currentTimeMillis();
        Impl impl = new Impl(value);
        File file = File.createTempFile("TestSerializationUtils05-", ".dat.gz");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            GZIPOutputStream gzipOut = new GZIPOutputStream(fos);
            new ObjectOutputStream(gzipOut).writeObject(impl);
            gzipOut.flush();
            gzipOut.close();
            fos.close();

            assertEquals(impl, SerializationUtils.deserialize(Impl.class, file, true));
        } finally {
            file.delete();
        }
    }

    @Test
    public void testDeserialize08_gzip_useMemory_disabled() throws Exception {
        String value = "Hey there, this is a test.  Time=" + System.currentTimeMillis();
        Impl impl = new Impl(value);
        File file = File.createTempFile("TestSerializationUtils06-", ".dat.gz");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            GZIPOutputStream gzipOut = new GZIPOutputStream(fos);
            new ObjectOutputStream(gzipOut).writeObject(impl);
            gzipOut.flush();
            gzipOut.close();
            fos.close();

            assertEquals(impl, SerializationUtils.deserialize(Impl.class, file, false));
        } finally {
            file.delete();
        }
    }

    @Test(expected = java.io.FileNotFoundException.class)
    public void testDeserialize09_invalid_fileNotFound() throws Exception {
        File file = File.createTempFile("TestSerializationUtils07-", ".dat");
        file.delete(); // make sure it doesn't exist
        SerializationUtils.deserialize(Impl.class, file, false);
        fail("Shouldn't have gotten here");
    }

    @Test(expected = java.io.IOException.class)
    public void testDeserialize10_invalid_data() throws Exception {
        File file = File.createTempFile("TestSerializationUtils06-", ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            new PrintStream(fos).println("Hey, this will totally deserialize properly...NOT!!!");
            fos.close();

            SerializationUtils.deserialize(Impl.class, file, false);
        } finally {
            file.delete();
        }
    }
}
