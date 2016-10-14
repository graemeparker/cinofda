package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="ROLE")
public class Role extends BusinessKey implements Named {
	
    private static final long serialVersionUID = 6L;

    public static final String USER_ROLE_USER							= "User";
    public static final String USER_ROLE_ADVERTISER						= "Advertiser";
    public static final String USER_ROLE_PUBLISHER						= "Publisher";
    public static final String USER_ROLE_AGENCY							= "Agency";
    public static final String USER_ROLE_ADMINISTRATOR					= "Administrator";
    public static final String USER_ROLE_SUPER_USER						= "SuperUser";
    public static final String USER_ROLE_PREMIUM_ADVERTISER				= "PremiumAdvertiser";
    public static final String USER_ROLE_TRANSPARENT_ADVERTISER_BETA	= "TransparentAdvertiserBeta";
    public static final String USER_ROLE_MAY_VIEW_PRICING				= "MayViewPricing";
    public static final String USER_ROLE_BACKFILL_DISABLED				= "BackfillDisabled";

    public static final String COMPANY_ROLE_PREMIUM						= "Premium";
    public static final String COMPANY_ROLE_CHANNELS					= "Channels";
    public static final String COMPANY_ROLE_DSP                         = "Dsp";
    public static final String COMPANY_ROLE_MOPUBINVENTORY              = "MoPubInventorySrc";
    public static final String COMPANY_ROLE_TOOLS2             		    = "Tools2";
    public static final String COMPANY_ROLE_DSPLIC                      = "DspLic";
    public static final String COMPANY_ROLE_DSP_READ_ONLY               = "DspReadOnly";
    public static final String COMPANY_ROLE_CATEGORY                    = "Category";
    public static final String COMPANY_ROLE_WEVE                        = "Weve";
    public static final String COMPANY_ROLE_PREPAY                      = "PrePay";
    public static final String COMPANY_ROLE_EDIT_AGENCY_DISCOUNT        = "EditAgencyDiscount";
    public static final String COMPANY_ROLE_SHOW_AGENCY_DISCOUNT        = "ShowAgencyDiscount";
    public static final String COMPANY_ROLE_THIRD_PARTY_TAGS			= "ThirdPartyTags";
    public static final String COMPANY_ROLE_THIRD_PARTY_IMPR_TRACKERS	= "ThirdPartyImpressionTrackers";
    public static final String COMPANY_ROLE_BETA_TEST                   = "BetaTest";
    public static final String COMPANY_ROLE_FIXED_MARGIN                = "FixedMargin";
    public static final String COMPANY_ROLE_AD_SERVING_CPM_FEE          = "AdServingCPMFee";
    
    public enum RoleType { USER, COMPANY }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="ROLE_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    Role() {}

    public Role(String name, RoleType roleType) {
    this.name = name;
        this.roleType = roleType;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RoleType getRoleType() {
        return roleType;
    }
    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }
}
