package com.adfonic.geo.postalcode;

import com.adfonic.geo.Coordinates;

public interface GeneralPostalCode extends Coordinates {
    String getPostalCode();

    String getProvince();

    String getCountryCode();

}
