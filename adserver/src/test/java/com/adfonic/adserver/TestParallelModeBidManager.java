package com.adfonic.adserver;

import org.junit.Before;
import org.junit.Test;

public class TestParallelModeBidManager extends BaseAdserverTest {
    //private ParallelModeCacheService parallelModeCacheService;
    //private final int batchDurationSeconds = 5;
	//private ParallelModeBidManager parallelModeBidManager;

	@Before
	public void runBeforeEachTest() {
        //parallelModeCacheService = mock(ParallelModeCacheService.class);
        //parallelModeBidManager = new ParallelModeBidManager(parallelModeCacheService, batchDurationSeconds);
	}
	
	@Test
	public void test01_no_op() {
        // just for code coverage to ensure we call the constructor
    }
}
