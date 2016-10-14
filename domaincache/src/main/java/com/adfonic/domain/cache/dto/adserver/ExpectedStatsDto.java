package com.adfonic.domain.cache.dto.adserver;

import java.io.Serializable;

public class ExpectedStatsDto implements Serializable {

    private static final long serialVersionUID = 3L;
    private double expectedCtr;
    private double expectedCvr;
    private double expectedRgr;
    private double priorOdds;

    public ExpectedStatsDto(double expectedCtr, double expectedCVr, double expectedRgr) {
        super();
        this.expectedCtr = expectedCtr;
        this.expectedCvr = expectedCVr;
        this.expectedRgr = expectedRgr;
        this.priorOdds = expectedRgr / (1 - expectedRgr);
    }

    public double getExpectedCtr() {
        return expectedCtr;
    }

    public double getExpectedCvr() {
        return expectedCvr;
    }

    public double getExpectedRgr() {
        return expectedRgr;
    }

    public double getPriorOdds() {
        return priorOdds;
    }

    public void capMaxRgr(double maxRgr) {
        if (this.expectedRgr > maxRgr) {
            this.expectedRgr = maxRgr;
            this.priorOdds = expectedRgr / (1 - expectedRgr);
        }
    }

    @Override
    public String toString() {
        return "ExpectedStatsDto {expectedCtr=" + expectedCtr + ", expectedCvr=" + expectedCvr + ", expectedRgr=" + expectedRgr + ", priorOdds=" + priorOdds + "}";
    }

}
