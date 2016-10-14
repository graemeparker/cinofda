package com.adfonic.tasks.combined.truste.dao;

import java.io.IOException;

import org.joda.time.DateTime;

public interface TasksDao {
	DateTime loadLastRunTime() throws IOException;
	void storeLastRunTime(DateTime lastRunTime) throws IOException;
}
