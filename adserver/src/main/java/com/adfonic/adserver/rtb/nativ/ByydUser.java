package com.adfonic.adserver.rtb.nativ;

import com.adfonic.util.Range;

public class ByydUser {

    public static final ByydUser EMPTY = new ByydUser();

    private String uid;

    // There are multiple ways in which age can be conveyed...we support any
    // and all, since each RTB exchange may handle it slightly differently.

    // This is akin to Parameters.DATE_OF_BIRTH, either yyyy or yyyyMMdd
    private String dateOfBirth;

    // This is akin to Parameters.AGE
    private Integer age;

    // This is akin to Parameters.AGE_LOW and Parameters.AGE_HIGH
    private Range<Integer> ageRange;

    // This is akin to Parameters.GENDER
    private String gender;

    // This is akin to Parameters.POSTAL_CODE
    private String postalCode;

    // This is akin to Parameters.COUNTRY_CODE
    private String countryCode;

    // This is akin to Parameters.STATE
    private String state;

    // This is akin to Parameters.DMA, either the name or numeric id
    private String dma;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Range<Integer> getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(Range<Integer> ageRange) {
        this.ageRange = ageRange;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDma() {
        return dma;
    }

    public void setDma(String dma) {
        this.dma = dma;
    }
}
