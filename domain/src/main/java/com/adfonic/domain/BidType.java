package com.adfonic.domain;

public enum BidType {
    // AD_SERVED/IMPRESSION migration (phase 1)
    CPC(AdAction.CLICK, 1),
    CPM(AdAction.AD_SERVED, 1000),
    CPI(AdAction.INSTALL, 1),
    CPA(AdAction.CONVERSION, 1),
    ;

    private final AdAction adAction;
    private final int quantity;
    
    private BidType(AdAction adAction, int quantity) {
        this.adAction = adAction;
        this.quantity = quantity;
    }

    // This method allows the enum value to be treated like a bean, i.e. in JSF
    public String getName() { return name(); }
    
    public AdAction getAdAction() { return adAction; }

    public int getQuantity() { return quantity; }
}
