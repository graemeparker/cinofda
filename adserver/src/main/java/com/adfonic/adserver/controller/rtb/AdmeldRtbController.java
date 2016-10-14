package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.domain.Medium;
import com.adfonic.geo.SimpleCoordinates;
import com.adfonic.util.stats.FreqLogr;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class AdmeldRtbController {

    private static final transient Logger LOG = Logger.getLogger(AdmeldRtbController.class.getName());

    private static final Pattern ADMELD_SIZE_PATTERN = Pattern.compile("(\\d+)x(\\d+)");

    private final RtbBidLogic rtbLogic;
    private final Level bidRequestLoggingLevel = Level.FINE;
    private final BackupLogger backupLogger;

    @Value("${Rtb.admeldbuyer.adfonic.id}")
    private String admeldBuyerId;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AdmeldRtbController(RtbBidLogic rtbLogic, BackupLogger backupLogger) {
        this.rtbLogic = rtbLogic;
        this.backupLogger = backupLogger;
    }

    @RequestMapping(value = "/rtb/adm/bid/{publisherExternalID}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String handleAdMeldBidRequest(@PathVariable("publisherExternalID") String publisherExternalID, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            @RequestParam(value = "admeld_user_id", required = false) String admeldUserId, @RequestParam(value = "external_user_id", required = false) String externalUserId,
            @RequestParam(value = "admeld_request_id", required = true) String admeldRequestId,
            @RequestParam(value = "admeld_publisher_id", required = true) Integer admeldPublisherId,
            @RequestParam(value = "admeld_website_id", required = true) Integer admeldWebsiteId, @RequestParam(value = "admeld_tag_id", required = false) Integer admeldTagId,
            @RequestParam(value = "ip_address", required = true) String ipAddress, @RequestParam(value = "user_agent", required = true) String userAgent,
            @RequestParam(value = "language", required = false) String language, @RequestParam(value = "time_zone", required = false) Integer timeZone,
            @RequestParam(value = "url", required = false) String url, @RequestParam(value = "refer_url", required = false) String referUrl,
            @RequestParam(value = "size", required = true) String size, @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "view_count", required = false) Integer viewCount, @RequestParam(value = "max_response_time", required = false) Integer maxResponseTime,
            @RequestParam(value = "latlong", required = false) String latlong, @RequestParam(value = "zip", required = false) String zip,
            @RequestParam(value = "dma", required = false) Integer dma, @RequestParam(value = "data_mobile_screen_size", required = false) String dataMobileScreenSize,
            @RequestParam(value = "data_mobile_carrier", required = false) String dataMobileCarrier,
            @RequestParam(value = "data_mobile_device_vendor", required = false) String dataMobileDeviceVendor,
            @RequestParam(value = "data_mobile_device_os", required = false) String dataMobileDeviceOs,
            @RequestParam(value = "data_mobile_device_osversion", required = false) String dataMobileDeviceOsversion,
            @RequestParam(value = "data_mobile_device_model", required = false) String dataMobileDeviceModel,
            @RequestParam(value = "data_mobile_device_features", required = false) String dataMobileDeviceFeatures) throws java.io.IOException, NoBidException {

        backupLogger.startControllerRequest();

        if (LOG.isLoggable(bidRequestLoggingLevel)) {
            //LOG.log(bidRequestLoggingLevel, request.getQueryString());
            LoggingUtils.log(LOG, bidRequestLoggingLevel, null, null, this.getClass(), "handleAdMeldBidRequest", httpRequest.getQueryString());
        }

        ByydRequest byydRequest = new ByydRequest(publisherExternalID, admeldRequestId);
        if (maxResponseTime != null) {
            byydRequest.setTmax(maxResponseTime.longValue());
        }

        // Using site intead of app
        byydRequest.setMedium(Medium.SITE);
        byydRequest.setPublicationUrlString(url);
        byydRequest.setPublicationRtbId(admeldPublisherId.toString() + "-" + admeldWebsiteId.toString());

        ByydDevice device = new ByydDevice();
        device.setIp(ipAddress);
        device.setUserAgent(userAgent);
        if (StringUtils.isNotEmpty(latlong)) {
            try {
                device.setCoordinates(new SimpleCoordinates(latlong));
            } catch (SimpleCoordinates.InvalidCoordinatesException e) {
                //LOG.warning(e.getMessage());
                LoggingUtils.log(LOG, bidRequestLoggingLevel, null, null, this.getClass(), "handleAdMeldBidRequest", e.getMessage());
            }
        }
        byydRequest.setDevice(device);

        ByydUser user = new ByydUser();
        // For the time being  
        user.setUid(admeldUserId);
        byydRequest.setUser(user);

        ByydImp imp = new ByydImp(admeldRequestId);

        Matcher sizeMatcher = ADMELD_SIZE_PATTERN.matcher(size);
        if (!sizeMatcher.matches()) {
            return getAdMeldErrorResponse("bad size format");
        }
        imp.setW(Integer.parseInt(sizeMatcher.group(1)));
        imp.setH(Integer.parseInt(sizeMatcher.group(2)));

        byydRequest.setImp(imp);

        byydRequest.doIncludeDestination(true);

        RtbHttpContext httpContext = new RtbHttpContext(RtbEndpoint.Admeld, publisherExternalID, httpRequest, httpResponse, "/rtb/win/");
        RtbExecutionContext<?, ?> rtbExecutionContext = new RtbExecutionContext(httpContext, false);
        rtbExecutionContext.setByydRequest(byydRequest);
        long now = System.currentTimeMillis();
        rtbExecutionContext.setRtbRequestParsedAt(now);
        rtbExecutionContext.setByydRequestMappedAt(now);
        // Generate the bid response
        ByydResponse nativeBidResponse = rtbLogic.bid(rtbExecutionContext, null, null);

        Map<String, Object> bid = new LinkedHashMap<String, Object>();
        ByydBid nativeBid = nativeBidResponse.getBid();
        bid.put("cpm", nativeBid.getPrice());
        bid.put("buyer", admeldBuyerId);// default adfonic value //TODO
        bid.put("request_id", admeldRequestId);
        bid.put("landing_page_url", nativeBid.getDestination());
        bid.put("creative", nativeBid.getAdm().replace("${AUCTION_PRICE}", "[admeld_win_price]"));

        Map<String, Object> bidResponse = new LinkedHashMap<String, Object>();
        bidResponse.put("bid", bid);
        return objectMapper.writeValueAsString(bidResponse);
    }

    @ExceptionHandler({ NoBidException.class })
    public void handleNoBidRequests(NoBidException e, HttpServletRequest request, Writer writer, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        writer.write(getAdMeldErrorResponse("no bid. nbr = " + e.getNoBidReason().ordinal()));
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, IllegalArgumentException.class, HttpMessageNotReadableException.class, JsonMappingException.class,
            Exception.class })
    public void handleBadAdMeldRequests(Exception x, HttpServletRequest request, Writer writer, HttpServletResponse response) throws IOException {
        //LoggingUtils.logUnexpectedError(LOG, e, null);
        FreqLogr.report(x);
        response.setContentType("application/json");
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        writer.write(getAdMeldErrorResponse("Invalid Request Format"));
    }

    private String getAdMeldErrorResponse(String message) {
        return String.format("{\"bid\":{\"cpm\": 0, \"buyer\":\"%s\", \"error\":\"%s\"}}", admeldBuyerId, message);
    }

}
