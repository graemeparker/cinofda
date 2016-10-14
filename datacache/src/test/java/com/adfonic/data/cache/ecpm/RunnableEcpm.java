package com.adfonic.data.cache.ecpm;

import com.adfonic.data.cache.ecpm.EcpmInputData;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;

public interface RunnableEcpm {
    void getEcpm(EcpmInputData data, EcpmInfo ecpmInfo);
}
