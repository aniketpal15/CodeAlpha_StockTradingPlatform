package com.app.stocktrading.model;

import com.app.stocktrading.util.TradingUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a user's investment portfolio.
 * Holds available cash balance, active stock holdings, and full transaction history.
 */
public class Portfolio implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_INITIAL_BALANCE = 10_000.00;

    private double cashBalance;
    private double initialCapital;
    private final Map<String, Holding> holdings;
    private final ArrayList<Transaction> transactions;

    public Portfolio() {
        this(DEFAULT_INITIAL_BALANCE);
    }

    public Portfolio(double initialBalance) {
        this.cashBalance = TradingUtils.round(initialBalance, 2);
        this.initialCapital = this.cashBalance;
        this.holdings = new LinkedHashMap<>();
        this.transactions = new ArrayList<>();

        // Record initial deposit transaction
        transactions.add(new Transaction(
                Transaction.Type.DEPOSIT, "USD", 1, initialBalance, initialBalance, cashBalance, 0.0));
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public double getInitialCapital() {
        return initialCapital;
    }

    public Map<String, Holding> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Holding getHolding(String ticker) {
        if (ticker == null) return null;
        return holdings.get(ticker.toUpperCase());
    }

    public int getSharesOwned(String ticker) {
        Holding h = getHolding(ticker);
        return h != null ? h.getShares() : 0;
    }

    /**
     * Executes a BUY order for a stock.
     */
    public synchronized Transaction buyStock(Stock stock, int shares) {
        if (stock == null) throw new IllegalArgumentException("Stock cannot be null.");
        if (shares <= 0) throw new IllegalArgumentException("Shares to purchase must be greater than zero.");

        double totalCost = TradingUtils.round(shares * stock.getCurrentPrice(), 2);
        if (totalCost > cashBalance) {
            throw new IllegalStateException(String.format(
                    "Insufficient cash balance. Required: %s, Available: %s",
                    TradingUtils.formatCurrency(totalCost), TradingUtils.formatCurrency(cashBalance)));
        }

        // Deduct cash
        cashBalance = TradingUtils.round(cashBalance - totalCost, 2);

        // Update or create holding
        String ticker = stock.getTicker();
        Holding holding = holdings.get(ticker);
        if (holding == null) {
            holding = new Holding(ticker, shares, stock.getCurrentPrice());
            holdings.put(ticker, holding);
        } else {
            holding.addShares(shares, stock.getCurrentPrice());
        }

        // Record transaction
        Transaction txn = new Transaction(
                Transaction.Type.BUY, ticker, shares, stock.getCurrentPrice(),
                totalCost, cashBalance, 0.0);
        transactions.add(txn);
        return txn;
    }

    /**
     * Executes a SELL order for a stock.
     */
    public synchronized Transaction sellStock(Stock stock, int shares) {
        if (stock == null) throw new IllegalArgumentException("Stock cannot be null.");
        if (shares <= 0) throw new IllegalArgumentException("Shares to sell must be greater than zero.");

        String ticker = stock.getTicker();
        Holding holding = holdings.get(ticker);
        if (holding == null || holding.getShares() < shares) {
            int owned = holding == null ? 0 : holding.getShares();
            throw new IllegalStateException(String.format(
                    "Insufficient shares of %s to sell. Owned: %d, Requested: %d",
                    ticker, owned, shares));
        }

        double totalProceeds = TradingUtils.round(shares * stock.getCurrentPrice(), 2);
        double costBasis = TradingUtils.round(shares * holding.getAverageBuyPrice(), 2);
        double realizedGainLoss = TradingUtils.round(totalProceeds - costBasis, 2);

        // Update cash
        cashBalance = TradingUtils.round(cashBalance + totalProceeds, 2);

        // Update holding
        if (holding.getShares() == shares) {
            holdings.remove(ticker);
        } else {
            holding.removeShares(shares);
        }

        // Record transaction
        Transaction txn = new Transaction(
                Transaction.Type.SELL, ticker, shares, stock.getCurrentPrice(),
                totalProceeds, cashBalance, realizedGainLoss);
        transactions.add(txn);
        return txn;
    }

    /**
     * Deposits additional cash into portfolio.
     */
    public synchronized void deposit(double amount) {
        if (amount <= 0.0) throw new IllegalArgumentException("Deposit amount must be positive.");
        cashBalance = TradingUtils.round(cashBalance + amount, 2);
        initialCapital += amount;
        transactions.add(new Transaction(
                Transaction.Type.DEPOSIT, "USD", 1, amount, amount, cashBalance, 0.0));
    }

    /**
     * Withdraws cash from portfolio if available.
     */
    public synchronized void withdraw(double amount) {
        if (amount <= 0.0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > cashBalance) {
            throw new IllegalStateException(String.format(
                    "Insufficient funds. Requested: %s, Available: %s",
                    TradingUtils.formatCurrency(amount), TradingUtils.formatCurrency(cashBalance)));
        }
        cashBalance = TradingUtils.round(cashBalance - amount, 2);
        transactions.add(new Transaction(
                Transaction.Type.WITHDRAWAL, "USD", 1, amount, amount, cashBalance, 0.0));
    }

    /**
     * Computes the total current market value of all stock holdings.
     */
    public double getTotalHoldingsMarketValue(Map<String, Stock> market) {
        double total = 0.0;
        for (Holding h : holdings.values()) {
            Stock stock = market.get(h.getTicker());
            double price = stock != null ? stock.getCurrentPrice() : h.getAverageBuyPrice();
            total += h.getMarketValue(price);
        }
        return TradingUtils.round(total, 2);
    }

    /**
     * Total portfolio equity = Cash + Current Market Value of Holdings.
     */
    public double getTotalPortfolioValue(Map<String, Stock> market) {
        return TradingUtils.round(cashBalance + getTotalHoldingsMarketValue(market), 2);
    }

    /**
     * Computes total unrealized profit/loss across active positions.
     */
    public double getTotalUnrealizedPnL(Map<String, Stock> market) {
        double totalCost = 0.0;
        double totalMarket = 0.0;
        for (Holding h : holdings.values()) {
            totalCost += h.getTotalCostBasis();
            Stock s = market.get(h.getTicker());
            double price = s != null ? s.getCurrentPrice() : h.getAverageBuyPrice();
            totalMarket += h.getMarketValue(price);
        }
        return TradingUtils.round(totalMarket - totalCost, 2);
    }

    /**
     * Computes total realized profit/loss from all past sell orders.
     */
    public double getTotalRealizedPnL() {
        double total = 0.0;
        for (Transaction txn : transactions) {
            if (txn.getType() == Transaction.Type.SELL) {
                total += txn.getRealizedGainLoss();
            }
        }
        return TradingUtils.round(total, 2);
    }

    /**
     * Computes overall portfolio return percentage against initial capital.
     */
    public double getOverallReturnPercent(Map<String, Stock> market) {
        if (initialCapital == 0.0) return 0.0;
        double netGain = getTotalPortfolioValue(market) - initialCapital;
        return TradingUtils.round((netGain / initialCapital) * 100.0, 2);
    }

    /**
     * Resets the portfolio to a fresh state.
     */
    public synchronized void reset(double startingBalance) {
        this.cashBalance = TradingUtils.round(startingBalance, 2);
        this.initialCapital = this.cashBalance;
        this.holdings.clear();
        this.transactions.clear();
        transactions.add(new Transaction(
                Transaction.Type.DEPOSIT, "USD", 1, startingBalance, startingBalance, cashBalance, 0.0));
    }
}
