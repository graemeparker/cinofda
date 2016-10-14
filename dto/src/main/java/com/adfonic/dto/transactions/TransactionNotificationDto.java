package com.adfonic.dto.transactions;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class TransactionNotificationDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source("amount")
    private BigDecimal amount;

    @Source("reference")
    private String reference;

    @Source("timestamp")
    private Date timestamp;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = (timestamp == null ? null : new Date(timestamp.getTime()));
    }
}
