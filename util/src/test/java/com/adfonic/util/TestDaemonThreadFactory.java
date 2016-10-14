package com.adfonic.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestDaemonThreadFactory {
    @Test
    public void testGetInstance() {
        assertNotNull(DaemonThreadFactory.getInstance());
    }

    @Test
    public void testNewThread() {
        Thread thread = DaemonThreadFactory.getInstance().newThread(new Runnable() {
            @Override
            public void run() {
            }
        });
        assertTrue(thread.isDaemon());
    }
}
