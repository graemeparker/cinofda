package com.adfonic.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestAuditLog {
    @Test
    public void testGetObjectId() {
        // Passing anything other than an instance of HasPrimaryKeyId should fail
        String objectId;
        try {
            objectId = AuditLog.getObjectId(123);
            fail("getObjectId(primitive) should throw IllegalArgumentException");
        } catch (IllegalArgumentException good) {
            // sweet
        }

        try {
            objectId = AuditLog.getObjectId(new java.util.Date());
            fail("getObjectId(Date) should throw IllegalArgumentException");
        } catch (IllegalArgumentException good) {
            // sweet
        }

        Company company = new Company("name");
        // Yeah, it's not persistent, so id should equal 0...but that's still a valid test
        objectId = AuditLog.getObjectId(company);
        assertEquals(company.getId() + "[OID]com.adfonic.domain.Company", objectId);
    }
}
