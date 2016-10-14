package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="POSTAL_ADDRESS")
public class PostalAddress extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="FIRST_NAME",length=50,nullable=true)
    private String firstName;
    @Column(name="LAST_NAME",length=50,nullable=true)
    private String lastName;
    @Column(name="ADDRESS1",length=50,nullable=true)
    private String address1;
    @Column(name="ADDRESS2",length=50,nullable=true)
    private String address2;
    @Column(name="CITY",length=50,nullable=true)
    private String city;
    @Column(name="STATE",length=50,nullable=true)
    private String state;
    @Column(name="POSTCODE",length=50,nullable=true)
    private String postcode;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="COUNTRY_ID",nullable=false)
    private Country country;

    public long getId() { return id; };
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }

    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
}
