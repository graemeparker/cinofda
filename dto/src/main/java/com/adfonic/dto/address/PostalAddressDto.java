package com.adfonic.dto.address;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.country.CountryDto;

public class PostalAddressDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source("firstName")
    private String firstName;
    @Source("lastName")
    private String lastName;
    @Source("address1")
    private String address1;
    @Source("address2")
    private String address2;
    @Source("city")
    private String city;
    @Source("state")
    private String state;
    @Source("postcode")
    private String postcode;
    @DTOCascade
    @Source("country")
    private CountryDto country;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public CountryDto getCountry() {
        return country;
    }

    public void setCountry(CountryDto country) {
        this.country = country;
    }

}
