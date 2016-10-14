package com.adfonic.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.adfonic.util.PasswordUtils.PasswordAndSalt;

public class TestPasswordUtils {
    @Test
    public void test() {
        for (int k = 0; k < 1000; ++k) {
            String password = UUID.randomUUID().toString();
            PasswordAndSalt p = PasswordUtils.encodePassword(password);
            assertNotNull(p);
            assertTrue(PasswordUtils.checkPassword(password, p.getPassword(), p.getSalt()));

            // Invalid password
            assertFalse(PasswordUtils.checkPassword("invalid", p.getPassword(), p.getSalt()));
        }
    }
}
