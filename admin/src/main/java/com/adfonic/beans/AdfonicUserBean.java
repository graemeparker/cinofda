package com.adfonic.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.adfonic.domain.AdfonicUser;

@RequestScoped
@ManagedBean
public class AdfonicUserBean extends BaseBean {
    private AdfonicUser adfonicUser;

    public AdfonicUserBean() {
        adfonicUser = adfonicUser();
    }

    public AdfonicUser getAdfonicUser() {
        return adfonicUser;
    }
}
