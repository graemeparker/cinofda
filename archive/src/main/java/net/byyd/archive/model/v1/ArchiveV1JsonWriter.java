package net.byyd.archive.model.v1;

import net.byyd.archive.mapping.fastjson.AbstractFastJsonWriter;

public class ArchiveV1JsonWriter extends AbstractFastJsonWriter<AdEvent> {

    private AdEvent d = new AdEvent();

    @Override
    public void write(AdEvent o, StringBuilder sb) {
        sb.append(openBracket);

        writeEnum("a", o.getAdAction(), d.getAdAction(), sb);
        write("a3p", o.getAccountingThirdPartyAdServing(), d.getAccountingThirdPartyAdServing(), sb);
        write("abp", o.getAccountingBuyerPremium(), d.getAccountingBuyerPremium(), sb);
        write("acc", o.getAccountingCost(), d.getAccountingCost(), sb);
        write("acd", o.getAccountingCampaignDiscount(), d.getAccountingCampaignDiscount(), sb);
        write("acm", o.getAccountingCustMargin(), d.getAccountingCustMargin(), sb);
        writeString("acp", o.getAccountingParameters(), d.getAccountingParameters(), sb);
        write("adc", o.getAccountingDirectCost(), d.getAccountingDirectCost(), sb);
        write("ad2", o.getAccountingDirectCostRaw(), d.getAccountingDirectCostRaw(), sb);
        write("adm", o.getAccountingDspMargin(), d.getAccountingDspMargin(), sb);
        writeString("adn", o.getAdomain(), d.getAdomain(), sb);
        write("adr", o.getAccountingDataRetail(), d.getAccountingDataRetail(), sb);
        write("advc", o.getAdvertiserCompanyId(), d.getAdvertiserCompanyId(), sb);
        write("advi", o.getAdvertiserId(), d.getAdvertiserId(), sb);
        write("adw", o.getAccountingDataWholesale(), d.getAccountingDataWholesale(), sb);
        write("apo", o.getAccountingPayout(), d.getAccountingPayout(), sb);
        write("app", o.getApplication(), d.getApplication(), sb);
        write("arf", o.getAgeFrom(), d.getAgeFrom(), sb);
        write("ari", o.isAgeIntegral(), d.isAgeIntegral(), sb);
        write("art", o.getAgeTo(), d.getAgeTo(), sb);
        write("as", o.getAdSpaceId(), d.getAdSpaceId(), sb);
        writeString("ase", o.getAdSpaceExternalId(), d.getAdSpaceExternalId(), sb);
        write("atf", o.getAccountingTechFee(), d.getAccountingTechFee(), sb);
        write("av", o.getActionValue(), d.getActionValue(), sb);
        write("awi", o.getAdOpsOwnerId(), d.getAdOpsOwnerId(), sb);
        write("bp", o.getRtbBidPrice(), d.getRtbBidPrice(), sb);
        writeString("bte", o.getBidType(), d.getBidType(), sb);
        write("c", o.getCreativeId(), d.getCreativeId(), sb);
        write("ca", o.getCampaignId(), d.getCampaignId(), sb);
        write("ccd", o.getCampaignCurrentDataFeeId(), d.getCampaignCurrentDataFeeId(), sb);
        writeString("clu", o.getClickUrlCookie(), d.getClickUrlCookie(), sb);
        write("co", o.getCountryId(), d.getCountryId(), sb);
        write("cri", o.getCarrierId(), d.getCarrierId(), sb);
        writeMap("di", o.getDeviceIdentifiers(), d.getDeviceIdentifiers(), sb);
        writeDate("dob", o.getDateOfBirth(), d.getDateOfBirth(), sb);
        write("dti", o.getDisplayTypeId(), d.getDisplayTypeId(), sb);
        write("exi", o.getExchangeId(), d.getExchangeId(), sb);
        write("fmi", o.getFormatId(), d.getFormatId(), sb);
        writeEnum("g", o.getGender(), d.getGender(), sb);
        writeString("gcn", o.getGeoCountry(), d.getGeoCountry(), sb);
        writeString("gct", o.getGeoCity(), d.getGeoCity(), sb);
        write("gla", o.getLatitude(), d.getLatitude(), sb);
        write("glo", o.getLongitude(), d.getLongitude(), sb);
        writeString("gls", o.getLocationSource(), d.getLocationSource(), sb);
        writeString("gpc", o.getGeoPostalCode(), d.getGeoPostalCode(), sb);
        writeString("grn", o.getGeoRegion(), d.getGeoRegion(), sb);
        write("gt", o.getGeotargetId(), d.getGeotargetId(), sb);
        write("gtp", o.getGeoType(), d.getGeoType(), sb);
        writeString("h", o.getHost(), d.getHost(), sb);
        writeString("hrf", o.getHeaderReferrer(), d.getHeaderReferrer(), sb);
        writeString("ia", o.getIpAddress(), d.getIpAddress(), sb);
        writeString("ieid", o.getImpressionExternalID(), d.getImpressionExternalID(), sb);
        writeString("ior", o.getIoReference(), d.getIoReference(), sb);
        write("it", o.getIntegrationTypeId(), d.getIntegrationTypeId(), sb);
        writeString("iul", o.getIurl(), d.getIurl(), sb);
        write("m", o.getModelId(), d.getModelId(), sb);
        writeString("mam", o.getAdditionalMessage(), d.getAdditionalMessage(), sb);
        writeString("mdr", o.getDetailReason(), d.getDetailReason(), sb);
        write("op", o.getOperatorId(), d.getOperatorId(), sb);
        write("p", o.getPublicationId(), d.getPublicationId(), sb);
        writeString("pd", o.getPublicationDomain(), d.getPublicationDomain(), sb);
        write("pc", o.getPostalCodeId(), d.getPostalCodeId(), sb);
        writeString("pct", o.getPostalCode(), d.getPostalCode(), sb);
        write("rc", o.getResponseController(), d.getResponseController(), sb);
        write("ro", o.getResponseOverall(), d.getResponseOverall(), sb);
        writeString("rqh", o.getRequestHost(), d.getRequestHost(), sb);
        writeString("rqu", o.getRequestURL(), d.getRequestURL(), sb);
        write("rtb", o.getRtb(), d.getRtb(), sb);
        writeString("s", o.getStrategy(), d.getStrategy(), sb);
        write("sp", o.getRtbSettlementPrice(), d.getRtbSettlementPrice(), sb);
        writeString("sr", o.getShard(), d.getShard(), sb);
        writeString("sv", o.getServerName(), d.getServerName(), sb);
        write("swi", o.getSalesOwnerId(), d.getSalesOwnerId(), sb);
        writeDate("t", o.getEventTime(), d.getEventTime(), sb);
        writeString("ti", o.getTrackingIdentifier(), d.getTrackingIdentifier(), sb);
        write("tm", o.isTestMode(), d.isTestMode(), sb);
        writeString("ua", o.getUserAgentHeader(), d.getUserAgentHeader(), sb);
        writeString("ucr", o.getUserCountry(), d.getUserCountry(), sb);
        writeString("uct", o.getUserCity(), d.getUserCity(), sb);
        writeEnum("ur", o.getUnfilledReason(), d.getUnfilledReason(), sb);
        write("ut", o.getUserTimeId(), d.getUserTimeId(), sb);
        writeMapBigDecimal("abb", o.getAccountingBidDeductionsByyd(), d.getAccountingBidDeductionsByyd(), sb);
        writeMapBigDecimal("abc", o.getAccountingBidDeductionsCustomer(), d.getAccountingBidDeductionsCustomer(), sb);
        writeMapBigDecimal("abv", o.getAccountingBidDeductionsVendor(), d.getAccountingBidDeductionsVendor(), sb);

        //Fields just for impressions and clicks
        writeString("pdu", o.getpDestinationurl(), d.getpDestinationurl(), sb);
        write("ssl", o.getSslRequired(), d.getSslRequired(), sb);

        sb.append(closeBracket);
    }

}
