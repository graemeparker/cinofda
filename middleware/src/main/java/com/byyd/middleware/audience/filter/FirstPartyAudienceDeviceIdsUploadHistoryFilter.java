package com.byyd.middleware.audience.filter;

import com.adfonic.domain.FirstPartyAudience;
import com.byyd.middleware.iface.dao.LikeSpec;

public class FirstPartyAudienceDeviceIdsUploadHistoryFilter {

    private FirstPartyAudience firstPartyAudience;
    
    private String filename;
    private LikeSpec filenameLikeSpec;
    private boolean filenameCaseSensitive;
    
    public FirstPartyAudienceDeviceIdsUploadHistoryFilter setFilename(String filename, boolean nameCaseSensitive) {
        return this.setFilename(filename, null, nameCaseSensitive);
    }
    
    public FirstPartyAudienceDeviceIdsUploadHistoryFilter setFilename(String filename, LikeSpec filenameLikeSpec, boolean filenameCaseSensitive) {
        this.filename = filename;
        this.filenameLikeSpec = filenameLikeSpec;
        this.filenameCaseSensitive = filenameCaseSensitive;
        return this;
    }
    public LikeSpec getFilenameLikeSpec() {
        return filenameLikeSpec;
    }
    public boolean isFilenameCaseSensitive() {
        return filenameCaseSensitive;
    }

    public String getFilename() {
        return filename;
    }

    public FirstPartyAudience getFirstPartyAudience() {
        return firstPartyAudience;
    }

    public FirstPartyAudienceDeviceIdsUploadHistoryFilter setFirstPartyAudience(FirstPartyAudience firstPartyAudience) {
        this.firstPartyAudience = firstPartyAudience;
        return this;
    }


}
