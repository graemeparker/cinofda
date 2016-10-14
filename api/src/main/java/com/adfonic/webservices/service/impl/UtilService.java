package com.adfonic.webservices.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.adfonic.domain.Company;
import com.adfonic.domain.Role;
import com.adfonic.webservices.exception.ValidationException;
import com.adfonic.webservices.service.IUtilService;
import com.adfonic.webservices.util.DspAccess;

/*
 * Util service
 */
@Service
public class UtilService implements IUtilService {

    public IUtilService validatePresence(String name, Object value) {
        if (hasNoPresence(value)) {
            throw new ValidationException("Expected a valid " + name + "!");
        }

        return this;
    }


    public boolean hasNoPresence(Object value) {
        return value == null || value instanceof String && ((String) value).isEmpty();
    }


    // stop gap stuff incase 2 DSP roles are involved. domain doesn't guarantee anything
    public Role getEffectiveDspRole(Set<Role> roles) {
        Role effeciveRole = null;
        for (Role role : roles) {
            if (role.getName().equals(Role.COMPANY_ROLE_DSP)) {
                return role;
            } else if (role.getName().equals(Role.COMPANY_ROLE_DSPLIC)) {
                effeciveRole = role;
            }
        }

        return effeciveRole;
    }


    @Override
    public DspAccess getEffectiveDspAccess(Company company) {
        return company.hasTechFee() ? DspAccess.RTB : DspAccess.ALL;
    }

}
