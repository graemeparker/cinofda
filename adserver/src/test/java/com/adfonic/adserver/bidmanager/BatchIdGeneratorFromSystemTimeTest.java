package com.adfonic.adserver.bidmanager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BatchIdGeneratorFromSystemTimeTest {

    @Test
    public void shouldGenerateANewIdEverySecond() throws Exception {

        BatchIdGenerator batchIdGenerator = new BatchIdGeneratorFromSystemTime(1);

        waitForTheNewSecondToStart();
        long oldId = batchIdGenerator.getBatchId();
        waitOneSecond();
        long newId = batchIdGenerator.getBatchId();
        assertThat(newId, is(oldId + 1));

    }

    private void waitForTheNewSecondToStart() throws InterruptedException {
        Thread.sleep(1001 - System.currentTimeMillis()%1000);
    }

    private void waitOneSecond() throws InterruptedException {
        Thread.sleep(1000);
    }
}
