package com.adfonic.domain.cache.dto.adserver;

import java.io.Serializable;

public class EcpmInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private double expectedRevenue;
	private double bidPrice;
	private double expectedProfit;
	private double expectedSettlementPrice;
	private double winningProbability;
	private double weight;

    public EcpmInfo() {
        reset();
    }

    public void reset(){
		expectedRevenue = 0;
		bidPrice = 0;
		expectedProfit = 0;
		expectedSettlementPrice = 0;
		winningProbability = 1.0;
		weight = 0;
	}

	public double getExpectedRevenue() {
		return expectedRevenue;
	}

	public void setExpectedRevenue(double expectedRevenue) {
		this.expectedRevenue = expectedRevenue;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(double bidPrice) {
		this.bidPrice = bidPrice;
	}

	public double getExpectedProfit() {
		return expectedProfit;
	}

	public void setExpectedProfit(double expectedProfit) {
		this.expectedProfit = expectedProfit;
	}

	public double getExpectedSettlementPrice() {
		return expectedSettlementPrice;
	}

	public void setExpectedSettlementPrice(double expectedSettlementPrice) {
		this.expectedSettlementPrice = expectedSettlementPrice;
	}

	public double getWinningProbability() {
		return winningProbability;
	}

	public void setWinningProbability(double winningProbability) {
		this.winningProbability = winningProbability;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EcpmInfo ecpmInfo = (EcpmInfo) o;

        if (Double.compare(ecpmInfo.bidPrice, bidPrice) != 0) return false;
        if (Double.compare(ecpmInfo.expectedProfit, expectedProfit) != 0) return false;
        if (Double.compare(ecpmInfo.expectedRevenue, expectedRevenue) != 0) return false;
        if (Double.compare(ecpmInfo.expectedSettlementPrice, expectedSettlementPrice) != 0) return false;
        if (Double.compare(ecpmInfo.weight, weight) != 0) return false;
        if (Double.compare(ecpmInfo.winningProbability, winningProbability) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = expectedRevenue != +0.0d ? Double.doubleToLongBits(expectedRevenue) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = bidPrice != +0.0d ? Double.doubleToLongBits(bidPrice) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = expectedProfit != +0.0d ? Double.doubleToLongBits(expectedProfit) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = expectedSettlementPrice != +0.0d ? Double.doubleToLongBits(expectedSettlementPrice) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = winningProbability != +0.0d ? Double.doubleToLongBits(winningProbability) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = weight != +0.0d ? Double.doubleToLongBits(weight) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "EcpmInfo{" +
                "expectedRevenue=" + expectedRevenue +
                ", bidPrice=" + bidPrice +
                ", expectedProfit=" + expectedProfit +
                ", expectedSettlementPrice=" + expectedSettlementPrice +
                ", winningProbability=" + winningProbability +
                ", weight=" + weight +
                '}';
    }
}
