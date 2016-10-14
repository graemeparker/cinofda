package com.adfonic.presentation.reporting.model;

import java.util.List;

/**
 * Class that groups the information detail of the totals row
 * 
 * Responsibility: To provide information regarding the totals columns
 * 
 * @author David Martin
 */
public class TotalsDetails {

    /** Generic style to use in the totals row */
    private Style totalStyle;
    
    /** Totals row details */
    private List<Total> totals;
    
    public TotalsDetails(Style totalStyle, List<Total> totals){
        this.totals = totals;
        this.totalStyle = totalStyle;
    }

    public Style getTotalStyle() {
        return totalStyle;
    }

    public List<Total> getTotals() {
        return totals;
    }
}
