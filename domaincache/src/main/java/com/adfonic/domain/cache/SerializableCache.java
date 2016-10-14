package com.adfonic.domain.cache;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface SerializableCache extends java.io.Serializable {

    // TODO return list of counts/values instead of logging it
    @Deprecated
    void logCounts(String description, Logger logger, Level level);

}
