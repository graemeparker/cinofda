package com.adfonic.adserver.controller.rtb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.adfonic.adserver.offence.OffenceRegistry.BidExceptionStats;
import com.adfonic.adserver.offence.OffenceSection;
import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.mapper.AdXMapper;
import com.adfonic.adserver.rtb.mapper.AdXMapperTest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;

public class AdXControllerTest extends AbstractBidTest<AdXController> {

    private static File TEST_FILE = new File("src/test/data/adx/app-320x50-iphone-8.2.proto.txt");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/dcadx/bid/ca6e5a4c-1d67-490c-95e2-4877cea57bb9";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.DcAdX;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return AdXMapper.protoText2Bytes(FileUtils.readFileToString(TEST_FILE));
    }

    @Override
    protected AdXController buildController() {
        //Rtb.Enc.dcadx.eKey64 and Rtb.Enc.dcadx.iKey64 from adfonic-adserver.properties
        return new AdXController(AdXMapperTest.adxMapper, rtbLogicMock, offenceRegistry, fisherman, bidListenerMock, backupLoggerMock, counterManager);
    }

    @Override
    ByydResponse getByydResponse() throws IOException {
        AdX.BidRequest adxRequest = AdX.BidRequest.parseFrom(getBidRequestPayload());
        ByydResponse byydResponse = buildByydResponse(String.valueOf(adxRequest.getAdslotList().get(0).getId()));
        // AdX eats only secure creatives
        byydResponse.getBid().getCreative().setSslCompliant(true);
        return byydResponse;
    }

    @Test
    public void test() throws Exception {
        AdX.BidRequest adxRequest = AdX.BidRequest.parseFrom(getBidRequestPayload());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        adxRequest.writeTo(baos);

        // When
        setRtbLogicResponse(getByydResponse());
        ResultActions mvcActions = mockMvc.perform(mockRequest().content(baos.toByteArray()));

        // Then
        mvcActions.andExpect(MockMvcResultMatchers.status().isOk());

        if (!offenceRegistry.values().isEmpty()) {
            OffenceSection section = offenceRegistry.values().iterator().next();
            BidExceptionStats[] stats = section.values().iterator().next();
            Exception offence = stats[0].getOffence();
            Assertions.fail("Unexpected offence", offence);
        }

        MvcResult mvcResult = mvcActions.andReturn();
        AdX.BidResponse adxResponse = AdX.BidResponse.parseFrom(mvcResult.getResponse().getContentAsByteArray());
        Assertions.assertThat(adxResponse.getAdList()).isNotEmpty();
        //System.out.println(TextFormat.printToString(adxResponse));

        /*
        builder.setId(ByteString.copyFromUtf8("Mv\2005\000\017.\001\n\345\177\307X\200M8"));
        builder.setIp(ByteString.copyFromUtf8("\314j\310"));
        builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.107 Safari/534.13,gzip");
        builder.setUrl("http://www.example.com/");
        builder.addDetectedLanguage("en");
        builder.addDetectedVertical(AdX.BidRequest.Vertical.newBuilder().setId(22).setWeight(0.67789277f));
        builder.addDetectedVertical(AdX.BidRequest.Vertical.newBuilder().setId(355).setWeight(0.32210726f));

        MatchingAdData.Builder mad1 = MatchingAdData.newBuilder().setAdgroupId(3254984134l).setMinimumCpmMicros(2000000);
        MatchingAdData.Builder mad2 = MatchingAdData.newBuilder().setAdgroupId(2646216548l).setMinimumCpmMicros(2000000);
        AdSlot adSlot = AdSlot.newBuilder().setId(1).addWidth(300).addHeight(250).addExcludedAttribute(7).addExcludedAttribute(22).addMatchingAdData(mad1).addMatchingAdData(mad2)
                .addTargetableChannel("all pages,middle right").setPublisherSettingsListId(4985794913155998850l).setSlotVisibility(SlotVisibility.BELOW_THE_FOLD).build();
        builder.addAdslot(adSlot);
        builder.setIsTest(false);
        builder.setCookieVersion(1);
        builder.setGoogleUserId("CAESEIcS1pC2TBvb-4SLDjMqsY9");
        builder.setSellerNetworkId(1);
        //builder.setPublisherSettingsListId(0)
        builder.setVerticalDictionaryVersion(2);
        builder.setTimezoneOffset(-300);
        builder.setCookieAgeSeconds(7685804);
        builder.setGeoCriteriaId(1001193);
        */
    }

}
