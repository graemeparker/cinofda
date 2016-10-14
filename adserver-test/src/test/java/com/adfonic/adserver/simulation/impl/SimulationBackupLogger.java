package com.adfonic.adserver.simulation.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.rtb.nativ.BidRequest;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.UnfilledReason;

public class SimulationBackupLogger implements BackupLogger {

	private List<Log> logs = new ArrayList<>();

	@Override
	public void startFilterRequest() {
	}

	@Override
	public void startControllerRequest() {
	}

	@Override
	public void endFilterRequest() {
	}

	@Override
	public void logAdServed(Impression impression, Date eventTime,
			TargetingContext context) {
		logs.add(new Log(AdAction.AD_SERVED, context, impression.toString()));
	}

	@Override
	public void logUnfilledRequest(UnfilledReason unfilledReason,
			Date eventTime, TargetingContext context) {
		logs.add(new Log(AdAction.UNFILLED_REQUEST, context,
				unfilledReason.toString()));
	}

	@Override
	public void logAdRequestFailure(String reason, TargetingContext context,
			String... extraValues) {
		logs.add(new Log(AdAction.BID_FAILED, context, reason));
	}

	@Override
	public void logRtbBidSuccess(Impression impression, BigDecimal price,
			Date eventTime, TargetingContext context) {
		logs.add(new Log(AdAction.AD_SERVED_AND_IMPRESSION, context, "RTB_BID_SUCCESS", impression.toString()));
	}

	@Override
	public void logRtbBidFailure(String reason, TargetingContext context, BidRequest req, String... extraValues) {
		logs.add(new Log(AdAction.BID_FAILED, context, "RTB_BID_FAILED", reason));
	}

	@Override
	public void logRtbLoss(Impression impression, Date eventTime,
			TargetingContext context) {
		logs.add(new Log(AdAction.BID_FAILED, context, "RTB_LOSS", impression.toString()));
	}

	@Override
	public void logRtbWinSuccess(Impression impression,
			BigDecimal settlementPrice, Date eventTime, TargetingContext context) {
		logs.add(new Log(AdAction.AD_SERVED_AND_IMPRESSION, context, "RTB_WIN", impression.toString()));
	}

	@Override
	public void logRtbWinFailure(String impressionExternalID, String reason,
			TargetingContext context, String... extraValues) {
		logs.add(new Log(AdAction.BID_FAILED, context, "RTB_WIN_FAILURE", reason, impressionExternalID));
	}

	@Override
	public void logBeaconSuccess(Impression impression, Date eventTime,
			TargetingContext context) {
		logs.add(new Log(AdAction.AD_SERVED_AND_IMPRESSION, context, "BEACON", impression.toString()));
	}

	@Override
	public void logBeaconFailure(String impressionExternalID, String reason,
			TargetingContext context, String... extraValues) {
		logs.add(new Log(AdAction.COMPLETED_VIEW, context, "BEACON_FAILED", impressionExternalID));
	}

	@Override
	public void logBeaconFailure(Impression impression, String reason,
			TargetingContext context, String... extraValues) {
		logs.add(new Log(AdAction.COMPLETED_VIEW, context, "BEACON_FAILED", impression.toString()));
	}

	@Override
	public void logClickSuccess(Impression impression, Date eventTime,
			TargetingContext context) {
		logs.add(new Log(AdAction.CLICK, context, "SUCCESS", impression.toString()));
	}

	@Override
	public void logClickFailure(String impressionExternalID, String reason,
			TargetingContext context, String... extraValues) {
		logs.add(new Log(AdAction.CLICK, context, "FAILED", impressionExternalID));
	}

	@Override
	public void logClickFailure(Impression impression, String reason,
			TargetingContext context, String... extraValues) {
		logs.add(new Log(AdAction.CLICK, context, "FAIKED", impression.toString()));
	}

	public class Log {
		AdAction adAction;
		TargetingContext context;
		String[] extraValues;

		public Log(AdAction adAction, TargetingContext context,
				String... extraValues) {
			super();
			this.adAction = adAction;
			this.context = context;
			this.extraValues = extraValues;
		}

		@Override
		public String toString() {
			return "Log [adAction=" + adAction + ", extraValues="
					+ Arrays.toString(extraValues) + "]";
		}

		public AdAction getAdAction() {
			return adAction;
		}

		public void setAdAction(AdAction adAction) {
			this.adAction = adAction;
		}

		public TargetingContext getContext() {
			return context;
		}

		public void setContext(TargetingContext context) {
			this.context = context;
		}

		public String[] getExtraValues() {
			return extraValues;
		}

		public void setExtraValues(String[] extraValues) {
			this.extraValues = extraValues;
		}
	}

	public List<Log> getLogs() {
		return logs;
	}

	public void setLogs(List<Log> logs) {
		this.logs = logs;
	}

    @Override
    public void logBidServed(Impression impression, Date eventTime, TargetingContext context, BidRequest bidRequest) {
    }
}
