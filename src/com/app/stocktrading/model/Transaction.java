package com.app.stocktrading.model;

import com.app.stocktrading.util.TradingUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an executed buy or sell transaction log entry.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        BUY, SELL, DEPOSIT, WITHDRAWAL
    }

    private final String id;
    private final LocalDateTime timestamp;
    private final Type type;
    private final String ticker;
    private final int shares;
    private final double pricePerShare;
    private final double totalAmount;
    private final double cashBalanceAfter;
    private final double realizedGainLoss;

    public Transaction(Type type, String ticker, int shares, double pricePerShare,
                       double totalAmount, double cashBalanceAfter, double realizedGainLoss) {
        this.id = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.ticker = ticker == null ? "CASH" : ticker.toUpperCase();
        this.shares = shares;
        this.pricePerShare = TradingUtils.round(pricePerShare, 2);
        this.totalAmount = TradingUtils.round(totalAmount, 2);
        this.cashBalanceAfter = TradingUtils.round(cashBalanceAfter, 2);
        this.realizedGainLoss = TradingUtils.round(realizedGainLoss, 2);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public String getTicker() {
        return ticker;
    }

    public int getShares() {
        return shares;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getCashBalanceAfter() {
        return cashBalanceAfter;
    }

    public double getRealizedGainLoss() {
        return realizedGainLoss;
    }

    @Override
    public String toString() {
        String pnlStr = type == Type.SELL ? String.format(" [Realized P&L: %s]", TradingUtils.formatCurrency(realizedGainLoss)) : "";
        return String.format("[%s] %s %d %s @ %s | Total: %s | Cash: %s%s",
                TradingUtils.formatDateTime(timestamp), type, shares, ticker,
                TradingUtils.formatCurrency(pricePerShare),
                TradingUtils.formatCurrency(totalAmount),
                TradingUtils.formatCurrency(cashBalanceAfter), pnlStr);
    }
}
