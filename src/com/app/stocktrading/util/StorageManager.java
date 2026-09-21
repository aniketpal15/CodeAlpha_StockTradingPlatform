package com.app.stocktrading.util;

import com.app.stocktrading.model.Holding;
import com.app.stocktrading.model.Portfolio;
import com.app.stocktrading.model.Transaction;
import com.app.stocktrading.model.User;

import java.io.*;

/**
 * Manages file persistence for user portfolios and transaction histories.
 */
public final class StorageManager {

    private StorageManager() {}

    /**
     * Serializes User state (including Portfolio, holdings, and transactions) to a binary file.
     */
    public static void saveUserState(User user, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(user);
        }
    }

    /**
     * Deserializes User state from a binary file.
     */
    public static User loadUserState(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (User) ois.readObject();
        }
    }

    /**
     * Exports active holdings to a CSV file.
     */
    public static void exportHoldingsToCSV(Portfolio portfolio, File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("Ticker,Shares,AverageBuyPrice,TotalCostBasis");
            bw.newLine();
            for (Holding h : portfolio.getHoldings().values()) {
                bw.write(String.format("%s,%d,%.2f,%.2f",
                        h.getTicker(), h.getShares(), h.getAverageBuyPrice(), h.getTotalCostBasis()));
                bw.newLine();
            }
        }
    }

    /**
     * Exports complete transaction log to a CSV file.
     */
    public static void exportTransactionsToCSV(Portfolio portfolio, File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("TransactionID,Timestamp,Type,Ticker,Shares,PricePerShare,TotalAmount,CashBalanceAfter,RealizedPnL");
            bw.newLine();
            for (Transaction t : portfolio.getTransactions()) {
                bw.write(String.format("%s,%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f",
                        t.getId(), TradingUtils.formatDateTime(t.getTimestamp()),
                        t.getType(), t.getTicker(), t.getShares(), t.getPricePerShare(),
                        t.getTotalAmount(), t.getCashBalanceAfter(), t.getRealizedGainLoss()));
                bw.newLine();
            }
        }
    }
}
