package com.app.stocktrading.model;

import com.app.stocktrading.util.TradingUtils;

import java.io.Serializable;

/**
 * Represents a user's position in a specific stock equity.
 * Maintains quantity of shares and weighted average purchase cost.
 */
public class Holding implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String ticker;
    private int shares;
    private double averageBuyPrice;

    public Holding(String ticker, int shares, double averageBuyPrice) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be empty.");
        }
        if (shares <= 0) {
            throw new IllegalArgumentException("Initial shares must be positive.");
        }
        if (averageBuyPrice <= 0.0) {
            throw new IllegalArgumentException("Average buy price must be positive.");
        }

        this.ticker = ticker.trim().toUpperCase();
        this.shares = shares;
        this.averageBuyPrice = TradingUtils.round(averageBuyPrice, 2);
    }

    public String getTicker() {
        return ticker;
    }

    public int getShares() {
        return shares;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    /**
     * Adds more shares and updates the weighted average purchase cost basis.
     */
    public void addShares(int additionalShares, double executionPrice) {
        if (additionalShares <= 0) {
            throw new IllegalArgumentException("Shares added must be positive.");
        }
        double currentTotalCost = this.shares * this.averageBuyPrice;
        double addedCost = additionalShares * executionPrice;
        this.shares += additionalShares;
        this.averageBuyPrice = TradingUtils.round((currentTotalCost + addedCost) / this.shares, 2);
    }

    /**
     * Reduces shares when selling.
     */
    public void removeShares(int sharesSold) {
        if (sharesSold <= 0 || sharesSold > this.shares) {
            throw new IllegalArgumentException("Invalid shares quantity to remove.");
        }
        this.shares -= sharesSold;
    }

    public double getTotalCostBasis() {
        return TradingUtils.round(shares * averageBuyPrice, 2);
    }

    public double getMarketValue(double currentPrice) {
        return TradingUtils.round(shares * currentPrice, 2);
    }

    public double getUnrealizedPnL(double currentPrice) {
        return TradingUtils.round(getMarketValue(currentPrice) - getTotalCostBasis(), 2);
    }

    public double getUnrealizedReturnPercent(double currentPrice) {
        if (getTotalCostBasis() == 0.0) return 0.0;
        return TradingUtils.round((getUnrealizedPnL(currentPrice) / getTotalCostBasis()) * 100.0, 2);
    }

    @Override
    public String toString() {
        return String.format("%s: %d shares @ avg %s (Cost Basis: %s)",
                ticker, shares, TradingUtils.formatCurrency(averageBuyPrice),
                TradingUtils.formatCurrency(getTotalCostBasis()));
    }
}
