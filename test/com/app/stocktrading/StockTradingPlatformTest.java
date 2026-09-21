package com.app.stocktrading;

import com.app.stocktrading.model.*;
import com.app.stocktrading.util.StorageManager;
import com.app.stocktrading.util.TradingUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Automated test suite for Stock Trading Platform (Task 2).
 * Runs standalone without third-party dependencies.
 */
public class StockTradingPlatformTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("          RUNNING STOCK TRADING PLATFORM TEST SUITE             ");
        System.out.println("==========================================================================");

        testStockModel();
        testStockPriceFluctuations();
        testHoldingWeightedAverage();
        testPortfolioBuyOrder();
        testPortfolioInsufficientFunds();
        testPortfolioSellOrderAndRealizedPnL();
        testPortfolioInsufficientShares();
        testPortfolioDepositAndWithdrawal();
        testPortfolioValuationMetrics();
        testMarketEngineOperations();
        testSerializationPersistence();
        testCsvExport();

        System.out.println("==========================================================================");
        System.out.printf("Test Suite Completed: %d Passed | %d Failed\n", testsPassed, testsFailed);
        System.out.println("==========================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println(" [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println(" [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void assertEquals(String testName, double expected, double actual, double delta) {
        boolean match = Math.abs(expected - actual) <= delta;
        if (match) {
            System.out.printf(" [PASS] %s (expected=%.2f, actual=%.2f)\n", testName, expected, actual);
            testsPassed++;
        } else {
            System.err.printf(" [FAIL] %s (expected=%.2f, actual=%.2f)\n", testName, expected, actual);
            testsFailed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        boolean match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (match) {
            System.out.println(" [PASS] " + testName + " (matched: " + actual + ")");
            testsPassed++;
        } else {
            System.err.println(" [FAIL] " + testName + " (expected=" + expected + ", actual=" + actual + ")");
            testsFailed++;
        }
    }

    private static void testStockModel() {
        Stock stock = new Stock("TEST", "Test Corporation", 150.00);
        assertEquals("Stock ticker uppercase normalization", "TEST", stock.getTicker());
        assertEquals("Stock name assignment", "Test Corporation", stock.getName());
        assertEquals("Stock initial price", 150.00, stock.getCurrentPrice(), 0.001);
        assertEquals("Stock initial high equals price", 150.00, stock.getDayHigh(), 0.001);
        assertEquals("Stock initial low equals price", 150.00, stock.getDayLow(), 0.001);

        stock.updatePrice(165.00);
        assertEquals("Stock updated price", 165.00, stock.getCurrentPrice(), 0.001);
        assertEquals("Stock new day high", 165.00, stock.getDayHigh(), 0.001);
        assertEquals("Stock price change amount", 15.00, stock.getChangeAmount(), 0.001);
        assertEquals("Stock price change percent", 10.00, stock.getChangePercent(), 0.001);

        stock.updatePrice(140.00);
        assertEquals("Stock new day low", 140.00, stock.getDayLow(), 0.001);
    }

    private static void testStockPriceFluctuations() {
        Stock stock = new Stock("VOL", "Volatile Inc.", 100.00);
        for (int i = 0; i < 10; i++) {
            stock.simulateFluctuation(3.0);
        }
        assertTrue("Stock price remains positive after fluctuations", stock.getCurrentPrice() > 0.0);
        assertTrue("Price history maintains entries", stock.getPriceHistory().size() > 1);
    }

    private static void testHoldingWeightedAverage() {
        // Buy 10 shares @ $100 -> Cost $1000
        Holding holding = new Holding("AAPL", 10, 100.00);
        assertEquals("Holding initial shares", 10, holding.getShares());
        assertEquals("Holding initial avg buy price", 100.00, holding.getAverageBuyPrice(), 0.001);
        assertEquals("Holding cost basis", 1000.00, holding.getTotalCostBasis(), 0.001);

        // Buy 10 more shares @ $200 -> Total cost $3000 for 20 shares -> Avg $150
        holding.addShares(10, 200.00);
        assertEquals("Holding updated shares", 20, holding.getShares());
        assertEquals("Holding weighted avg buy price", 150.00, holding.getAverageBuyPrice(), 0.001);
        assertEquals("Holding updated cost basis", 3000.00, holding.getTotalCostBasis(), 0.001);

        // Sell 5 shares
        holding.removeShares(5);
        assertEquals("Holding shares after selling 5", 15, holding.getShares());
        assertEquals("Holding avg price remains unchanged after sell", 150.00, holding.getAverageBuyPrice(), 0.001);
    }

    private static void testPortfolioBuyOrder() {
        Portfolio portfolio = new Portfolio(5000.00);
        Stock msft = new Stock("MSFT", "Microsoft", 200.00);

        Transaction txn = portfolio.buyStock(msft, 10);
        assertEquals("Cash deducted after buy order (5000 - 2000)", 3000.00, portfolio.getCashBalance(), 0.001);
        assertEquals("Shares owned of MSFT", 10, portfolio.getSharesOwned("MSFT"));
        assertEquals("Transaction type BUY", Transaction.Type.BUY, txn.getType());
        assertEquals("Transaction total amount", 2000.00, txn.getTotalAmount(), 0.001);
    }

    private static void testPortfolioInsufficientFunds() {
        Portfolio portfolio = new Portfolio(100.00);
        Stock expensive = new Stock("BRK", "Berkshire", 500.00);

        boolean exceptionThrown = false;
        try {
            portfolio.buyStock(expensive, 1);
        } catch (IllegalStateException ex) {
            exceptionThrown = true;
        }
        assertTrue("Insufficient funds throws IllegalStateException", exceptionThrown);
        assertEquals("Cash balance intact after failed buy", 100.00, portfolio.getCashBalance(), 0.001);
    }

    private static void testPortfolioSellOrderAndRealizedPnL() {
        Portfolio portfolio = new Portfolio(5000.00);
        Stock nvda = new Stock("NVDA", "Nvidia Corp", 100.00);

        // Buy 10 shares @ $100 (Cash becomes $4000)
        portfolio.buyStock(nvda, 10);

        // Price rises to $150
        nvda.updatePrice(150.00);

        // Sell 6 shares: proceeds = 6 * 150 = 900. Cost basis = 6 * 100 = 600. Realized gain = +300.
        Transaction sellTxn = portfolio.sellStock(nvda, 6);
        assertEquals("Cash balance after sell (4000 + 900)", 4900.00, portfolio.getCashBalance(), 0.001);
        assertEquals("Remaining shares of NVDA", 4, portfolio.getSharesOwned("NVDA"));
        assertEquals("Realized P&L on sell transaction", 300.00, sellTxn.getRealizedGainLoss(), 0.001);
        assertEquals("Total cumulative portfolio realized P&L", 300.00, portfolio.getTotalRealizedPnL(), 0.001);

        // Sell remaining 4 shares @ $80 (loss)
        nvda.updatePrice(80.00);
        Transaction lossTxn = portfolio.sellStock(nvda, 4);
        // proceeds = 4 * 80 = 320. Cost basis = 4 * 100 = 400. Realized loss = -80.
        assertEquals("Realized P&L on loss transaction", -80.00, lossTxn.getRealizedGainLoss(), 0.001);
        assertEquals("Position closed, 0 shares remaining", 0, portfolio.getSharesOwned("NVDA"));
        assertTrue("Holding removed from active portfolio", portfolio.getHolding("NVDA") == null);
        assertEquals("Net portfolio realized P&L (300 - 80)", 220.00, portfolio.getTotalRealizedPnL(), 0.001);
    }

    private static void testPortfolioInsufficientShares() {
        Portfolio portfolio = new Portfolio(2000.00);
        Stock goog = new Stock("GOOGL", "Alphabet", 100.00);
        portfolio.buyStock(goog, 5);

        boolean exceptionThrown = false;
        try {
            portfolio.sellStock(goog, 10); // owns only 5
        } catch (IllegalStateException ex) {
            exceptionThrown = true;
        }
        assertTrue("Selling more shares than owned throws IllegalStateException", exceptionThrown);
    }

    private static void testPortfolioDepositAndWithdrawal() {
        Portfolio portfolio = new Portfolio(1000.00);
        portfolio.deposit(500.00);
        assertEquals("Cash after deposit", 1500.00, portfolio.getCashBalance(), 0.001);

        portfolio.withdraw(300.00);
        assertEquals("Cash after withdrawal", 1200.00, portfolio.getCashBalance(), 0.001);

        boolean overdrawn = false;
        try {
            portfolio.withdraw(5000.00);
        } catch (IllegalStateException ex) {
            overdrawn = true;
        }
        assertTrue("Overdraft throws IllegalStateException", overdrawn);
    }

    private static void testPortfolioValuationMetrics() {
        MarketEngine market = new MarketEngine();
        Portfolio portfolio = new Portfolio(10000.00);

        Stock aapl = market.getStock("AAPL");
        double initialAaplPrice = aapl.getCurrentPrice();
        portfolio.buyStock(aapl, 10);

        double expectedHoldingsValue = 10 * initialAaplPrice;
        assertEquals("Holdings market value calculation",
                expectedHoldingsValue, portfolio.getTotalHoldingsMarketValue(market.getAllStocks()), 0.05);

        assertEquals("Total portfolio value equals initial capital upon entry",
                10000.00, portfolio.getTotalPortfolioValue(market.getAllStocks()), 0.05);
    }

    private static void testMarketEngineOperations() {
        MarketEngine market = new MarketEngine();
        Map<String, Stock> stocks = market.getAllStocks();
        assertTrue("Default market contains 8 stocks", stocks.size() >= 8);
        assertTrue("Contains AAPL", market.getStock("AAPL") != null);
        assertTrue("Contains NVDA", market.getStock("NVDA") != null);

        market.tickMarket();
        assertTrue("Market tick updates prices", market.getStock("AAPL").getCurrentPrice() > 0.0);
    }

    private static void testSerializationPersistence() {
        User user = new User("USR-TEST", "Samantha Jones", 15000.00);
        MarketEngine market = new MarketEngine();
        user.getPortfolio().buyStock(market.getStock("AAPL"), 5);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("app_test_user", ".dat");
            tempFile.deleteOnExit();

            StorageManager.saveUserState(user, tempFile);
            assertTrue("Binary user state file created", tempFile.exists() && tempFile.length() > 0);

            User restored = StorageManager.loadUserState(tempFile);
            assertEquals("Restored user ID matches", "USR-TEST", restored.getUserId());
            assertEquals("Restored user name matches", "Samantha Jones", restored.getName());
            assertEquals("Restored cash balance matches",
                    user.getPortfolio().getCashBalance(), restored.getPortfolio().getCashBalance(), 0.01);
            assertEquals("Restored shares of AAPL", 5, restored.getPortfolio().getSharesOwned("AAPL"));
        } catch (Exception e) {
            assertTrue("Serialization failed with exception: " + e.getMessage(), false);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private static void testCsvExport() {
        User user = new User("USR-CSV", "Bob Builder", 8000.00);
        MarketEngine market = new MarketEngine();
        user.getPortfolio().buyStock(market.getStock("AAPL"), 10);
        user.getPortfolio().sellStock(market.getStock("AAPL"), 3);

        File holdingsCsv = null;
        File transactionsCsv = null;
        try {
            holdingsCsv = File.createTempFile("holdings_test", ".csv");
            transactionsCsv = File.createTempFile("txns_test", ".csv");
            holdingsCsv.deleteOnExit();
            transactionsCsv.deleteOnExit();

            StorageManager.exportHoldingsToCSV(user.getPortfolio(), holdingsCsv);
            StorageManager.exportTransactionsToCSV(user.getPortfolio(), transactionsCsv);

            List<String> holdingLines = Files.readAllLines(holdingsCsv.toPath());
            List<String> txnLines = Files.readAllLines(transactionsCsv.toPath());

            assertTrue("Holdings CSV has header and data", holdingLines.size() >= 2);
            assertTrue("Holdings CSV contains AAPL", holdingLines.get(1).contains("AAPL"));
            assertTrue("Transactions CSV has header and entries", txnLines.size() >= 3);
        } catch (IOException e) {
            assertTrue("CSV export failed with exception: " + e.getMessage(), false);
        } finally {
            if (holdingsCsv != null && holdingsCsv.exists()) holdingsCsv.delete();
            if (transactionsCsv != null && transactionsCsv.exists()) transactionsCsv.delete();
        }
    }
}
