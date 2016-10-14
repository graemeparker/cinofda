package net.byyd.archive.model.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import net.byyd.archive.transform.EventFeed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.domain.DeviceIdentifierType;

public class BackupLogReader {
	private static final Logger LOG = LoggerFactory
			.getLogger(BackupLogReader.class);

	private static final String[] DEVICE_ID_IDENTIFIER = new String[] { "",
			DeviceIdentifierType.SYSTEM_NAME_DPID,
			DeviceIdentifierType.SYSTEM_NAME_ODIN_1,
			DeviceIdentifierType.SYSTEM_NAME_OPENUDID,
			DeviceIdentifierType.SYSTEM_NAME_ANDROID,
			DeviceIdentifierType.SYSTEM_NAME_UDID,
			DeviceIdentifierType.SYSTEM_NAME_IFA,
			DeviceIdentifierType.SYSTEM_NAME_HIFA,
			DeviceIdentifierType.SYSTEM_NAME_ATID,
			DeviceIdentifierType.SYSTEM_NAME_ADID,
			DeviceIdentifierType.SYSTEM_NAME_ADID_MD5,
			DeviceIdentifierType.SYSTEM_NAME_GOUID,
			DeviceIdentifierType.SYSTEM_NAME_IDFA,
			DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5, };

	private static int IDX_TIMESTAMP = 0;
	private static int IDX_REQUEST_TIME = 1;
	private static int IDX_CONTROLLER_TIME = 2;
	private static int IDX_ACTION = 3;
	private static int IDX_REASON = 4;
	private static int IDX_REQUEST_URI = 5;
	private static int IDX_REMOTE_ADDRESS = 6;
	private static int IDX_DEVICE_IP = 7;
	// private static int IDX_DERIVED_IP = 8;
	private static int IDX_TEST_MODE = 8;
	private static int IDX_ADSPACE_ID = 9;
	private static int IDX_MODEL_ID = 10;
	private static int IDX_COUNTRY_ID = 11;
	private static int IDX_EFF_USER_AGENT = 12;
	private static int IDX_DEVICE_ID = 13;
	private static int IDX_CLICK_ID_COOKIE = 14;
	private static int IDX_REFERER = 15;
	private static int IDX_CREATIVE_ID = 16;
	private static int IDX_DISPLAY_TYPE_ID = 17;
	private static int IDX_IMPRESSION_EXTERNAL_ID = 18;

	private String dateFormat = "yyyyMMddHHmmss";

	public void feed(InputStream is, EventFeed<AdEvent> feed)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		DateFormat df = new SimpleDateFormat(dateFormat);

		String line;
		while ((line = br.readLine()) != null) {
			String[] sp = line.split("\t");

			if (sp.length > 6) {
				AdEvent ae = new AdEvent();
				try {
					ae.setAdAction(convertAction(sp[IDX_ACTION]));
					// if (ae.getAdAction() != AdAction.AD_SERVED) {
					// continue;
					// }
					ae.setEventTime(df.parse(sp[IDX_TIMESTAMP]));
					// if (ae.getAdAction() == AdAction.IMPRESSION_FAILED
					// || ae.getAdAction() == AdAction.CLICK
					// || ae.getAdAction() == AdAction.CLICK_FAILED
					// || ae.getAdAction() == AdAction.UNFILLED_REQUEST
					// || ae.getAdAction() == AdAction.RTB_WIN_FAILED) {
					// continue;
					// }
					ae.setResponseOverall(convLong(sp[IDX_REQUEST_TIME]));
					ae.setResponseController(convLong(sp[IDX_CONTROLLER_TIME]));
					ae.setAdSpaceId(convLong(sp[IDX_ADSPACE_ID]) == null ? 0
							: convLong(sp[IDX_ADSPACE_ID]));
					// ae.setCampaignId(convLong(sp[IDX_CAMPAIGN_ID]));
					ae.setCountryId(convLong(sp[IDX_COUNTRY_ID]));
					ae.setModelId(convLong(sp[IDX_MODEL_ID]));
					ae.setUserAgentHeader(conv(sp[IDX_EFF_USER_AGENT]));
					if (sp.length > IDX_DEVICE_ID) {
						ae.setDeviceIdentifiers(readMap(sp[IDX_DEVICE_ID]));
					}
					ae.setIpAddress(conv(sp[IDX_DEVICE_IP]));
					ae.setRequestHost(conv(sp[IDX_REMOTE_ADDRESS]));
					ae.setRequestURL(conv(sp[IDX_REQUEST_URI]));
					switch (ae.getAdAction()) {
					case UNFILLED_REQUEST:
						ae.setUnfilledReason(convReason(sp[IDX_REASON]));
						if (ae.getUnfilledReason() == null) {
							ae.setDetailReason(conv(sp[IDX_REASON]));
						}
						break;
					case BID_FAILED:
						if (sp.length > IDX_CREATIVE_ID) {
							ae.setImpressionExternalID(conv(sp[IDX_CREATIVE_ID]));
						}
						ae.setDetailReason(conv(sp[IDX_REASON]));
						break;
					case CLICK:
					case IMPRESSION_FAILED:
					case CLICK_FAILED:
					case RTB_WIN_FAILED:
						ae.setImpressionExternalID(conv(sp[IDX_CREATIVE_ID]));
						if (sp.length > IDX_CREATIVE_ID + 1) {
							ae.setCreativeId(convLong(sp[IDX_CREATIVE_ID + 1]));
						}
						ae.setDetailReason(conv(sp[IDX_REASON]));
						break;
//					case RTB_WON:
//						ae.setCreativeId(convLong(sp[IDX_CREATIVE_ID]));
//						ae.setImpressionExternalID(conv(sp[IDX_CREATIVE_ID + 1]));
//						ae.setRtbSettlementPrice(convDec(sp[IDX_CREATIVE_ID + 2]));
//						break;
					case RTB_LOST:
						ae.setCreativeId(convLong(sp[IDX_CREATIVE_ID]));
						ae.setIntegrationTypeId(convLong(sp[IDX_DISPLAY_TYPE_ID]));
						ae.setImpressionExternalID(conv(sp[IDX_IMPRESSION_EXTERNAL_ID]));
						ae.setRtbSettlementPrice(convDec(sp[IDX_CREATIVE_ID + 3]));
						break;
					case IMPRESSION:
						ae.setCreativeId(convLong(sp[IDX_CREATIVE_ID]));
						ae.setImpressionExternalID(conv(sp[IDX_CREATIVE_ID + 1]));
						break;
					case RTB_FAILED:
						ae.setAdditionalMessage(conv(sp[IDX_CREATIVE_ID]));
						ae.setDetailReason(conv(sp[IDX_REASON]));
						break;
					default:
						if (sp.length > IDX_CREATIVE_ID) {
							ae.setCreativeId(convLong(sp[IDX_CREATIVE_ID]));
						}
						// ae.setOperatorId(convLong(sp[IDX_OPERATOR]));
						if (sp.length > IDX_DISPLAY_TYPE_ID) {
							ae.setIntegrationTypeId(convLong(sp[IDX_DISPLAY_TYPE_ID]));
						}
						if (sp.length > IDX_IMPRESSION_EXTERNAL_ID) {
							ae.setImpressionExternalID(conv(sp[IDX_IMPRESSION_EXTERNAL_ID]));
						}
					}
					feed.onEvent(ae);
				} catch (RuntimeException | ParseException e) {
					LOG.warn("Unable to read adevent log:" + e.getMessage()
							+ " / " + line);
				}
			}
		}
		feed.finish();
	}

	private BigDecimal convDec(String dec) {
		return dec != null && !dec.isEmpty() ? new BigDecimal(dec.trim())
				: null;
	}

	private UnfilledReason convReason(String detailReason) {
		try {
			return UnfilledReason.valueOf(detailReason);
		} catch (Exception e) {
		}
		return null;
	}

	private String conv(String string) {
		return string != null && !string.isEmpty() ? string.trim() : null;
	}

	private Map<Long, String> readMap(String deviceIDs) {
		Map<Long, String> retval = null;

		if (deviceIDs != null && !deviceIDs.isEmpty()) {
			retval = new TreeMap<>();
			String[] devIds = deviceIDs.split(";");
			for (String d : devIds) {
				String[] part = d.split("=");
				if (part.length == 2) {
					retval.put(translateDevId(part[0]), part[1]);
				}
			}
		}

		return retval;
	}

	private Long translateDevId(String devid) {
		long n = 0;

		for (; n < DEVICE_ID_IDENTIFIER.length; n++) {
			if (DEVICE_ID_IDENTIFIER[(int) n].equals(devid)) {
				break;
			}
		}

		return n == DEVICE_ID_IDENTIFIER.length ? -1 : n;
	}

	private Long convLong(String s) {
		return s != null && !s.isEmpty() ? Long.parseLong(s) : null;
	}

	private AdAction convertAction(String action) {
		AdAction r = null;
		if ("AD".equals(action)) {
			r = AdAction.AD_SERVED;
		} else if ("AD_FAILED".equals(action)) {
			r = AdAction.BID_FAILED;
		} else if ("BEACON".equals(action)) {
			r = AdAction.IMPRESSION;
		} else if ("BEACON_FAILED".equals(action)) {
			r = AdAction.IMPRESSION_FAILED;
		} else if ("CLICK".equals(action)) {
			r = AdAction.CLICK;
		} else if ("CLICK_FAILED".equals(action)) {
			r = AdAction.CLICK_FAILED;
		} else if ("RTB_FAILED".equals(action)) {
			r = AdAction.RTB_FAILED;
		} else if ("UNFILLED".equals(action)) {
			r = AdAction.UNFILLED_REQUEST;
		} else if ("RTB_LOST".equals(action)) {
			r = AdAction.RTB_LOST;
		} else if ("RTB_WIN_FAILED".equals(action)) {
			r = AdAction.RTB_WIN_FAILED;
//		} else if ("RTB_WON".equals(action)) {
//			r = AdAction.RTB_WON;
		}
		return r;
	}

}
