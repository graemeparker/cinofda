package com.adfonic.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.campaignagencydiscount.CampaignAgencyDiscountDto;
import com.adfonic.dto.campaign.campaignbid.CampaignBidDto;
import com.adfonic.presentation.campaign.CampaignService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/adfonic-tools2-context.xml" })
public class CampaignServiceIT {

    @Autowired
    private CampaignService campaignService;

    @Test
    public void testSaveBid() {
        try {
            double bidValue = 0.5;
            double discountValue = 0.6;
            CampaignDto campaign = new CampaignDto();
            campaign.setId(3471L);

            campaign = campaignService.getCampaignById(campaign);

            // CampaignBidDto oldBid = campaign.getCurrentBid();
            // CampaignAgencyDiscountDto oldDiscount =
            // campaign.getCurrentAgencyDiscount();

            CampaignBidDto bid = new CampaignBidDto();
            bid.setAmount(new BigDecimal(bidValue));
            campaign.setCurrentBid(bid);

            CampaignAgencyDiscountDto discount = new CampaignAgencyDiscountDto();
            discount.setDiscount(new BigDecimal(discountValue));
            campaign.setCurrentAgencyDiscount(discount);

            campaign = campaignService.saveBid(campaign);

            assertNotNull(campaign.getCurrentBid());
            assertEquals(new Double(campaign.getCurrentBid().getAmount().doubleValue()), new Double(bidValue));
            assertNotNull(campaign.getCurrentAgencyDiscount());
            assertEquals(new Double(campaign.getCurrentAgencyDiscount().getDiscount().doubleValue()), new Double(discountValue));

            bid = new CampaignBidDto();
            bid.setAmount(new BigDecimal(bidValue + 0.1));
            campaign.setCurrentBid(bid);
            discount = new CampaignAgencyDiscountDto();
            discount.setDiscount(new BigDecimal(discountValue + 0.1));
            campaign.setCurrentAgencyDiscount(discount);

            campaign = campaignService.saveBid(campaign);

            assertNotNull(campaign.getCurrentBid());
            assertEquals(new Double(campaign.getCurrentBid().getAmount().doubleValue()), new Double(bidValue + 0.1));
            assertNotNull(campaign.getCurrentAgencyDiscount());
            assertEquals(new Double(campaign.getCurrentAgencyDiscount().getDiscount().doubleValue()), new Double(discountValue + 0.1));

        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }

    }
}
