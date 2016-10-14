package com.adfonic.tools.beans.campaign.history;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;
import com.adfonic.dto.audience.CampaignAudienceDto;
import com.adfonic.dto.auditlog.AuditLogDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.model.ValueTransformer;
import com.adfonic.presentation.util.DateUtils;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.security.SecurityUtils;

/**
 * Value transformer implementation for transform the values for the userName,
 * userEmail, fieldName, and for the new and old values.
 */
public class CampaignHistoryValueTransformer implements ValueTransformer {
    public static final String COLUMN_KEY_TIMESTAMP = "Timestamp";
    public static final String COLUMN_KEY_USER_NAME = "AuditLogDtoForUserName";
    public static final String COLUMN_KEY_USER_EMAIL = "AuditLogDtoForUserEmail";
    public static final String COLUMN_KEY_NAME = "AuditLogDtoForFieldName";
    public static final String COLUMN_KEY_TOOLTIP = "AuditLogDtoForFieldToolTip";
    public static final String COLUMN_KEY_OLD_VALUE = "AuditLogDtoForOldValue";
    public static final String COLUMN_KEY_NEW_VALUE = "AuditLogDtoForNewValue";

    private static final String DOTS = " ...";
    private static final String LIST_SEP = ",";
    private static final String DOT_SEP = ".";
    private static final String EXTRA_INFO_SEP = ": ";
    private static final int MAX_BLOB_STRING_SIZE = 50;
    private static final String MSG_KEY_PREFIX = "page.campaign.history.table.entry.";
    private static final String MSG_TOOLTIP_SUFFIX = "tooltip";
    private static final SimpleDateFormat sdfTimezoneValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
    private static final SimpleDateFormat sdfTimeColumn = new SimpleDateFormat(DateUtils.getTimeStampFormat());
    private static final Object AUDITLOG_ENTITY_NAME_CREATIVE = "Creative";
    private static final Object AUDITLOG_ENTITY_NAME_CAMPAIGN_AUDIENCE = "CampaignAudience";

    private Boolean loggedAsAdmin;
    private Boolean trimBlobs;
    private TimeZone userTimezone;
    private Boolean replaceHtmlCode;
    private List<CreativeDto> creatives;
    private Map<Long, String> creativesMap;
    private List<CampaignAudienceDto> audiences;
    private Map<Long, String> audiencesMap;

    public CampaignHistoryValueTransformer(Boolean trimBlobs, TimeZone userTimezone, Boolean replaceHtmlCode, List<CreativeDto> creatives,
            List<CampaignAudienceDto> audiences) {
        List<String> roles = new ArrayList<String>(0);
        roles.add(Constants.LOGGED_IN_AS_ADMIN_ROLE);
        this.loggedAsAdmin = SecurityUtils.hasUserRoles(roles);
        this.trimBlobs = trimBlobs;
        this.userTimezone = userTimezone;
        this.replaceHtmlCode = replaceHtmlCode;
        this.creatives = creatives;
        this.audiences = audiences;
    }

    @Override
    public Object transform(String fieldName, Object value) {
        if (value == null) {
            value = "-";
        } else {
            String shortEntityName = null;

            switch (fieldName) {
            case COLUMN_KEY_TIMESTAMP:
                value = sdfTimeColumn.format(DateUtils.getTimezoneDate((Date) value, userTimezone));
                break;

            case COLUMN_KEY_USER_NAME:
                if (loggedAsAdmin) {
                    value = WordUtils.capitalize(((AuditLogDto) value).getUserName());
                } else {
                    if (((AuditLogDto) value).isAdmin()) {
                        value = FacesUtils.getBundleMessage("page.campaign.history.table.value.admin");
                    } else {
                        value = WordUtils.capitalize(((AuditLogDto) value).getUserName());
                    }
                }
                break;

            case COLUMN_KEY_USER_EMAIL:
                if (loggedAsAdmin) {
                    value = ((AuditLogDto) value).getUserEmail();
                } else {
                    if (((AuditLogDto) value).isAdmin()) {
                        value = "";
                    } else {
                        value = ((AuditLogDto) value).getUserEmail();
                    }
                }
                break;

            case COLUMN_KEY_NAME:
                shortEntityName = getShortEntityName((AuditLogDto) value);
                String columnKeyI18Msg = getI18nMessage(getI18nEntityFieldKey((AuditLogDto) value, shortEntityName));
                String columnKeyExtraInfo = "";
                if (AUDITLOG_ENTITY_NAME_CREATIVE.equals(shortEntityName)) {
                    columnKeyExtraInfo = EXTRA_INFO_SEP + getCreativeName((AuditLogDto) value);
                } else if (AUDITLOG_ENTITY_NAME_CAMPAIGN_AUDIENCE.equals(shortEntityName)) {
                    columnKeyExtraInfo = EXTRA_INFO_SEP + getAudienceName((AuditLogDto) value);
                }

                value = columnKeyI18Msg + columnKeyExtraInfo;
                break;
            case COLUMN_KEY_TOOLTIP:
                shortEntityName = getShortEntityName((AuditLogDto) value);
                value = getI18nMessage(getI18nEntityFieldTooltip((AuditLogDto) value, shortEntityName));
                if ((replaceHtmlCode && StringUtils.isNotEmpty((String) value))) {
                    value = ((String) value).replaceAll("\\<[^>]*>", "");
                }
                break;
            case COLUMN_KEY_OLD_VALUE:
                value = transformedEntryValue(((AuditLogDto) value).getOldValue(), ((AuditLogDto) value).getAuditLogEntryType());
                break;

            case COLUMN_KEY_NEW_VALUE:
                value = transformedEntryValue(((AuditLogDto) value).getNewValue(), ((AuditLogDto) value).getAuditLogEntryType());
                break;

            default:
                break;
            }
        }
        return value;
    }

    private String getCreativeName(AuditLogDto value) {
        Long entityId = value.getEntityId();

        if (creativesMap == null) {
            creativesMap = new HashMap<Long, String>(creatives.size());
        }

        String creativeName = creativesMap.get(entityId);
        if (creativeName == null) {
            for (CreativeDto creative : creatives) {
                if (creative.getId().longValue() == entityId.longValue()) {
                    creativeName = creative.getName();
                    creativesMap.put(entityId, creativeName);
                }
            }
        }

        return creativeName;
    }

    private String getAudienceName(AuditLogDto value) {
        Long entityId = value.getEntityId();

        if (audiencesMap == null) {
            audiencesMap = new HashMap<Long, String>(audiences.size());
        }

        String audienceName = audiencesMap.get(entityId);
        if (audienceName == null) {
            for (CampaignAudienceDto audience : audiences) {
                if (audience.getId().longValue() == entityId.longValue()) {
                    audienceName = audience.getAudience().getName();
                    audiencesMap.put(entityId, audienceName);
                }
            }
        }

        return audienceName;
    }

    private String getI18nEntityFieldKey(AuditLogDto value, String shortEntityName) {
        return MSG_KEY_PREFIX + shortEntityName + DOT_SEP + value.getName();
    }

    private String getI18nEntityFieldTooltip(AuditLogDto value, String shortEntityName) {
        return getI18nEntityFieldKey(value, shortEntityName) + DOT_SEP + MSG_TOOLTIP_SUFFIX;
    }

    private String getShortEntityName(AuditLogDto value) {
        return value.getEntityName().substring(value.getEntityName().lastIndexOf(DOT_SEP) + 1);
    }

    private String transformedEntryValue(String entryValue, AuditLogEntryType entryType) {
        if (StringUtils.isEmpty(entryValue)) {
            entryValue = "-";
        } else {
            entryValue = entryValue.trim();
            switch (entryType) {
            case BOOLEAN:
                entryValue = (Boolean.valueOf(entryValue)) ? "ON" : "OFF";
                break;

            case DECIMAL:
                entryValue = new DecimalFormat("0.00##").format(new BigDecimal(entryValue));
                break;

            case BLOB:
                entryValue = entryValue.substring(1, entryValue.length() - 1); // remove
                                                                               // trailing
                                                                               // [
                                                                               // ]
                                                                               // parenthesis
                entryValue = getFirstNListElement(entryValue);
                break;

            case DATETIME:
                Date date = getDateFromTimezonString(entryValue);
                entryValue = sdfTimezoneValue.format(DateUtils.getTimezoneDate(date, userTimezone));
                break;

            default:
                break;
            }
        }
        return entryValue;
    }

    private String getFirstNListElement(String entry) {
        StringBuilder sb = new StringBuilder();

        String[] listItems = entry.split(LIST_SEP);
        String trimmedItem = null;
        for (String item : listItems) {
            trimmedItem = item.trim();
            if ((this.trimBlobs) && (sb.length() + trimmedItem.length() > MAX_BLOB_STRING_SIZE)) {
                sb.append(LIST_SEP).append(DOTS);
                break;
            } else {
                if (sb.length() != 0) {
                    sb.append(LIST_SEP).append(" ");
                }
                sb.append(trimmedItem);
            }
        }

        return sb.toString();
    }

    private Date getDateFromTimezonString(String timezone) {
        Date date = null;
        try {
            date = sdfTimezoneValue.parse(timezone);
        } catch (ParseException e) {
        }
        return date;
    }

    private String getI18nMessage(String i18nKey) {
        String i18Value = "";
        try {
            i18Value = FacesUtils.getBundleMessage(i18nKey);
        } catch (Exception e) {
            // Do nothing
        }
        return i18Value;
    }
}
