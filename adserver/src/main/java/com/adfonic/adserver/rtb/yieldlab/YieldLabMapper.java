package com.adfonic.adserver.rtb.yieldlab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.AdObject;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydMarketPlace;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;
import com.adfonic.geo.SimpleCoordinates;

/**
 * https://tickets.byyd-tech.com/secure/attachment/21869/Yieldlab%20RTB%20for%20Demand_2.7_EN.pdf
 *
 */
public class YieldLabMapper {

    /**
     * Yieldlab requires a response time of maximum 120 ms. Any bids received after the response time has elapsed will be ignored.
     */
    public static final long YL_DEFAULT_TIMEOUT = 120L;

    /**
     * WIDTHxHEIGHT (e.g. 120x600)
     */
    private static final Pattern YL_SIZE_PATTERN = Pattern.compile("(\\d+)x(\\d+)");

    // Not super nice to hardcode it, but this value will never change
    private static final String YL_BYYD_SEAT = "22147";

    /**
     * Byyd default SeatID for Yieldlab. 
     * Since we turned on multiagency on Yieldlab, every bid request now contains "seat" atribute 
     * and we are required to send "seatid" in bid response.
     * 
     * By default and when no deal is in bid request (99.9% of bid requests), this default SeatID is sent as "seatid" in response. 
     * If some direct deal shall be set up using this seat, then this SeatID should be put into BID_SEAT table for Advertiser (but not necessary I think) 
     *   
     * Adding seat is email based administrative process that goes through PS 
     * Additional seats can be introduced for individual advertisers and if they will appear in bid request, they must be respected.  
     */
    private final String byydYlSeatId;

    public YieldLabMapper() {
        this(YL_BYYD_SEAT);
    }

    public YieldLabMapper(String byydYlSeatId) {
        Objects.requireNonNull(byydYlSeatId);
        this.byydYlSeatId = byydYlSeatId;
    }

    public ByydRequest getRequest(String publisherExternalId, HttpServletRequest httpRequest, RtbBidEventListener listener) throws NoBidException {

        List<String> missingParams = new ArrayList<>();

        String ylTransactionId = getParameter(httpRequest, "tid", true, missingParams); // Unique transaction ID
        ByydRequest byydRequest = new ByydRequest(publisherExternalId, ylTransactionId);

        // Parameter "tmax" is not part of Yl API, but it is useful for development/debugging  
        String tmaxStr = httpRequest.getParameter("tmax");
        if (StringUtils.isNotBlank(tmaxStr)) {
            byydRequest.setTmax(Long.parseLong(tmaxStr));
        } else {
            byydRequest.setTmax(YL_DEFAULT_TIMEOUT);
        }

        String ylwtype = httpRequest.getParameter("wtype"); // Integer indicating inventory type (0 - web, 1 - mobile web, 2 - mobile app, 3 - video/vast)
        switch (ylwtype) {
        case "1":
            byydRequest.setMedium(Medium.SITE);
            break;
        case "2":
            byydRequest.setMedium(Medium.APPLICATION);
            break;
        default:
            byydRequest.setMedium(Medium.UNKNOWN);
        }

        String ylRefer = httpRequest.getParameter("refer");
        if (ylRefer != null) {
            if (ylRefer.startsWith("http")) {
                byydRequest.setPublicationUrlString(ylRefer);
            } else {
                byydRequest.setPublicationUrlString("http://" + ylRefer);
            }
        }

        String ylSupplierId = getParameter(httpRequest, "sid", true, missingParams); // Supply Partner’s ID
        String ylWebsiteId = getParameter(httpRequest, "wid", true, missingParams); // Integer Parameter for the Yieldlab adslot ID
        byydRequest.setPublicationRtbId(ylSupplierId + "-" + ylWebsiteId);

        ByydImp byydImp = extractImp(httpRequest, missingParams, ylTransactionId, byydRequest);
        byydRequest.setImp(byydImp);

        ByydDevice device = extractDevice(httpRequest, missingParams);
        byydRequest.setDevice(device);

        ByydMarketPlace seatsAndDeals = extractSeatsAndDeals(httpRequest);
        byydRequest.setMarketPlace(seatsAndDeals);

        String ylLanguage = httpRequest.getParameter("lang");
        if (StringUtils.isNotBlank(ylLanguage)) {
            byydRequest.setAcceptedLanguageIsoCodes(Arrays.asList(ylLanguage));
        }

        String ylUserId = getParameter(httpRequest, "yl_id", true, missingParams); //Yieldlab user cookie ID
        if (StringUtils.isNotEmpty(ylUserId)) {
            ByydUser user = new ByydUser();
            user.setUid(ylUserId);

            // Apparently yieldlab has a habit of sending contradicting country info which can
            //  lead to us rejecting stuff down the line. So, don't take them seriously w.r.t. country
            //String yieldlabCountry = getParameter(request,"country",false,missingParams);
            //user.setCountryCode(yieldlabCountry);
            byydRequest.setUser(user);
        }

        byydRequest.doIncludeDestination(true);

        /*
         * Following fields are not being used
         * did - Demand Partner ID - Required for standard (non multi-agency) bid requests
        String yieldlabExternalUserID = getParameter(request,"ext_id",false,missingParams);
        String yieldlabGeoType = getParameter(request,"geo_type",false,missingParams);
        String yieldlabGeoAccuracy = getParameter(request,"geo_acc",false,missingParams);
        */
        if (!missingParams.isEmpty()) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, missingParams);
        }
        return byydRequest;
    }

    private ByydImp extractImp(HttpServletRequest httpRequest, List<String> missingParams, String ylTransactionId, ByydRequest byydRequest) throws NoBidException {
        ByydImp byydImp = new ByydImp(ylTransactionId);
        byydImp.setBidfloorcur(Constant.EUR);
        byydImp.setAdObject(AdObject.BANNER);

        String ylAdSize = getParameter(httpRequest, "adsize", true, missingParams);
        if (ylAdSize != null) {
            Matcher sizeMatcher = YL_SIZE_PATTERN.matcher(ylAdSize);
            if (!sizeMatcher.matches()) {
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.FORMAT_INVALID, ylAdSize);
            }
            byydImp.setW(Integer.parseInt(sizeMatcher.group(1)));
            byydImp.setH(Integer.parseInt(sizeMatcher.group(2)));
        }

        if (byydRequest.getMedium() == Medium.SITE) {
            byydImp.setContentFormWhiteList(ByydImp.CF_MOBILE_WEB);
        }

        String strSecure = httpRequest.getParameter("secure");
        if ("1".equals(strSecure)) {
            byydImp.setSslRequired(true);
        }
        return byydImp;
    }

    private ByydDevice extractDevice(HttpServletRequest httpRequest, List<String> missingParams) {
        ByydDevice byydDevice = new ByydDevice();
        String ylIp = getParameter(httpRequest, "ip", true, missingParams);
        byydDevice.setIp(ylIp);
        String ylUserAgent = getParameter(httpRequest, "user_agent", true, missingParams);
        byydDevice.setUserAgent(ylUserAgent);

        String ylLattitude = httpRequest.getParameter("lat");
        String ylLongitude = httpRequest.getParameter("lon");
        if (StringUtils.isNotEmpty(ylLattitude) && StringUtils.isNotEmpty(ylLongitude)) {
            try {
                double lat = Double.parseDouble(ylLattitude);
                double lon = Double.parseDouble(ylLongitude);
                byydDevice.setCoordinates(new SimpleCoordinates(lat, lon));
            } catch (NumberFormatException e) {
                //ignore
            }
        }

        // Beware they are sending empty string, '0' and possibly other crap
        // Also they are sending IFA/ADID with '-' characters removed (probably to save giga-penta-zeta-bytes of traffic)
        // Fortunately ther keep letters in IFA upper case as they should be
        String ylDeviceId = httpRequest.getParameter("ifa"); //Unique Identifier for mobile apps, e.g. Apple IFA, Android Advertising ID
        if (ylDeviceId != null && ylUserAgent != null) {
            if (ylDeviceId.length() == 32) {
                // Reconstruct GUID format 8-4-4-4-12 again 
                ylDeviceId = ylDeviceId.substring(0, 8) + "-" + ylDeviceId.substring(8, 12) + "-" + ylDeviceId.substring(12, 16) + "-" + ylDeviceId.substring(16, 20) + "-"
                        + ylDeviceId.substring(20, 32);
            } else if (ylDeviceId.length() == 36) {
                // length ok - do nothing 
            } else {
                ylDeviceId = null; // some crap - kill it
            }

            if (ylDeviceId != null) {
                if (ylUserAgent.contains("Android")) {
                    byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ADID, ylDeviceId);
                } else {
                    // Is sent lower-cased
                    byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_IFA, ylDeviceId.toUpperCase());
                }
            }
        }

        return byydDevice;
    }

    /**
     * Most likely is maximum of one seat and one deal, but to honor the Yieldlab spec...
     */
    public ByydMarketPlace extractSeatsAndDeals(HttpServletRequest httpRequest) {
        String ylSeats = httpRequest.getParameter("seats"); //* Comma-separated string of seat IDs. Required for multiagency bid requests.
        if (StringUtils.isEmpty(ylSeats)) {
            return null;
        } else {
            Map<String, List<String>> deal2seats = new HashMap<String, List<String>>(1);
            List<String> allDealSeatIds = new LinkedList<String>();
            String[] ylSeatIds = ylSeats.split(",");
            for (String seatId : ylSeatIds) {
                // Comma-separated string of deal IDs. If parameter is not provided for a given seat ID, all deals are allowed for that seat
                String ylDeals = httpRequest.getParameter("deals_" + seatId);
                if (StringUtils.isNotEmpty(ylDeals)) {
                    String[] dealIds = ylDeals.split(",");
                    for (String dealId : dealIds) {
                        List<String> seats4deal = deal2seats.get(dealId);
                        if (seats4deal == null) {
                            seats4deal = new LinkedList<String>();
                            deal2seats.put(dealId, seats4deal);
                        }
                        seats4deal.add(seatId);
                    }
                } else {
                    allDealSeatIds.add(seatId);
                }
            }
            if (deal2seats.isEmpty()) {
                return null; // there were no deal(s) only seat(s)
            }
            List<ByydDeal> byydDeals = new ArrayList<ByydDeal>(deal2seats.size());
            for (Map.Entry<String, List<String>> entry : deal2seats.entrySet()) {
                String dealId = entry.getKey();
                List<String> seatIds = entry.getValue();
                if (!allDealSeatIds.isEmpty()) {
                    seatIds.addAll(allDealSeatIds);
                }
                ByydDeal byydDeal = new ByydDeal(dealId, seatIds);
                //byydDeal.setAuctionType(AuctionType.SECOND_PRICE_ACTION);//Ask Yieldlab what kind of auction it is
                byydDeals.add(byydDeal);
            }

            //Ask Yieldlab is anybody outside of direct deal can bid on it
            return new ByydMarketPlace(byydDeals, true);
        }
    }

    public YieldlabBidResponse getResponse(ByydResponse byydResponse) {
        if (byydResponse == null || byydResponse.getBid() == null) {
            return new YieldlabBidResponse(byydResponse.getId()); // cpm:0
        }
        YieldlabBidResponse rtbResponse = new YieldlabBidResponse(byydResponse.getId());
        ByydBid byydBid = byydResponse.getBid();
        YieldlabBid yBid = rtbResponse.getBid();
        yBid.setCpm(byydBid.getPrice().toString());
        yBid.setAdtag(byydBid.getAdm());
        yBid.setTid(byydResponse.getId());
        yBid.setAdvertiser(byydBid.getAdomain());
        yBid.setCamurl(byydBid.getDestination());

        // Required once we start using seats (Just like Rubicon)
        String seat = byydBid.getSeat();
        if (seat != null) {
            // PMP seat
            yBid.setSeatid(seat);
        } else {
            // Use default seatid then... 
            yBid.setSeatid(byydYlSeatId);
        }

        // Optional deal
        String dealId = byydBid.getDealId();
        yBid.setDealid(dealId);
        return rtbResponse;
    }

    private String getParameter(HttpServletRequest request, String paranmName, boolean required, List<String> missingParamList) {
        String paramValue = request.getParameter(paranmName);
        if (required && StringUtils.isEmpty(paramValue)) {
            missingParamList.add(paranmName);
        }
        return paramValue;
    }

}