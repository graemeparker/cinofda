package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.adfonic.util.TimeZoneUtils;

/**
 * Represents a Company that may be a publisher, an advertiser, or both.
 */
@Entity
@Table(name="COMPANY")
public class Company extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 20L;

    public enum AdvertiserCategory {
        LONG_TAIL,
        MANAGED_DIRECT,
        MANAGED_AGENCY,
        EXCHANGE,
        MADISON_MANAGED,
        MADISON_LICENCE;
    };

    public enum PublisherCategory {
        LONG_TAIL,
        MANAGED_EXCLUSIVE,
        MANAGED_MEDIATED,
        SUPPLY_SIDE_PLATFORM,
        AD_NETWORK;
    };

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @Column(name="CREATION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="DEFAULT_TIME_ZONE",length=80,nullable=false)
    private String defaultTimeZone;
    @Column(name="IS_INVOICE_DATE_IN_GMT",nullable=false)    
    private boolean isInvoiceDateInGMT;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_MANAGER_ID",nullable=true)
    private User accountManager;
    @OneToMany(mappedBy="company",fetch=FetchType.LAZY)
    private Set<Advertiser> advertisers;
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(mappedBy="company",fetch=FetchType.LAZY)
    private Publisher publisher;
    @OneToMany(mappedBy="company",fetch=FetchType.LAZY)
    private Set<NotificationFlag> notificationFlags;
    @Column(name="ACCOUNT_TYPE_FLAGS",nullable=false)
    private int accountTypeFlags; // see enum values
    @Column(name="INDIVIDUAL",nullable=false)
    private boolean individual;

    // Advertiser-related
    @Column(name="AUTO_TOPUP_LIMIT",nullable=true)
    private BigDecimal autoTopupLimit; // null if disabled
    @Column(name="AUTO_TOPUP_AMOUNT",nullable=true)
    private BigDecimal autoTopupAmount; // null if disabled
    @Column(name="AUTO_TOPUP_AUTH_TRANSACTION_ID",nullable=true)
    private String autoTopupAuthTransactionId; // null if it hasn't been set up
    @Column(name="CREDIT_LIMIT",nullable=true)
    private BigDecimal creditLimit;
    @Column(name="DISCOUNT",nullable=false)
    private BigDecimal discount;
    @Column(name="POST_PAY_ACTIVATION_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date postPayActivationDate;
    // How many days do they have to pay their invoice, e.g. 14
    @Column(name="POST_PAY_TERM_DAYS",nullable=true)
    private Integer postPayTermDays;

    // If true, all creatives for this company should be treated as backfill,
    // and account balance is irrelevant.
    @Column(name="BACKFILL",nullable=false)
    private boolean backfill;
    @Column(name="ADVERTISER_CATEGORY",nullable=false)
    @Enumerated(EnumType.STRING)
    private AdvertiserCategory advertiserCategory;
    @Column(name="PUBLISHER_CATEGORY",nullable=false)
    @Enumerated(EnumType.STRING)
    private PublisherCategory publisherCategory;

    @Column(name="TAXABLE_ADVERTISER",nullable=false)
    private boolean taxableAdvertiser;
    @Column(name="TAXABLE_PUBLISHER",nullable=false)
    private boolean taxablePublisher;
    @Column(name="TAX_CODE",length=32,nullable=true)
    private String taxCode; // EU VAT registration code
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="COUNTRY_ID",nullable=false)
    private Country country;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PAYMENT_OPTIONS_ID",nullable=true)
    private PaymentOptions paymentOptions;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="AFFILIATE_PROGRAM_ID",nullable=true)
    private AffiliateProgram affiliateProgram;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="LOGO_CONTENT_ID",nullable=true)
    private UploadedContent logo;

    @OneToMany(mappedBy="company",fetch=FetchType.LAZY)
    private Set<User> users;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="COMPANY_ROLE",joinColumns=@JoinColumn(name="COMPANY_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="ROLE_ID",referencedColumnName="ID"))
    private Set<Role> roles;
    
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CURRENT_MEDIA_COST_MARGIN_ID",nullable=true)
    private AdvertiserMediaCostMargin currentMediaCostMargin;

    @NotCopied("This accumulates naturally as media costs margins are created")
    @OneToMany(mappedBy="company",fetch=FetchType.LAZY)
    private Set<AdvertiserMediaCostMargin> historicalMediaCostMargins;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_WHITE_LIST_ID",nullable=true)
    private PublicationList publicationWhiteList;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_BLACK_LIST_ID",nullable=true)
    private PublicationList publicationBlackList;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="COMPANY_RESTRICTED_DMP_VENDOR",joinColumns=@JoinColumn(name="COMPANY_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="DMP_VENDOR_ID",referencedColumnName="ID"))
    private Set<DMPVendor> restrictedDMPVendors;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="COMPANY_WHITELIST_IP_ADDRESS_RANGE",joinColumns=@JoinColumn(name="COMPANY_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="IP_ADDRESS_RANGE_ID",referencedColumnName="ID"))
    private Set<IpAddressRange> ipAddressRanges;

    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    // TODO: change the JoinColumn annotation to nullable=false
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="MARGIN_SHARE_DSP_ID",nullable=true)
    private MarginShareDSP currentMarginShareDSP;

    @NotCopied("This accumulates naturally as media costs margins are created")
    @OneToMany(mappedBy="company",fetch=FetchType.LAZY)
    private Set<MarginShareDSP> historicalMarginShareDSPs;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CURRENT_COMPANY_DIRECT_COST_ID",nullable=true)
    private CompanyDirectCost companyDirectCost;
    
    @Column(name="ENABLE_RTB_BID_SEAT", nullable=false)
    private Boolean  enableRtbBidSeat;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="COMPANY_BID_SEAT",joinColumns=@JoinColumn(name="COMPANY_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="BID_SEAT_ID",referencedColumnName="ID"))
    private Set<BidSeat> companyRtbBidSeats;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_FIXED_MARGIN_ID",nullable=true)
    private AccountFixedMargin currentAccountFixedMargin;

    @NotCopied("This accumulates naturally as account fixed margin are created")
    @OneToMany(mappedBy="company",fetch=FetchType.LAZY)
    private Set<AccountFixedMargin> historicalAccountFixedMargins;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DEFAULT_CURRENCY_EXCHANGE_RATE_ID", nullable=false)
    private CurrencyExchangeRate defaultCurrencyExchangeRate;
    
    {
        this.externalID = UUID.randomUUID().toString();
        this.defaultTimeZone = "GMT-0";
        this.advertisers = new HashSet<Advertiser>();
        this.creationTime = new Date();
        this.discount = BigDecimal.ZERO;
        this.advertiserCategory = AdvertiserCategory.LONG_TAIL;
        this.publisherCategory = PublisherCategory.LONG_TAIL;
        this.users = new HashSet<User>();
        this.roles = new HashSet<Role>();
        this.historicalMediaCostMargins = new HashSet<AdvertiserMediaCostMargin>();
        this.restrictedDMPVendors = new HashSet<DMPVendor>();
        this.ipAddressRanges = new HashSet<IpAddressRange>();
        this.historicalMarginShareDSPs = new HashSet<MarginShareDSP>();
        this.companyRtbBidSeats = new HashSet<BidSeat>();
        this.historicalAccountFixedMargins = new HashSet<AccountFixedMargin>();
        this.enableRtbBidSeat=false;
    }

    Company() {}

    public Company(String name) {
        this.name = name;
    }

    public long getId() { return id; };

    public String getExternalID() { return externalID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TimeZone getDefaultTimeZone() {
        return TimeZoneUtils.getTimeZoneNonBlocking(defaultTimeZone);
    }

    public void setDefaultTimeZone(TimeZone defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone.getID();
    }
    
    public String getDefaultTimeZoneId() {
        return this.defaultTimeZone;
    }

    public void setDefaultTimeZoneId(String id) {
        this.defaultTimeZone = id;
    }
    
    public boolean isInvoiceDateInGMT() {
		return isInvoiceDateInGMT;
	}
    
    public boolean getIsInvoiceDateInGMT(){
    	return isInvoiceDateInGMT;
    }
    
    public void setIsInvoiceDateInGMT(boolean isDateInGMT){
    	isInvoiceDateInGMT = isDateInGMT;
    }

    public User getAccountManager() { return accountManager; }
    public void setAccountManager(User accountManager) {
        this.accountManager = accountManager;
    }

    public Set<Advertiser> getAdvertisers() {
        return advertisers;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public int getAccountTypeFlags() { return accountTypeFlags; }

    public boolean isAccountType(AccountType accountType) {
        return accountType.isSet(accountTypeFlags);
    }

    public void setAccountTypeFlag(AccountType accountType) {
        accountTypeFlags = accountTypeFlags | accountType.bitValue();
    }

    public void clearAccountTypeFlag(AccountType accountType) {
        if (accountType.isSet(accountTypeFlags)) {
            accountTypeFlags -= accountType.bitValue();
        }
    }
    
    public void clearAccountTypeFlags() {
    	accountTypeFlags = 0;
    }

    public BigDecimal getAutoTopupLimit() { return autoTopupLimit; }
    public void setAutoTopupLimit(BigDecimal autoTopupLimit) {
        this.autoTopupLimit = autoTopupLimit;
    }

    public BigDecimal getAutoTopupAmount() { return autoTopupAmount; }
    public void setAutoTopupAmount(BigDecimal autoTopupAmount) {
        this.autoTopupAmount = autoTopupAmount;
    }

    public String getAutoTopupAuthTransactionId() {
        return autoTopupAuthTransactionId;
    }
    public void setAutoTopupAuthTransactionId(String autoTopupAuthTransactionId) {
        this.autoTopupAuthTransactionId = autoTopupAuthTransactionId;
    }

    public boolean isIndividual() { return individual; }
    public void setIndividual(boolean individual) {
        this.individual = individual;
    }

    public Set<NotificationFlag> getNotificationFlags() { return notificationFlags; }
    public void setNotificationFlags(Set<NotificationFlag> flags) { this.notificationFlags = flags; }

    public boolean isTaxableAdvertiser() {
        return taxableAdvertiser;
    }

    public boolean isTaxablePublisher() {
        return taxablePublisher;
    }

    private void calculateTaxable() {
        if (country != null) {
            taxableAdvertiser = TaxUtils.isAdvertiserTaxable(country, taxCode);
            taxablePublisher = TaxUtils.isPublisherTaxable(country, taxCode);
        } else {
            taxableAdvertiser = false;
            taxablePublisher = false;
        }
    }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
        calculateTaxable();
    }

    public Country getCountry() { return country; }
    public void setCountry(Country country) {
        this.country = country;
        calculateTaxable();
    }

    public Date getCreationTime() { return creationTime; }

    public PaymentOptions getPaymentOptions() {
        return paymentOptions;
    }

    public void setPaymentOptions(PaymentOptions paymentOptions) {
        this.paymentOptions = paymentOptions;
    }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public Date getPostPayActivationDate() {
        return postPayActivationDate;
    }
    public void setPostPayActivationDate(Date postPayActivationDate) {
        this.postPayActivationDate = postPayActivationDate;
    }

    // I got sick of seeing this same null check happening everywhere so I'm
    // whipping up a convenience method for it.
    public boolean isPostPay() {
        return postPayActivationDate != null;
    }

    public Integer getPostPayTermDays() { return postPayTermDays; }
    public void setPostPayTermDays(Integer postPayTermDays) {
        this.postPayTermDays = postPayTermDays;
    }

    public boolean isBackfill() { return backfill; }
    public void setBackfill(boolean backfill) {
        this.backfill = backfill;
    }

    public AdvertiserCategory getAdvertiserCategory() { return advertiserCategory; }
    public void setAdvertiserCategory(AdvertiserCategory advertiserCategory) {
        this.advertiserCategory = advertiserCategory;
    }

    public PublisherCategory getPublisherCategory() { return publisherCategory; }
    public void setPublisherCategory(PublisherCategory publisherCategory) {
        this.publisherCategory = publisherCategory;
    }

    public void setAffiliateProgram(AffiliateProgram affiliateProgram) {
        this.affiliateProgram = affiliateProgram;
    }

    public AffiliateProgram getAffiliateProgram() {
        return affiliateProgram;
    }

    public UploadedContent getLogo() {
        return logo;
    }
    public void setLogo(UploadedContent logo) {
        this.logo = logo;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Set<Role> getRoles() {
        return roles;
    }
    public AdvertiserMediaCostMargin getCurrentMediaCostMargin() {
        return currentMediaCostMargin;
    }

    public AdvertiserMediaCostMargin createNewMediaCostMargin(BigDecimal amount, Date date) {
        if(amount==null || amount.equals(new BigDecimal(0))){
            currentMediaCostMargin = null;
        }
        // Archive previous bid
//        if (currentRichMediaAdServingFee != null) {
//            currentRichMediaAdServingFee.setEndDate(date);
//        }
        // Create the new one
        currentMediaCostMargin = new AdvertiserMediaCostMargin(this,amount);
        currentMediaCostMargin.setStartDate(date);
        historicalMediaCostMargins.add(currentMediaCostMargin);
        return currentMediaCostMargin;
    }
    
    public Set<AdvertiserMediaCostMargin> getHistoricalMediaCostMargins() {
        return historicalMediaCostMargins;
    }

    public AdvertiserMediaCostMargin getMediaCostMarginForDate(Date date) {
        for (AdvertiserMediaCostMargin cdf : historicalMediaCostMargins) {
            if (!date.before(cdf.getStartDate())) {
                Date cbEnd = cdf.getEndDate();
                if ((cbEnd == null) || date.before(cbEnd)) {
                    return cdf;
                }
            }
        }
        return null; // no bid for that time
    }

	public PublicationList getPublicationWhiteList() {
		return publicationWhiteList;
	}

	public void setPublicationWhiteList(PublicationList publicationWhiteList) {
		this.publicationWhiteList = publicationWhiteList;
	}

	public PublicationList getPublicationBlackList() {
		return publicationBlackList;
	}

	public void setPublicationBlackList(PublicationList publicationBlackList) {
		this.publicationBlackList = publicationBlackList;
	}

	public Set<DMPVendor> getRestrictedDMPVendors() {
		return restrictedDMPVendors;
	}

	public void setRestrictedDMPVendors(Set<DMPVendor> restrictedDMPVendors) {
		this.restrictedDMPVendors = restrictedDMPVendors;
	}

    public Set<IpAddressRange> getIpAddressRanges() {
        return ipAddressRanges;
    }

    public void setIpAddressRanges(Set<IpAddressRange> ipAddressRanges) {
        this.ipAddressRanges = ipAddressRanges;
    }

	public MarginShareDSP getCurrentMarginShareDSP() {
		return currentMarginShareDSP;
	}

	public BigDecimal getCurrentMarginShareDSPValue() {
		return currentMarginShareDSP.getMargin();
	}

	public void setCurrentMarginShareDSP(MarginShareDSP currentMarginShareDSP) {
		this.currentMarginShareDSP = currentMarginShareDSP;
	}

	public Set<MarginShareDSP> getHistoricalMarginShareDSPs() {
		return historicalMarginShareDSPs;
	}

	public void setHistoricalMarginShareDSPs(
			Set<MarginShareDSP> historicalMarginShareDSPs) {
		this.historicalMarginShareDSPs = historicalMarginShareDSPs;
	}
    
    public MarginShareDSP createNewMarginShareDSP(BigDecimal margin, Date date) {
    	currentMarginShareDSP = new MarginShareDSP(this,margin);
    	currentMarginShareDSP.setStartDate(date);
        historicalMarginShareDSPs.add(currentMarginShareDSP);
        return currentMarginShareDSP;
    }
    
    public boolean hasTechFee(){
        return this.currentMediaCostMargin != null && this.currentMediaCostMargin.getMediaCostMargin().doubleValue()>0;
    }

    public String getRolesAsString() {
        return NamedUtils.namedCollectionToString(roles);
    }

	public CompanyDirectCost getCompanyDirectCost() {
		return companyDirectCost;
	}

	public void setCompanyDirectCost(CompanyDirectCost companyDirectCost) {
		this.companyDirectCost = companyDirectCost;
	}

    public Boolean getEnableRtbBidSeat() {
        return enableRtbBidSeat;
    }

    public void setEnableRtbBidSeat(Boolean enableRtbBidSeat) {
        this.enableRtbBidSeat = enableRtbBidSeat;
    }

    public Set<BidSeat> getCompanyRtbBidSeats() {
        return companyRtbBidSeats;
    }

    public void setCompanyRtbBidSeat(Set<BidSeat> companyRtbBidSeats) {
        this.companyRtbBidSeats = companyRtbBidSeats;
    }
    
    public AccountFixedMargin createNewAccountFixedMargin(BigDecimal margin, Date date) {
        currentAccountFixedMargin = new AccountFixedMargin(this,margin);
        currentAccountFixedMargin.setStartDate(date);
        historicalAccountFixedMargins.add(currentAccountFixedMargin);
        return currentAccountFixedMargin;
    }
    
    public AccountFixedMargin getCurrentAccountFixedMargin() {
        return currentAccountFixedMargin;
    }

    public Set<AccountFixedMargin> getHistoricalAccountFixedMargins() {
        return historicalAccountFixedMargins;
    }

    public void setHistoricalAccountFixedMargins(Set<AccountFixedMargin> historicalAccountFixedMargins) {
        this.historicalAccountFixedMargins = historicalAccountFixedMargins;
    }

    public CurrencyExchangeRate getDefaultCurrencyExchangeRate() {
        return defaultCurrencyExchangeRate;
    }

    public void setDefaultCurrencyExchangeRate(CurrencyExchangeRate defaultCurrencyExchangeRate) {
        this.defaultCurrencyExchangeRate = defaultCurrencyExchangeRate;
    }
}
