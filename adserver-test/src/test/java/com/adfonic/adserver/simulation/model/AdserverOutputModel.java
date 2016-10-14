package com.adfonic.adserver.simulation.model;


public class AdserverOutputModel {

	private VerificationModel<LogOutputModel> backupLogs;
	private VerificationModel<CounterOutputModel> counter;
	private VerificationModel<JmsOutputModel> jms;

	public VerificationModel<LogOutputModel> getBackupLogs() {
		return backupLogs;
	}

	public void setBackupLogs(VerificationModel<LogOutputModel> backupLogs) {
		this.backupLogs = backupLogs;
	}

	public VerificationModel<CounterOutputModel> getCounter() {
		return counter;
	}

	public void setCounter(VerificationModel<CounterOutputModel> counter) {
		this.counter = counter;
	}

	public VerificationModel<JmsOutputModel> getJms() {
		return jms;
	}

	public void setJms(VerificationModel<JmsOutputModel> jms) {
		this.jms = jms;
	}

}
