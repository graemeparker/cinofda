package com.adfonic.tasks.xaudit;

import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;

public interface ApprovalServiceManager<T extends BaseExternalCreative> {

    public String postCreative(T creative);

    public T getCreative(String reference);

    //public void updateCreative(ExternalCreativeId reference, T creative);

    public boolean deleteCreative(String reference);

    public AppNexusCreativeRecord updateCreative(String reference, AppNexusCreativeRecord creative);
}
