package com.adfonic.tools.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.campaigndatafee.CampaignDataFeeDto;
import com.adfonic.dto.campaign.campaignrichmediaadservingfee.CampaignRichMediaAdServingFeeDto;
import com.adfonic.dto.campaign.campaigntradingdeskmargin.CampaignTradingDeskMarginDto;
import com.adfonic.dto.company.AccountFixedMarginDto;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.exception.BudgetValidatorException;

public class BudgetUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetUtils.class);

    private static final String KEY_BID_GREATER_THAN_OVERALL_BUDGET = "page.campaign.bidbudget.greaterthanbudget";
    private static final String KEY_BID_GREATER_THAN_DAILY_BUDGET = "page.campaign.bidbudget.greaterthandalily";
    private static final String KEY_BID_GREATER_THAN_THRESHOLD = "page.campaign.bidbudget.greaterthanthreshold";
    private static final String KEY_BID_PRICE_REQUIRED = "page.campaign.bidbudget.bidprice.required";
    private static final String KEY_BID_CPX_MIN = "page.campaign.bidbudget.bidprice.minimum.cpx";
    
    private static final int INT_2 = 2;
    private static final int INT_3 = 3;

    private static final String COMP_CPC_PRICE = "cpc-price";
    private static final String COMP_CPM_PRICE = "cpm-price";
    private static final String COMP_CPX_PRICE = "cpx-price";
    private static final String COMP_DAILY_BUDGET = "daily-budget";
    private static final String COMP_CAMPAIGN_BUDGET = "campaign-budget";
    
    public static final String CPC = "CPC";
    public static final String CPM = "CPM";
    public static final String CPI = "CPI";
    public static final String CPA = "CPA";

    public static final String CLICK = "CLICK";
    public static final String MONETARY = "MONETARY";
    public static final String IMPRESSION = "IMPRESSION";
    
    public static final BigDecimal BIG_DECIMAL_0 = new BigDecimal(0);
    public static final BigDecimal BIG_DECIMAL_100 = new BigDecimal(100);
    
    private BudgetUtils () {
        // Utility class
    }
    
    /**
     * Validate Budget prices
     * 
     * @throws BudgetValidatorException
     */
    public static void validatePrices(CampaignDto campaignDto, String bidType, String amountCpc, String amountCpm, String amountCpx,
        String dailyBudget, String overallBudget, Double campaignDailyBudget, boolean averageMaximumBidEnabled, BigDecimal averageMaximumBidThreshold) {
        validateAdServincCpmFee(campaignDto);
        validateCpc(campaignDto, bidType, amountCpc, dailyBudget, overallBudget, averageMaximumBidEnabled, averageMaximumBidThreshold);
        validateCpm(campaignDto, bidType, amountCpm, dailyBudget, overallBudget, averageMaximumBidEnabled, averageMaximumBidThreshold);
        validateCpx(campaignDto, bidType, amountCpx, dailyBudget, overallBudget, averageMaximumBidEnabled, averageMaximumBidThreshold);
        validateBudgets(campaignDto, dailyBudget, overallBudget, campaignDailyBudget);
    }

    /**
     * Validate daily and overall budgets
     * 
     * @throws BudgetValidatorException
     */
    private static void validateBudgets(CampaignDto campaignDto, String dailyBud, String overallBud, Double campaignDailyBudget) {
        String budgetType = campaignDto.getBudType();
        String dailyBudget = roundPrice(campaignDto, dailyBud);
        String overallBudget = roundPrice(campaignDto, overallBud);
        
        if (isMonetary(budgetType) && !StringUtils.isEmpty(dailyBudget) && Double.valueOf(dailyBudget) < Constants.MIN_DAILY_BUDGET) {
            LOGGER.debug("Daily budget lower than 10");
            throw new BudgetValidatorException(COMP_DAILY_BUDGET, "page.campaign.bidbudget.dailybudget.low");
        } else if (!StringUtils.isEmpty(dailyBudget) && Double.valueOf(dailyBudget) <= 0) {
            LOGGER.debug("Daily budget lower than 0");
            throw new BudgetValidatorException(COMP_DAILY_BUDGET, "page.campaign.bidbudget.budget.low");
        }
        if (!StringUtils.isEmpty(overallBudget) && Double.valueOf(overallBudget) <= 0) {
            LOGGER.debug("overall budget lower than 0");
            throw new BudgetValidatorException(COMP_CAMPAIGN_BUDGET, "page.campaign.bidbudget.budget.low");
        }
        if (isMonetary(budgetType) && !StringUtils.isEmpty(dailyBudget) && campaignDailyBudget != null && Double.valueOf(dailyBudget) > campaignDailyBudget) {
            LOGGER.debug("Daily budget bigger than account daily budget");
            throw new BudgetValidatorException(COMP_DAILY_BUDGET, "page.campaign.bidbudget.dailybudget.big");
        }
        if (isMonetary(budgetType) && !StringUtils.isEmpty(dailyBudget) && campaignDailyBudget != null && Double.valueOf(dailyBudget) > campaignDailyBudget) {
            LOGGER.debug("Daily budget bigger than account daily budget");
            throw new BudgetValidatorException(COMP_DAILY_BUDGET, "page.campaign.bidbudget.dailybudget.big");
        }
        if (!StringUtils.isEmpty(overallBudget) && !StringUtils.isEmpty(dailyBudget) && Double.valueOf(overallBudget) < Double.valueOf(dailyBudget)) {
            LOGGER.debug("Overall budget smaller than daily budget!");
            throw new BudgetValidatorException(COMP_DAILY_BUDGET, "page.campaign.bidbudget.dailygreaterthanbudget");
        }
    }

    /**
     * Validate CPX
     * 
     * @throws BudgetValidatorException
     */
    private static void validateCpx(CampaignDto campaignDto, String bidType, String cpxAmount, String dailyBud, String overallBud,
    		boolean averageMaximumBidEnabled, BigDecimal averageMaximumBidThreshold) {
        if (getCpxSelected(bidType)) {
            String budgetType = campaignDto.getBudType();
            String amountCpx = roundPrice(campaignDto, cpxAmount);
            String dailyBudget = roundPrice(campaignDto, dailyBud);
            String overallBudget = roundPrice(campaignDto, overallBud);
            boolean isPriceOverridden = campaignDto.isPriceOverridden();
            
            if (StringUtils.isEmpty(amountCpx)) {
                LOGGER.debug("CPX empty");
                throw new BudgetValidatorException(COMP_CPX_PRICE, KEY_BID_PRICE_REQUIRED);
                
            // since this is disabled in UI for non-admin just check overridden.
            // cpx min price >= MIN_BID
            } else if (isPriceOverridden) {
                if (isPriceOverridden && isCPI(bidType) && Double.valueOf(amountCpx) < Constants.MIN_BID) {
                    LOGGER.debug("CPI smaller MIN_BID and priceOverriden");
                    throw new BudgetValidatorException(COMP_CPX_PRICE, KEY_BID_CPX_MIN, CPI, Double.toString(Constants.MIN_BID));
                } else if (isPriceOverridden && isCPA(bidType) && Double.valueOf(amountCpx) < Constants.MIN_BID) {
                    LOGGER.debug("CPA smaller than MIN_BID and priceOverridden");
                    throw new BudgetValidatorException(COMP_CPX_PRICE, KEY_BID_CPX_MIN, CPA, Double.toString(Constants.MIN_BID));
                }
            } else if (isCPI(bidType) && Double.valueOf(amountCpx) < Constants.MIN_CPI_BID) {
                LOGGER.debug("CPI smaller than 2");
                throw new BudgetValidatorException(COMP_CPX_PRICE, KEY_BID_CPX_MIN, CPI, Double.toString(Constants.MIN_CPI_BID));
            } else if (isCPA(bidType) && Double.valueOf(amountCpx) < Constants.MIN_CPA_BID) {
                LOGGER.debug("CPA smaller than 5");
                throw new BudgetValidatorException(COMP_CPX_PRICE, KEY_BID_CPX_MIN, CPA, Double.toString(Constants.MIN_CPA_BID));
            }
            if (isMonetary(budgetType) && !StringUtils.isEmpty(dailyBudget) && amountCpx != null && Double.valueOf(dailyBudget) < Double.valueOf(amountCpx)) {
                LOGGER.debug("Daily budget smaller than cpx!");
                throw new BudgetValidatorException(COMP_CPX_PRICE, KEY_BID_GREATER_THAN_DAILY_BUDGET);
            }
            if (isMonetary(budgetType) && !StringUtils.isEmpty(overallBudget) && amountCpx != null && Double.valueOf(overallBudget) < Double.valueOf(amountCpx)) {
                LOGGER.debug("Overall budget smaller than cpx!");
                throw new BudgetValidatorException(COMP_CPX_PRICE, KEY_BID_GREATER_THAN_OVERALL_BUDGET);
            }
            validateBidPriceAgainstAverageMaximumBidBiddingStrategy(campaignDto, COMP_CPX_PRICE, amountCpx, averageMaximumBidEnabled, averageMaximumBidThreshold);
        }
    }

    /**
     * Validate CPM
     * 
     * @throws BudgetValidatorException
     */
    private static void validateCpm(CampaignDto campaignDto, String bidType, String cpmAmount, String dailyBud, String overallBud,
    		boolean averageMaximumBidEnabled, BigDecimal averageMaximumBidThreshold) {
        if (getCpmSelected(bidType)) {
            String budgetType = campaignDto.getBudType();
            String amountCpm = roundPrice(campaignDto, cpmAmount);
            String dailyBudget = roundPrice(campaignDto, dailyBud);
            String overallBudget = roundPrice(campaignDto, overallBud);
            
            if (StringUtils.isEmpty(amountCpm)) {
                LOGGER.debug("CPM empty");
                throw new BudgetValidatorException(COMP_CPM_PRICE, KEY_BID_PRICE_REQUIRED);
            } else if (Double.valueOf(amountCpm) < Constants.MIN_BID) {
                LOGGER.debug("CPM smaller than 0.01");
                throw new BudgetValidatorException(COMP_CPM_PRICE, "page.campaign.bidbudget.bidprice.small");
            }
            if (isMonetary(budgetType) && !StringUtils.isEmpty(dailyBudget) && amountCpm != null && Double.valueOf(dailyBudget) < Double.valueOf(amountCpm)) {
                LOGGER.debug("Daily budget smaller than cpm!");
                throw new BudgetValidatorException(COMP_CPM_PRICE, KEY_BID_GREATER_THAN_DAILY_BUDGET);
            }
            if (isMonetary(budgetType) && !StringUtils.isEmpty(overallBudget) && amountCpm != null && Double.valueOf(overallBudget) < Double.valueOf(amountCpm)) {
                LOGGER.debug("Overall budget smaller than cpm!");
                throw new BudgetValidatorException(COMP_CPM_PRICE, KEY_BID_GREATER_THAN_OVERALL_BUDGET);
            }
            // Validation for CPM bid lower than (DATA_FEE + RM_AD_SERVING_FEE)
            // / (1 - Trading_Desk_Margin)
            if (!isValidBidAgainstFees(campaignDto, cpmAmount)) {
                LOGGER.debug("CPM bid lower than (DATA_FEE + RM_AD_SERVING_FEE) / (1 - Trading_Desk_Margin)");
                throw new BudgetValidatorException(COMP_CPM_PRICE, "page.campaign.bidbudget.cpm.low");
            }
            validateBidPriceAgainstAverageMaximumBidBiddingStrategy(campaignDto, COMP_CPM_PRICE, amountCpm, averageMaximumBidEnabled, averageMaximumBidThreshold);
        }
    }

    /**
     * Validate CPC
     * 
     * @throws BudgetValidatorException
     */
    private static void validateCpc(CampaignDto campaignDto, String bidType, String cpc, String dailyBud, String overallBud,
    		boolean averageMaximumBidEnabled, BigDecimal averageMaximumBidThreshold) {
        if (getCpcSelected(bidType)) {
            String budgetType = campaignDto.getBudType();
            String amountCpc = roundPrice(campaignDto, cpc);
            String dailyBudget = roundPrice(campaignDto, dailyBud);
            String overallBudget = roundPrice(campaignDto, overallBud);
            
            if (StringUtils.isEmpty(amountCpc)) {
                LOGGER.debug("CPC empty");
                throw new BudgetValidatorException(COMP_CPC_PRICE, KEY_BID_PRICE_REQUIRED);
            } else if (Double.valueOf(amountCpc) < Constants.MIN_BID) {
                LOGGER.debug("CPC smaller than 0.01");
                throw new BudgetValidatorException(COMP_CPC_PRICE, "page.campaign.bidbudget.bidprice.small");
            }
            if (isMonetary(budgetType) && !StringUtils.isEmpty(dailyBudget) && amountCpc != null && Double.valueOf(dailyBudget) < Double.valueOf(amountCpc)) {
                LOGGER.debug("Daily budget smaller than cpc!");
                throw new BudgetValidatorException(COMP_CPC_PRICE, KEY_BID_GREATER_THAN_DAILY_BUDGET);
            }
            if (isMonetary(budgetType) && !StringUtils.isEmpty(overallBudget) && amountCpc != null && Double.valueOf(overallBudget) < Double.valueOf(amountCpc)) {
                LOGGER.debug("Overall budget smaller than cpc!");
                throw new BudgetValidatorException(COMP_CPC_PRICE, KEY_BID_GREATER_THAN_OVERALL_BUDGET);
            }
            validateBidPriceAgainstAverageMaximumBidBiddingStrategy(campaignDto, COMP_CPC_PRICE, amountCpc, averageMaximumBidEnabled, averageMaximumBidThreshold);
        }
    }

    /**
     * Validate Ad Serving Fee
     * 
     * @throws BudgetValidatorException
     */
    private static void validateAdServincCpmFee(CampaignDto campaignDto) {
        BigDecimal adServingCpmFee = readAdServingCpmFee(campaignDto);
        
        double fee = 0;
        if (adServingCpmFee != null) {
            fee = adServingCpmFee.doubleValue();
        }
        if (fee < 0) {
            LOGGER.debug("Fee less than 0");
            throw new BudgetValidatorException("ad-serving-cpm-fee", "page.campaign.bidbudget.adservfee.value");
        }
    }
    
    /**
     * Validate Bid Price against Average Maximum Bid Bidding Strategy threshold
     * 
     * @throws BudgetValidatorException
     */
    private static void validateBidPriceAgainstAverageMaximumBidBiddingStrategy(CampaignDto campaignDto, String componentId, String amount,
    		boolean averageMaximumBidEnabled, BigDecimal averageMaximumBidThreshold) {
    	if (averageMaximumBidEnabled) {
	    	
	    	if (averageMaximumBidThreshold != null && averageMaximumBidThreshold.doubleValue() < Double.valueOf(amount)) {
	    		throw new BudgetValidatorException(componentId, KEY_BID_GREATER_THAN_THRESHOLD, averageMaximumBidThreshold.setScale(2).toString());
	    	}
    	}
    }
    
    // Budget prices read methods

    public static BigDecimal readFixedMargin(CampaignDto campaignDto, AccountFixedMarginDto accountFixedMarginDto) {
        BigDecimal fixedMargin = null; 
        
        if ((campaignDto.getCurrentTradingDeskMargin() != null) && 
            ((accountFixedMarginDto==null) || (campaignDto.getCurrentTradingDeskMargin().getTradingDeskMargin().compareTo(accountFixedMarginDto.getMargin())!=0)) && 
            (campaignDto.getCurrentTradingDeskMargin().getTradingDeskMargin().doubleValue() != 0.0)){
            fixedMargin = campaignDto.getCurrentTradingDeskMargin().getTradingDeskMargin().multiply(BIG_DECIMAL_100);
        }
        
        return fixedMargin;
    }

    public static BigDecimal readAdServingCpmFee(CampaignDto campaignDto) {
        if (campaignDto.getCurrentRichMediaAdServingFee() != null && campaignDto.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee().doubleValue() != 0.0) {
            return campaignDto.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee();
        } else {
            return null;
        }
    }

    public static String readBidType(CampaignDto campaignDto) {
        String bidType = campaignDto.getCurrentBid().getBidTypeStr();
        
        if (StringUtils.isEmpty(bidType)) {
            LOGGER.debug("Empty bid type");
            bidType = CPC;
        }
        return bidType;
    }
    
    public static String readAmountCpx(CampaignDto campaignDto) {
        String amount = getValueAmount(campaignDto.getCurrentBid().getAmount(), campaignDto.isPriceOverridden());
        String bidType = readBidType(campaignDto);
        
        String amountCpx = "";
        if (isCPI(bidType) || isCPA(bidType)) {
            amountCpx = "";
            if (campaignDto.getCurrentBid().getAmount() == null) {
                amountCpx = "";
            } else {
                amountCpx = String.valueOf(amount);
            }
        }
        return amountCpx;
    }

    public static String readAmountCpc(CampaignDto campaignDto) {
        String amount = getValueAmount(campaignDto.getCurrentBid().getAmount(), campaignDto.isPriceOverridden());
        String bidType = readBidType(campaignDto);
        
        String amountCpc = "";
        if (isCpc(bidType)) {
            if (campaignDto.getCurrentBid().getAmount() == null) {
                amountCpc = "";
            } else {
                amountCpc = String.valueOf(amount);
            }
        } else if (isCpm(bidType)) {
            amountCpc = "";
        }
        return amountCpc;
    }

    public static String readAmountCpm(CampaignDto campaignDto) {
        String amount = getValueAmount(campaignDto.getCurrentBid().getAmount(), campaignDto.isPriceOverridden());
        String bidType = readBidType(campaignDto);
        
        String amountCpm = "";
        if (isCpc(bidType)) {
            amountCpm = "";
        } else if (isCpm(bidType)) {
            if (campaignDto.getCurrentBid().getAmount() == null) {
                amountCpm = "";
            } else {
                amountCpm = String.valueOf(amount);
            }
        }
        return amountCpm;
    }
    
    public static String readOverallBudget(CampaignDto campaignDto) {
        String overallBudget = "";
        String budgetType = campaignDto.getBudType();
        if (isMonetary(budgetType)) {
            if (campaignDto.getOverallBudget() != null) {
                overallBudget = campaignDto.getOverallBudget().toString();
            } else {
                overallBudget = "";
            }
        } else if (isClick(budgetType)) {
            if (campaignDto.getOverallBudgetClicks() != null) {
                overallBudget = campaignDto.getOverallBudgetClicks().toString();
            } else {
                overallBudget = "";
            }
        } else if (isImpression(budgetType)) {
            if (campaignDto.getOverallBudgetImpressions() != null) {
                overallBudget = campaignDto.getOverallBudgetImpressions().toString();
            } else {
                overallBudget = "";
            }
        }
        return overallBudget;
    }

    public static String readDailyBudget(CampaignDto campaignDto) {
        String dailyBudget = "";
        String budgetType = campaignDto.getBudType();
        if (isMonetary(budgetType)) {
            if (campaignDto.getDailyBudget() != null) {
                dailyBudget = campaignDto.getDailyBudget().toString();
            } else {
                dailyBudget = "";
            }
        } else if (isClick(budgetType)) {
            if (campaignDto.getDailyBudgetClicks() != null) {
                dailyBudget = campaignDto.getDailyBudgetClicks().toString();
            } else {
                dailyBudget = "";
            }
        } else if (isImpression(budgetType)) {
            if (campaignDto.getDailyBudgetImpressions() != null) {
                dailyBudget = campaignDto.getDailyBudgetImpressions().toString();
            } else {
                dailyBudget = "";
            }
        }
        return dailyBudget;
    }
    
    // Budget prices save methods
    
    public static void writeCurrentBid(CampaignDto campaignDto, String amountCpc, String amountCpm, String amountCpx) {
        String bidType = readBidType(campaignDto);
        if (isCpc(bidType)) {
            campaignDto.getCurrentBid().setAmount(new BigDecimal(roundPrice(campaignDto, amountCpc)));
        } else if (isCpm(bidType)) {
            campaignDto.getCurrentBid().setAmount(new BigDecimal(roundPrice(campaignDto, amountCpm)));
        } else if (isCpx(bidType)) {
            campaignDto.getCurrentBid().setAmount(new BigDecimal(roundPrice(campaignDto, amountCpx)));
        }
    }
    
    public static void writeFixedMargin(CampaignDto campaignDto, BigDecimal fixedMargin, AccountFixedMarginDto accountFixedMarginDto) {
        if ((fixedMargin != null) &&
            !((accountFixedMarginDto!=null) && (fixedMargin.compareTo(accountFixedMarginDto.getMargin())==0))) {
            if (campaignDto.getCurrentTradingDeskMargin() == null) {
                campaignDto.setCurrentTradingDeskMargin(new CampaignTradingDeskMarginDto());
                campaignDto.getCurrentTradingDeskMargin().setStartDate(new Date());
            }
            campaignDto.getCurrentTradingDeskMargin().setTradingDeskMargin(fixedMargin.divide(BIG_DECIMAL_100));
        }else {
            if (campaignDto.getCurrentTradingDeskMargin() == null) {
                campaignDto.setCurrentTradingDeskMargin(new CampaignTradingDeskMarginDto());
                campaignDto.getCurrentTradingDeskMargin().setStartDate(new Date());
            }
            campaignDto.getCurrentTradingDeskMargin().setTradingDeskMargin(null);
        }
    }
    
    public static void writeAdServingCpmFee(CampaignDto campaignDto, BigDecimal adServingCpmFee) {
        if (adServingCpmFee != null) {
            if (campaignDto.getCurrentRichMediaAdServingFee() == null) {
                campaignDto.setCurrentRichMediaAdServingFee(new CampaignRichMediaAdServingFeeDto());
                campaignDto.getCurrentRichMediaAdServingFee().setStartDate(new Date());
            }
            campaignDto.getCurrentRichMediaAdServingFee().setRichMediaAdServingFee(adServingCpmFee);
        } else {
            if (campaignDto.getCurrentRichMediaAdServingFee() == null) {
                campaignDto.setCurrentRichMediaAdServingFee(new CampaignRichMediaAdServingFeeDto());
                campaignDto.getCurrentRichMediaAdServingFee().setStartDate(new Date());
            }
            campaignDto.getCurrentRichMediaAdServingFee().setRichMediaAdServingFee(BIG_DECIMAL_0);
        }
    }
    
    public static void writeOverallBudget(CampaignDto campaignDto, String budgetType, String overallBud) {
        String overallBudget = roundPrice(campaignDto, overallBud);
      //Check even ditribution
        if(isNoCap(overallBud) && campaignDto.isEvenDistributionOverallBudget()){
            campaignDto.setEvenDistributionOverallBudget(false);
        }
        if (isMonetary(budgetType)) {
            if (!isNoCap(overallBudget)) {
                campaignDto.setOverallBudget(new BigDecimal(overallBudget));
            } else {
                campaignDto.setOverallBudget(null);
                campaignDto.setEvenDistributionOverallBudget(false);
                // If there is no daily budget daily even distribution must be false
                if(campaignDto.getDailyBudget()==null){
                    campaignDto.setEvenDistributionDailyBudget(false);
                }
            }
            campaignDto.setOverallBudgetClicks(null);
            campaignDto.setOverallBudgetImpressions(null);
        } else if (isClick(budgetType)) {
            if (!isNoCap(overallBudget)) {
                campaignDto.setOverallBudgetClicks(new BigDecimal(overallBudget));
                campaignDto.setOverallBudgetClicks(new BigDecimal(campaignDto.getOverallBudgetClicks().toBigInteger()));
            } else {
                campaignDto.setOverallBudgetClicks(null);
                campaignDto.setEvenDistributionOverallBudget(false);
                // If there is no daily budget daily even distribution must be false
                if(campaignDto.getDailyBudgetClicks()==null){
                    campaignDto.setEvenDistributionDailyBudget(false);
                }
            }
            campaignDto.setOverallBudget(null);
            campaignDto.setOverallBudgetImpressions(null);
        } else if (isImpression(budgetType)) {
            if (!isNoCap(overallBudget)) {
                campaignDto.setOverallBudgetImpressions(new BigDecimal(overallBudget));
                campaignDto.setOverallBudgetImpressions(new BigDecimal(campaignDto.getOverallBudgetImpressions().toBigInteger()));
            } else {
                campaignDto.setOverallBudgetImpressions(null);
                campaignDto.setEvenDistributionOverallBudget(false);
                // If there is no daily budget daily even distribution must be false
                if(campaignDto.getDailyBudgetConversions()==null){
                    campaignDto.setEvenDistributionDailyBudget(false);
                }
            }
            campaignDto.setOverallBudgetClicks(null);
            campaignDto.setOverallBudget(null);
        }
        
    }
    
    public static void writeDailyBudget(CampaignDto campaignDto, String budgetType, String dailyBud) {
        String dailyBudget = roundPrice(campaignDto, dailyBud);
        if (isMonetary(budgetType)) {
            if (!isNoCap(dailyBudget)) {
                campaignDto.setDailyBudget(new BigDecimal(dailyBudget));
            } else {
                campaignDto.setDailyBudget(null);
            }
            campaignDto.setDailyBudgetClicks(null);
            campaignDto.setDailyBudgetImpressions(null);
        } else if (isClick(budgetType)) {
            if (!isNoCap(dailyBudget)) {
                campaignDto.setDailyBudgetClicks(new BigDecimal(dailyBudget));
                campaignDto.setDailyBudgetClicks(new BigDecimal(campaignDto.getDailyBudgetClicks().toBigInteger()));
            } else {
                campaignDto.setDailyBudgetClicks(null);
            }
            campaignDto.setDailyBudget(null);
            campaignDto.setDailyBudgetImpressions(null);
        } else if (isImpression(budgetType)) {
            if (!isNoCap(dailyBudget)) {
                campaignDto.setDailyBudgetImpressions(new BigDecimal(dailyBudget));
                campaignDto.setDailyBudgetImpressions(new BigDecimal(campaignDto.getDailyBudgetImpressions().toBigInteger()));
            } else {
                campaignDto.setDailyBudgetImpressions(null);
            }
            campaignDto.setDailyBudgetClicks(null);
            campaignDto.setDailyBudget(null);
        }
        //Check even ditribution
        if(isNoCap(dailyBudget) &&  !campaignDto.isEvenDistributionOverallBudget()){
            campaignDto.setEvenDistributionDailyBudget(false);
        }
    }
    
    /**
     * Price is no cap only if it is empty or zero.
     * 
     * @param price daily budget or overall budget for instance
     * @return true in case of no cap
     */
    public static boolean isNoCap(String price) {
        return (StringUtils.isEmpty(price) || Double.valueOf(price).intValue() == 0) ? true : false;
    }
    
    public static boolean isCpm(String bidType) {
        return bidType.equals(CPM);
    }
    
    public static boolean isCpc(String bidType) {
        return bidType.equals(CPC);
    }
    
    // Helper methods
    
    private static boolean isMonetary(String budgetType) {
        return budgetType.equals(MONETARY);
    }
    
    private static boolean isImpression(String budgetType) {
        return budgetType.equals(IMPRESSION);
    }
    
    private static boolean isClick(String budgetType) {
        return budgetType.equals(CLICK);
    }
    
    private static boolean isCPA(String bidType) {
        return bidType.equals(CPA);
    }
    
    private static boolean isCPI(String bidType) {
        return bidType.equals(CPI);
    }
    
    private static boolean isCpx(String bidType) {
        return isCPA(bidType) || isCPI(bidType);
    }
    
    private static String getValueAmount(BigDecimal amount, boolean isPriceOverridden) {
        if (amount == null) {
            return "";
        }
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(INT_2);
        format.setMaximumFractionDigits(isPriceOverridden ? INT_3 : INT_2);
        return format.format(amount.doubleValue());
    }
    
    private static boolean getCpxSelected(String bidType) {
        return !getCpcSelected(bidType) && !getCpmSelected(bidType);
    }

    private static boolean isValidBidAgainstFees(CampaignDto campaignDto, String cpmAmount) {
        BigDecimal margin = BIG_DECIMAL_0;
        BigDecimal dataFee = BIG_DECIMAL_0;
        BigDecimal adRMFee = BIG_DECIMAL_0;

        CampaignDataFeeDto currentDataFee = campaignDto.getCurrentDataFee();
        if (currentDataFee != null && currentDataFee.getDataFee() != null) {
            dataFee = currentDataFee.getDataFee();
        }
        BigDecimal fixedMargin = readFixedMargin(campaignDto, null);
        if (fixedMargin != null) {
            margin = fixedMargin.divide(BIG_DECIMAL_100);
        }
        
        BigDecimal adServingCpmFee = readAdServingCpmFee(campaignDto);
        if (adServingCpmFee != null) {
            adRMFee = adServingCpmFee;
        }
        if (margin.equals(BigDecimal.ONE)) {
            return true;
        }
        BigDecimal value = (dataFee.add(adRMFee).divide(BigDecimal.ONE.subtract(margin), 2, RoundingMode.HALF_UP));
        // amountCpm < value
        if (BigDecimal.valueOf(Double.valueOf(cpmAmount)).compareTo(value) == -1) {
            return false;
        }
        return true;
    }
    
    private static String roundPrice(CampaignDto campaignDto, String price) {
        return (!StringUtils.isEmpty(price)) ? roundString(price, campaignDto.isPriceOverridden()) : price;
    }
    
    private static String roundString(String amount, boolean isPriceOverridden) {
        if (!amount.contains(".")) {
            return amount;
        }

        DecimalFormat decFormat = new DecimalFormat("#.##");
        if (isPriceOverridden) {
            decFormat = new DecimalFormat("#.###");
        }
        decFormat.setRoundingMode(RoundingMode.HALF_UP);
        Double d = Double.valueOf(decFormat.format(Double.valueOf(amount)));
        return d.toString();
    }
    
    private static boolean getCpcSelected(String bidType) {
        return (bidType != null && CPC.equals(bidType)) ? true : false;
    }

    private static boolean getCpmSelected(String bidType) {
        return (bidType != null && CPM.equals(bidType)) ? true : false;
    }

}
