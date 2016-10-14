package com.adfonic.tools.beans.campaign.inventory;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("view")
public class CategoriesBlackListMBean extends CampaignPublicationListMBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4188436945608530254L;

    private boolean hasSelectedList = false;

    public boolean isHasSelectedList() {
        if (this.isListSelected()) {
            return true;
        }
        return hasSelectedList;
    }

    public void setHasSelectedList(boolean hasSelectedList) {
        this.hasSelectedList = hasSelectedList;
    }
}
