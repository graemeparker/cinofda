package com.adfonic.adserver.rtb.open.v2.ext.nexage;

public class NexageDevice extends com.adfonic.adserver.rtb.open.v2.Device {

    private DeviceExt ext;

    public DeviceExt getExt() {
        return ext;
    }

    public void setExt(DeviceExt ext) {
        this.ext = ext;
    }

    public static class DeviceExt {

        //iOS ~ IDFA_RAW, Android ~ ADID_RAW
        private String nex_ifa;

        public String getNex_ifa() {
            return nex_ifa;
        }

        public void setNex_ifa(String nex_ifa) {
            this.nex_ifa = nex_ifa;
        }

    }
}
