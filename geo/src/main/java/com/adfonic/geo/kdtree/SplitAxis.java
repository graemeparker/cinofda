package com.adfonic.geo.kdtree;

public enum SplitAxis {
    LATITUDE_AXIS, 
    LONGITUDE_AXIS;

    public static SplitAxis getAxisFromDeep(int deep){
        return deep % 2 == 0 ? LATITUDE_AXIS : LONGITUDE_AXIS;
    }
}
