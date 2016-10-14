package com.adfonic.domain.cache.dto.adserver.creative;

import java.io.Serializable;
import java.math.BigDecimal;

public class DmpSelectorDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;

    private String externalId;

    // Factual "audience" audiences are per-exchange, null for everybody else
    private Long publisherId;

    private BigDecimal price;

    public DmpSelectorDto() {
        // default
    }

    public DmpSelectorDto(long id, String externalId, BigDecimal price) {
        this(id, externalId, price, null);
    }

    public DmpSelectorDto(long id, String externalId, BigDecimal price, Long publisherId) {
        this.id = id;
        this.externalId = externalId;
        this.price = price;
        this.publisherId = publisherId;
    }

    public String getExternalId() {
        return externalId;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "DmpSelectorDto { id=" + id + ", externalId=" + externalId + ", price=" + price + (publisherId != null ? ", publisherId=" + publisherId : "") + "}";
    }

}
