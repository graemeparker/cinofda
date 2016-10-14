package com.adfonic.webservices.view;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.springframework.web.servlet.View;

import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.Publication;

public abstract class BaseAbstractView implements View {
    protected static final ThreadLocal<DecimalFormat> CURRENCY_FORMAT = new ThreadLocal<DecimalFormat>() {
        public DecimalFormat initialValue() {
            return new DecimalFormat("######0.00");
        }
    };

    protected static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM-dd-yyyy HH:mm");
        }
    };
 
    protected static final ThreadLocal<DecimalFormat> PERCENTAGE_FORMAT = new ThreadLocal<DecimalFormat>() {
        public DecimalFormat initialValue() {
            return new DecimalFormat("######0.00");
        }
    };

    protected int makeInt(Object obj) {
        int value = 0;
        if (obj instanceof Number) {
            value = ((Number) obj).intValue();
        }
        return value;
    }
    
    protected double makeDouble(Object obj) {
    	double value = 0.0;
    	if(obj instanceof Number) {
            value = ((Number) obj).doubleValue();
    	}
    	return value;
    }

    protected String formattedBudget(BigDecimal budget) {
        return formattedBudget(budget, BudgetType.MONETARY);
    }

    protected String formattedBudget(BigDecimal budget, BudgetType budgetType) {
        return budget == null ? "" : budgetType == BudgetType.MONETARY ? CURRENCY_FORMAT.get().format(budget) : budget.toString();
    }

    protected String getDisclosedPublicationName(Publication publication) {
        if (publication == null) return "";
        if (publication.isDisclosed()) {
            String friendlyName = publication.getFriendlyName();
            return (friendlyName == null) ? publication.getName() : friendlyName;
        } else {
            return publication.getExternalID();
        }
    }
}
