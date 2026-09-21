package com.app.stocktrading.model;

import com.app.stocktrading.util.TradingUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a publicly traded stock equity.
 * Tracks current price, daily fluctuations, historical prices, and volume.
 */
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String ticker;
    private final String name;
    private double currentPrice;
    private double previousClosePrice;
    private double dayHigh;
    private double dayLow;
    private long volume;
    private final ArrayList<Double> priceHistory;

    private static final Random RANDOM = new Random();

    public Stock(String ticker, String name, double initialPrice) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
        if (initialPrice <= 0.0) {
            throw new IllegalArgumentException("Initial price must be greater than zero.");
        }

        this.ticker = ticker.trim().toUpperCase();
        this.name = name.trim();
        this.currentPrice = TradingUtils.round(initialPrice, 2);
        this.previousClosePrice = this.currentPrice;
        this.dayHigh = this.currentPrice;
        this.dayLow = this.currentPrice;
        this.volume = 100_000 + RANDOM.nextInt(500_000);

        this.priceHistory = new ArrayList<>();
        this.priceHistory.add(this.currentPrice);
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getPreviousClosePrice() {
        return previousClosePrice;
    }

    public double getDayHigh() {
        return dayHigh;
    }

    public double getDayLow() {
        return dayLow;
    }

    public long getVolume() {
        return volume;
    }

    public List<Double> getPriceHistory() {
        return new ArrayList<>(priceHistory);
    }

    public double getChangeAmount() {
        return TradingUtils.round(currentPrice - previousClosePrice, 2);
    }

    public double getChangePercent() {
        if (previousClosePrice == 0.0) return 0.0;
        return TradingUtils.round(((currentPrice - previousClosePrice) / previousClosePrice) * 100.0, 2);
    }

    /**
     * Updates the stock's price, tracking high, low, and history.
     */
    public void updatePrice(double newPrice) {
        if (newPrice <= 0.01) newPrice = 0.01;
        this.currentPrice = TradingUtils.round(newPrice, 2);

        if (this.currentPrice > this.dayHigh) {
            this.dayHigh = this.currentPrice;
        }
        if (this.currentPrice < this.dayLow) {
            this.dayLow = this.currentPrice;
        }

        this.priceHistory.add(this.currentPrice);
        if (this.priceHistory.size() > 100) {
            this.priceHistory.remove(0);
        }
    }

    /**
     * Simulates realistic price movement based on volatility.
     */
    public void simulateFluctuation(double maxPercentMovement) {
        // Gaussian fluctuation with slight bias toward standard market drifts
        double deltaPercent = (RANDOM.nextGaussian() * (maxPercentMovement / 2.0));
        double priceDelta = currentPrice * (deltaPercent / 100.0);
        double targetPrice = currentPrice + priceDelta;

        if (targetPrice < 1.0) {
            targetPrice = 1.0 + (RANDOM.nextDouble() * 2.0);
        }

        updatePrice(targetPrice);
        this.volume += 1_000 + RANDOM.nextInt(25_000);
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s [%s]",
                ticker, name, TradingUtils.formatCurrency(currentPrice),
                TradingUtils.formatChange(getChangeAmount(), getChangePercent()));
    }
}
