package com.adfonic.tasks.xaudit;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;

public interface ExternalApprovalService {

    String newCreative(Creative creative, Publisher publisher);

    AppNexusCreativeRecord getAppNexusCreative(String externalReference);
    
    void updateCreative(String externalReference, Creative creative, Publisher publisher);
    
    String checkForAnyCreativeIncompatibility(Creative creative);
}
