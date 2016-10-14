package com.adfonic.reporting.sql.dto.gen;

import java.io.Serializable;
import java.lang.reflect.Field;

public class BasicPubSideStatsDto implements Serializable, OrderedStatistics {

    private static final long serialVersionUID = 1L;

    private long requests;
    private long impressions;
    private long clicks;
    private double payout;


    public void setRequests(long requests) {
        this.requests = requests;
    }


    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }


    public void setClicks(long clicks) {
        this.clicks = clicks;
    }


    public void setPayout(double payout) {
        this.payout = payout;
    }
    

    /* (non-Javadoc)
     * @see com.adfonic.reporting.sql.dto.gen.OrderedStatistics#asObjectArray()
     */
    @Override
    public Object[] asObjectArray() {
        return new Object[] { requests, impressions, clicks, payout };
    }
    
    @Override
    public Object getStatisticByName(String name) {
        try {
            Field field = BasicAdvSideStatsDto.class.getDeclaredField(name);
            return field.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException  e) {
            e.printStackTrace();
        }
        return null;
    }

}
