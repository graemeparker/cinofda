package com.adfonic.webservices.service.impl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.webservices.service.IRestrictingCopyService;
import com.adfonic.webservices.service.IRestrictor;

/*
 * TODO - make IRestrictor ones value objects; currently will create/destroy small objects in processing thread 
 */
@Service
public class RestrictingCopyService<T, D> extends AbstractCopyService<T, D> implements IRestrictingCopyService<T, D>, Cloneable {

    public Set<IRestrictor> restrictionSet;

    public RestrictingCopyService() {

    }

    public IRestrictingCopyService<T, D> restrictOnCampaignStatus(Campaign.Status campaignStatus) {
        return (restrict(new CampaignStatusRestrictor(campaignStatus)));
    }

    public IRestrictingCopyService<T, D> restrictOnCreativeStatus(Creative.Status creativeStatus) {
        return (restrict(new CreativeStatusRestrictor(creativeStatus)));
    }

    private IRestrictingCopyService<T, D> restrict(IRestrictor restrictor) {
        RestrictingCopyService candidate = this;
        if (restrictionSet == null) {
            try {
                candidate = (RestrictingCopyService) this.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            candidate.restrictionSet = new HashSet<IRestrictor>();
        }

        candidate.restrictionSet.add(restrictor);
        return (candidate);
    }

    public boolean isRestricted(Field field) {
        for (IRestrictor restrictor : restrictionSet != null ? restrictionSet : Collections.<IRestrictor> emptySet()) {
            if (restrictor.isRestricted(field)) {
                return (true);
            }
        }
        return (false);
    }

}
