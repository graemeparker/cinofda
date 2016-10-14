package com.adfonic.dto.resultwrapper;

import java.io.Serializable;
import java.util.List;

public class DeviceIdsValidated implements Serializable {

    private static final long serialVersionUID = 1L;

    private long devicesRead;

    private long devicesValidated;

    private List<String> idsValidated;

    public long getDevicesRead() {
        return devicesRead;
    }

    public void setDevicesRead(long devicesRead) {
        this.devicesRead = devicesRead;
    }

    public long getDevicesValidated() {
        return devicesValidated;
    }

    public void setDevicesValidated(long devicesValidated) {
        this.devicesValidated = devicesValidated;
    }

    public List<String> getIdsValidated() {
        return idsValidated;
    }

    public void setIdsValidated(List<String> idsValidated) {
        this.idsValidated = idsValidated;
    }
}
