package com.adfonic.tools.validator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.campaign.bid.CampaignBidMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.security.SecurityUtils;

public class MinBidValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        List<String> roles = new ArrayList<String>(0);
        roles.add(Constants.LOGGED_IN_AS_ADMIN_ROLE);
        Double val;
        String componentId = componet.getId();
        boolean isMinBidActive = true;
        if (SecurityUtils.hasUserRoles(roles) || componentId.equals("cpx-price")) {
            isMinBidActive = false;
        }
        if (value instanceof Long) {
            val = round(Double.valueOf((Long) value));
        } else {
            val = round((Double) value);
        }

        if (isMinBidActive && !isValid(val, context, "cpm-price".equals(componentId) ? false : true)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.minbidprice", "");

            throw new ValidatorException(fm);
        }
    }

    private boolean isValid(Double value, FacesContext context, boolean isCpc) {
        if (value != null) {
            CampaignBidMBean cBean = Utils.findBean(context, Constants.CAMPAIGN_BID_BEAN);
            // BigDecimal bidMin = null;
            if (isCpc) {
                // cBean.getCampaignDto().getCurrentBid().setAmountCpc(null);
                cBean.getCampaignDto().getCurrentBid().setBidType("CPC");
                // bidMin = cBean.getBidMinCpc();
            } else {
                // cBean.getCampaignDto().getCurrentBid().setAmountCpm(null);
                cBean.getCampaignDto().getCurrentBid().setBidType("CPM");
                // bidMin = cBean.getBidMinCpm();
            }
            // if(bidMin!=null && bidMin.doubleValue()<=value.doubleValue()) {
            // return true;
            // }else{
            // return false;
            // }
        }
        return false;
    }

    private Double round(Double amount) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        twoDForm.setRoundingMode(RoundingMode.HALF_UP);
        Double d = Double.valueOf(twoDForm.format(amount));
        return d;
    }
}
