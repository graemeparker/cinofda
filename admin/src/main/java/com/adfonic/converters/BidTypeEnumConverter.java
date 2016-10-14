package com.adfonic.converters;

import javax.faces.convert.EnumConverter;
import javax.faces.convert.FacesConverter;

import com.adfonic.domain.BidType;

@FacesConverter(value="bidTypeConverter")
public class BidTypeEnumConverter extends EnumConverter {

    public BidTypeEnumConverter() {
        super(BidType.class);
    }
}
