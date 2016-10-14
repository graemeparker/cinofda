package com.adfonic.reporting.sql.dto.gen;

import java.io.Serializable;

public class StatisticsBasicDto extends AbstractTaggedDto implements Tagged, Serializable {

    private static final long serialVersionUID = 1L;

    private OrderedStatistics statistics;


    public StatisticsBasicDto(Tag tag) {
        super(tag);
    }


    public OrderedStatistics getStatistics() {
        return statistics;
    }


    public void setStatistics(OrderedStatistics statistics) {
        this.statistics = statistics;
    }


}
