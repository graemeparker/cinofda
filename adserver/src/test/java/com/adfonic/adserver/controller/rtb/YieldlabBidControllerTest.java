package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydMarketPlace;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.yieldlab.YieldLabMapper;

/**
 * 
 * @author mvanek
 * 
 * Yieldlab
 * - GET instead of JSON POST
 * - Mobile websites only
 * - EUR instead of USD
 * - Operates in Germany
 * 
 * https://confluence.byyd-tech.com/display/TECH/Yieldlab
 *
 */
public class YieldlabBidControllerTest extends AbstractBidTest<YieldlabController> {

    private final String EMPTY_RESPONSE = "{\"bid\":{\"cpm\":\"0.0\",\"tid\":\"${tid}\"}}";

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/yieldlab/bid/3d36b3c5-b513-4bf2-bc91-f39ec6495e7b";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.YieldLab;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected YieldlabController buildController() {
        return new YieldlabController(rtbLogicMock, offenceRegistry, fisherman, backupLoggerMock, bidListenerMock, counterManager);
    }

    /**
     * In the above example, the agencies with seat IDs 100, 101 and 102 can participate in the auction. 
     * In addition, the agency with seat 100 is only allowed to make bids belonging to the deal with ID 245. 
     * All other seats are allowed to make bids belong to any deal, if applicable. 
     * @throws Exception 
     */
    @Test
    public void seatsAndDeals() throws Exception {
        YieldLabMapper mapper = new YieldLabMapper();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/whatever", "publ-extid-irelevant");
        requestBuilder.param("seats", "100,101,102");
        requestBuilder.param("deals_100", "245");
        MockHttpServletRequest httpRequest = requestBuilder.buildRequest(new MockServletContext());

        // When
        ByydMarketPlace marketPlace = mapper.extractSeatsAndDeals(httpRequest);
        // Then
        Assertions.assertThat(marketPlace.isPrivateDeal()).isTrue();
        List<ByydDeal> deals = marketPlace.getDeals();
        Assertions.assertThat(deals).hasSize(1);
        ByydDeal byydDeal = deals.get(0);
        Assertions.assertThat(byydDeal.getId()).isEqualTo("245");
        Assertions.assertThat(byydDeal.getSeats()).containsExactly("100", "101", "102");
        Assertions.assertThat(byydDeal.getBidFloor()).isNull();
    }

    @Test
    public void seatsAndDealsSimpleScenario() throws Exception {
        YieldLabMapper mapper = new YieldLabMapper();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/whatever", "publ-extid-irelevant");
        requestBuilder.param("seats", "seat-abcd");
        requestBuilder.param("deals_seat-abcd", "deal-123456");
        MockHttpServletRequest httpRequest = requestBuilder.buildRequest(new MockServletContext());

        // When
        ByydMarketPlace marketPlace = mapper.extractSeatsAndDeals(httpRequest);
        // Then
        Assertions.assertThat(marketPlace.isPrivateDeal()).isTrue();
        List<ByydDeal> deals = marketPlace.getDeals();
        Assertions.assertThat(deals).hasSize(1);
        ByydDeal byydDeal = deals.get(0);
        Assertions.assertThat(byydDeal.getId()).isEqualTo("deal-123456");
        Assertions.assertThat(byydDeal.getSeats()).containsExactly("seat-abcd");
        Assertions.assertThat(byydDeal.getBidFloor()).isNull();
    }

    @Test
    public void seatsAndDealsComplexScenario() throws Exception {
        YieldLabMapper mapper = new YieldLabMapper();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/whatever", "publ-extid-irelevant");
        requestBuilder.param("seats", "byyd-seat,second-seat,another-seat,super-seat");
        requestBuilder.param("deals_byyd-seat", "1234,5678");
        requestBuilder.param("deals_second-seat", "abcd");
        requestBuilder.param("deals_another-seat", "1234");

        MockHttpServletRequest httpRequest = requestBuilder.buildRequest(new MockServletContext());
        // When
        ByydMarketPlace marketPlace = mapper.extractSeatsAndDeals(httpRequest);
        // Then
        Assertions.assertThat(marketPlace.isPrivateDeal()).isTrue();
        List<ByydDeal> deals = marketPlace.getDeals();
        Assertions.assertThat(deals).hasSize(3);

        ByydDeal byydDeal1234 = marketPlace.findDealById("1234");
        Assertions.assertThat(byydDeal1234.getId()).isEqualTo("1234");
        Assertions.assertThat(byydDeal1234.getSeats()).containsExactly("byyd-seat", "another-seat", "super-seat");

        ByydDeal byydDeal5678 = marketPlace.findDealById("5678");
        Assertions.assertThat(byydDeal5678.getId()).isEqualTo("5678");
        Assertions.assertThat(byydDeal5678.getSeats()).containsExactly("byyd-seat", "super-seat");

        ByydDeal byydDealabcd = marketPlace.findDealById("abcd");
        Assertions.assertThat(byydDealabcd.getId()).isEqualTo("abcd");
        Assertions.assertThat(byydDealabcd.getSeats()).containsExactly("second-seat", "super-seat");
    }

    @Test
    public void http200_OnNoBidException() throws Exception {

        MockHttpServletRequestBuilder mockRequest = getBidHttpRequest();
        //MockHttpServletRequest httpRequest = mockRequest.buildRequest(new MockServletContext());

        ByydRequest byydRequest = new ByydRequest("pub-ext-id-zx-zx-zx-zx", "byyd-req-" + System.currentTimeMillis());
        NoBidException noBidException = new NoBidException(byydRequest, NoBidReason.NOTHING_TO_BID, AdSrvCounter.UNKNOWN_PUBLICATION, "test-" + System.currentTimeMillis());
        setRtbLogicException(noBidException);

        //When 
        ResultActions actions = mockMvc.perform(mockRequest);
        //Then

        // Ensure that mock was called 
        Mockito.verify(rtbLogicMock).bid(Mockito.any(RtbExecutionContext.class), Mockito.eq(bidListenerMock), Mockito.any(TargetingEventListener.class));

        String tid = mockRequest.buildRequest(null).getParameter("tid");

        // Returned
        actions.andExpect(MockMvcResultMatchers.status().isOk());
        //actions.andExpect(MockMvcResultMatchers.content().contentType(getEndpoint().getProtocol().getResponseMediaType()));
        MvcResult mvcResult = actions.andReturn();
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(EMPTY_RESPONSE.replace("${tid}", tid));
        actions.andExpect(MockMvcResultMatchers.header().string("Expires", "0"));
        actions.andExpect(MockMvcResultMatchers.header().string("Pragma", "No-Cache"));

        // Recorded
        Assertions.assertThat(offenceRegistry.values()).hasSize(0);

        // Logged
        /*
        List<LogRecord> rtbSequenceLogs = LogCapturingHandler.get().list().stream().filter(record -> record.getLoggerName().equals(RtbBidSequence.class.getName()))
                .collect(Collectors.toList());
        LogRecord lastLogRecord = rtbSequenceLogs.get(rtbSequenceLogs.size() - 1);

        Assertions.assertThat(lastLogRecord.getLoggerName()).isEqualTo(RtbBidSequence.class.getName());//getController().getClass().getName());
        Assertions.assertThat(lastLogRecord.getMessage()).isEqualTo(noBidException.toString());
        */
    }

    /**
     * yl_id=78dd160e-205f-4f3e-88c8-064305df8600&tid=215c5e65-5f1f-4bb5-b14a-e60b363a14e9&sid=8477&wid=26135&adsize=300x600&wtype=0&wpos=0&at=1&secure=0&country=DE
     * &user_agent=Mozilla%2F5.0+%28iPad%3B+CPU+OS+8_2+like+Mac+OS+X%29+AppleWebKit%2F600.1.4+%28KHTML%2C+like+Gecko%29+Mobile%2F12D508+%5BFBAN%2FFBIOS%3BFBAV%2F25.0.0.11.10%3BFBBV%2F7293189%3BFBDV%2FiPad5%2C3%3BFBMD%2FiPad%3BFBSN%2FiPhone+OS%3BFBSV%2F8.2%3BFBSS%2F2%3B+FBCR%2F%3BFBID%2Ftablet%3BFBLC%2Fde_DE%3BFBOP%2F1%5D
     * &lang=de_DE&ip=79.234.227.0&cat=IAB11,IAB12,IAB17,IAB2,IAB3&did=22147
     * 
     * yl_id=15359307-4131-4d79-9163-8112da9cdd86&tid=44c07a26-8003-4adf-aca7-353ae891e9fc&sid=31648&wid=71886&adsize=320x50&wtype=1&wpos=0&at=2&secure=0&country=DE
     * &user_agent=Mozilla%2F5.0+%28iPad%3B+CPU+OS+8_1_2+like+Mac+OS+X%29+AppleWebKit%2F600.1.4+%28KHTML%2C+like+Gecko%29+Mobile%2F12B440+%5BFBAN%2FFBIOS%3BFBAV%2F25.0.0.11.10%3BFBBV%2F7293189%3BFBDV%2FiPad4%2C1%3BFBMD%2FiPad%3BFBSN%2FiPhone+OS%3BFBSV%2F8.1.2%3BFBSS%2F2%3B+FBCR%2F%3BFBID%2Ftablet%3BFBLC%2Fde_DE%3BFBOP%2F1%5D&lang=de_DE
     * &refer=intouch.de&ip=91.47.236.0&cat=IAB14,IAB18,IAB22,IAB7&did=22147&ifa=
     * 
     * yl_id=b184c693-2593-47cd-86a1-cb50a6f3e1c2&tid=053b6408-51ad-415e-895c-f9a2000abe02&sid=5710&wid=12252&adsize=320x50&wtype=2&wpos=0&at=2&secure=0&country=DE
     * &user_agent=Mozilla%2F5.0+%28Linux%3B+Android+5.0%3B+SM-G900F+Build%2FLRX21T%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F37.0.0.0+Mobile+Safari%2F537.36
     * &lang=de_DE&refer=m.sport1.de&ip=2.206.2.0&did=22147
     * 
     * yl_id=98835af1-22b2-4bae-8302-57ac1b0f7881&tid=d9ace808-4406-4d01-a9c0-419776d90a3a&sid=8477&wid=26135&adsize=300x600&wtype=0&wpos=0&at=1&secure=0&country=DE
     * &user_agent=Mozilla%2F5.0+%28Windows+NT+6.1%3B+rv%3A36.0%29+Gecko%2F20100101+Firefox%2F36.0
     * &lang=de&ip=91.2.95.0&cat=IAB11,IAB12,IAB17,IAB2,IAB3&did=22147
     */
    @Override
    public MockHttpServletRequestBuilder getBidHttpRequest() {
        //See YieldLabMapper what parameters are required and optional
        MockHttpServletRequestBuilder mockRequest = mockRequest();
        //taken from real traffic
        mockRequest.param("yl_id", "03c1e6cb-cd64-48d4-bd2e-b3a3a1cf8307");
        mockRequest.param("tid", "bbce6462-0d1f-4f5b-9dfd-80898fb99387");
        mockRequest.param("sid", "172");
        mockRequest.param("wid", "49357");
        mockRequest.param("adsize", "320x75");
        mockRequest.param("wtype", "2");
        mockRequest.param("wpos", "0");
        mockRequest.param("at", "2");
        mockRequest.param("secure", "0");
        mockRequest.param("country", "DE");
        mockRequest.param("user_agent",
                "Mozilla/5.0 (Linux; Android 4.4.2; SM-G900F Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
        mockRequest.param("lang", "de_DE");
        mockRequest.param("refer", "http://a.spiegel.de/wirtschaft/unternehmen/a-1023214.html");
        mockRequest.param("ip", "17.165.90.0");
        mockRequest.param("did", "22147");
        return mockRequest;
    }

}
