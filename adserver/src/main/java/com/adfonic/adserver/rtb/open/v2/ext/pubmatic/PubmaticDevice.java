package com.adfonic.adserver.rtb.open.v2.ext.pubmatic;

public class PubmaticDevice extends com.adfonic.adserver.rtb.open.v2.Device {

    private DeviceExt ext;

    public DeviceExt getExt() {
        return ext;
    }

    public void setExt(DeviceExt ext) {
        this.ext = ext;
    }

    //https://developer.pubmatic.com/documentation/device-id-parameter
    public static class DeviceExt {

        private String idfa;
        private String androidadvid;
        private String androidid;
        private String openudid;
        private String otherdeviceid;

        public String getIdfa() {
            return idfa;
        }

        public void setIdfa(String idfa) {
            this.idfa = idfa;
        }

        public String getAndroidadvid() {
            return androidadvid;
        }

        public void setAndroidadvid(String androidadvid) {
            this.androidadvid = androidadvid;
        }

        public String getAndroidid() {
            return androidid;
        }

        public void setAndroidid(String androidid) {
            this.androidid = androidid;
        }

        public String getOpenudid() {
            return openudid;
        }

        public void setOpenudid(String openudid) {
            this.openudid = openudid;
        }

        public String getOtherdeviceid() {
            return otherdeviceid;
        }

        public void setOtherdeviceid(String otherdeviceid) {
            this.otherdeviceid = otherdeviceid;
        }

    }

}
