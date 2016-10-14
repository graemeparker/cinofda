package com.adfonic.domain.cache.dto.datacollector.publication;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class PublisherDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private PublisherRevShareDto currentPublisherRevShare;
    private List<PublisherRevShareDto> revShareHistory = new ArrayList<PublisherRevShareDto>();
    private Long accountId;
    private CompanyDto company;
    private BigDecimal buyerPremium;

    public PublisherRevShareDto getCurrentPublisherRevShare() {
        return currentPublisherRevShare;
    }

    public void setCurrentPublisherRevShare(PublisherRevShareDto currentPublisherRevShare) {
        this.currentPublisherRevShare = currentPublisherRevShare;
    }

    public List<PublisherRevShareDto> getRevShareHistory() {
        return revShareHistory;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public CompanyDto getCompany() {
        return company;
    }

    public void setCompany(CompanyDto company) {
        this.company = company;
    }

    public BigDecimal getRevShareForDate(Date date) {
        for (PublisherRevShareDto rs : revShareHistory) {
            if (!date.before(rs.getStartDate())) {
                Date rsEnd = rs.getEndDate();
                if ((rsEnd == null) || date.before(rsEnd)) {
                    return rs.getRevShare();
                }
            }
        }
        return null; // no revshare for that time
    }

    public BigDecimal getBuyerPremium() {
        return buyerPremium;
    }

    public void setBuyerPremium(BigDecimal buyerPremium) {
        this.buyerPremium = buyerPremium;
    }

    @Override
    public String toString() {
        return "PublisherDto {" + getId() + ", currentPublisherRevShare=" + currentPublisherRevShare + ", revShareHistory=" + revShareHistory + ", accountId=" + accountId
                + ", company=" + company + ", buyerPremium=" + buyerPremium + "}";
    }

}
