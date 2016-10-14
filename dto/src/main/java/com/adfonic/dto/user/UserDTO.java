package com.adfonic.dto.user;

import java.util.Collection;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.domain.User;
import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.publisher.PublisherDto;

public class UserDTO extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source(value = "email")
    protected String email;

    @Source(value = "formattedEmail")
    protected String formattedEmail;

    @Source(value = "firstName")
    protected String firstName;

    @Source(value = "lastName")
    protected String lastName;

    @DTOCascade
    @Source(value = "company")
    protected CompanyDto company;

    @DTOTransient
    protected Collection<AdvertiserDto> advertiserListDto;

    @DTOTransient
    protected AdvertiserDto advertiserDto;

    @DTOTransient
    protected PublisherDto publisherDto;

    @Source(value = "phoneNumber")
    protected String phoneNumber;

    @Source(value = "status")
    protected String status;

    @Source(value = "password")
    protected String password;

    @Source(value = "company.accountTypeFlags")
    protected Integer accountTypeFlags;

    @DTOCascade
    @Source(value = "country")
    protected CountryDto country;

    @Source(value = "developerKey")
    private String developerKey;

    @Source(value = "alias")
    private String alias;

    @DTOTransient
    protected String userType;

    @DTOTransient
    private List<String> userTypes;

    @DTOTransient
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Integer getAccountTypeFlags() {
        return accountTypeFlags;
    }

    public void setAccountTypeFlags(Integer accountTypeFlags) {
        this.accountTypeFlags = accountTypeFlags;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFormattedEmail() {
        return formattedEmail;
    }

    public void setFormattedEmail(String formattedEmail) {
        this.formattedEmail = formattedEmail;
    }

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

    public CompanyDto getCompany() {
        return company;
    }

    public void setCompany(CompanyDto company) {
        this.company = company;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AdvertiserDto getAdvertiserDto() {
        return advertiserDto;
    }

    public void setAdvertiserDto(AdvertiserDto advertiserDto) {
        this.advertiserDto = advertiserDto;
    }

    public PublisherDto getPublisherDto() {
        return publisherDto;
    }

    public void setPublisherDto(PublisherDto publisherDto) {
        this.publisherDto = publisherDto;
    }

    public Collection<AdvertiserDto> getAdvertiserListDto() {
        return advertiserListDto;
    }

    public void setAdvertiserListDto(Collection<AdvertiserDto> advertiserListDto) {
        this.advertiserListDto = advertiserListDto;
    }

    public List<String> getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(List<String> userTypes) {
        this.userTypes = userTypes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DTOTransient
    public String getFormattedName() {
        return firstName + " " + lastName + " " + email;
    }

    public CountryDto getCountry() {
        return country;
    }

    public void setCountry(CountryDto country) {
        this.country = country;
    }

    public String getDeveloperKey() {
        return developerKey;
    }

    public void setDeveloperKey(String developerKey) {
        this.developerKey = developerKey;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserDTO [email=");
        builder.append(email);
        builder.append(", formattedEmail=");
        builder.append(formattedEmail);
        builder.append(", firstName=");
        builder.append(firstName);
        builder.append(", lastName=");
        builder.append(lastName);
        builder.append(", company=");
        builder.append(company);
        builder.append(", advertiserDto=");
        builder.append(advertiserDto);
        builder.append(", phoneNumber=");
        builder.append(phoneNumber);
        builder.append(", status=");
        builder.append(status);
        builder.append(", accountTypeFlags=");
        builder.append(accountTypeFlags);
        builder.append(", userType=");
        builder.append(userType);
        builder.append(", developerKey=");
        builder.append(developerKey);
        builder.append(", alias=");
        builder.append(alias);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
