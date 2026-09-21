package com.app.stocktrading.cli;

import com.app.stocktrading.model.*;
import com.app.stocktrading.util.StorageManager;
import com.app.stocktrading.util.TradingUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interactive Console Interface for the Stock Trading Platform.
 */
public class ConsoleUI {

    private final MarketEngine market;
    private final User user;
    private final Scanner scanner;

    public ConsoleUI(MarketEngine market, User user) {
        this.market = market;
        this.user = user;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        printBanner();

        while (running) {
            printMenu();
            int choice = readIntPrompt("Choose an option (0-10): ", 0, 10);
            System.out.println();

            switch (choice) {
                case 1 -> handleViewMarket();
                case 2 -> handleBuyStock();
                case 3 -> handleSellStock();
                case 4 -> handleViewPortfolio();
                case 5 -> handleViewTransactions();
                case 6 -> handleDeposit();
                case 7 -> handleWithdraw();
                case 8 -> handleSimulateMarketTick();
                case 9 -> handleExportCSV();
                case 10 -> handleResetPortfolio();
                case 0 -> {
                    System.out.println("Thank you for trading with Stock Trading Platform. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid selection.");
            }

            if (running) {
                pausePrompt();
            }
        }
    }

    private void printBanner() {
        System.out.println("==========================================================================");
        System.out.println("          JAVA PROGRAMMING INTERNSHIP - TASK 2: STOCK TRADING PLATFORM          ");
        System.out.println("       Simulated Financial Exchange | Portfolio Management | Live Analytics ");
        System.out.println("==========================================================================");
        System.out.printf(" Trader: %s | Starting Cash: %s\n",
                user.getName(), TradingUtils.formatCurrency(user.getPortfolio().getCashBalance()));
    }

    private void printMenu() {
        Portfolio p = user.getPortfolio();
        double totalVal = p.getTotalPortfolioValue(market.getAllStocks());
        double pnl = p.getTotalUnrealizedPnL(market.getAllStocks());

        System.out.println();
        System.out.println("+------------------------------------------------------------------------+");
        System.out.printf("| CASH: %-12s | TOTAL VALUE: %-12s | UNREALIZED P&L: %-10s |\n",
                TradingUtils.formatCurrency(p.getCashBalance()),
                TradingUtils.formatCurrency(totalVal),
                TradingUtils.formatCurrency(pnl));
        System.out.println("+------------------------------------------------------------------------+");
        System.out.println("| [1]  View Live Market Quotations                                       |");
        System.out.println("| [2]  Buy Stock (Market Order)                                          |");
        System.out.println("| [3]  Sell Stock (Market Order)                                         |");
        System.out.println("| [4]  View Portfolio & Performance Breakdown                            |");
        System.out.println("| [5]  View Transaction Execution History                                |");
        System.out.println("| [6]  Deposit Cash                                                      |");
        System.out.println("| [7]  Withdraw Cash                                                     |");
        System.out.println("| [8]  Simulate Market Tick (Advance Market Time)                        |");
        System.out.println("| [9]  Export Data to CSV (Holdings & Transactions)                      |");
        System.out.println("| [10] Reset Portfolio ($10,000 Capital)                                 |");
        System.out.println("| [0]  Exit Application                                                  |");
        System.out.println("+------------------------------------------------------------------------+");
    }

    private void handleViewMarket() {
        System.out.println("=================================================================================================");
        System.out.printf("| %-6s | %-24s | %-10s | %-18s | %-10s | %-10s |\n",
                "TICKER", "COMPANY NAME", "PRICE", "DAY CHANGE", "DAY HIGH", "DAY LOW");
        System.out.println("-------------------------------------------------------------------------------------------------");

        for (Stock s : market.getAllStocks().values()) {
            System.out.printf("| %-6s | %-24s | %10s | %18s | %10s | %10s |\n",
                    s.getTicker(),
                    truncate(s.getName(), 24),
                    TradingUtils.formatCurrency(s.getCurrentPrice()),
                    TradingUtils.formatChange(s.getChangeAmount(), s.getChangePercent()),
                    TradingUtils.formatCurrency(s.getDayHigh()),
                    TradingUtils.formatCurrency(s.getDayLow()));
        }
        System.out.println("=================================================================================================");
    }

    private void handleBuyStock() {
        System.out.println("--- Buy Stock ---");
        handleViewMarket();
        System.out.print("Enter Stock Ticker to buy (e.g. AAPL): ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        Stock stock = market.getStock(ticker);
        if (stock == null) {
            System.out.println("[ERROR] Unknown stock ticker: " + ticker);
            return;
        }

        System.out.printf("Selected: %s (%s) @ %s\n",
                stock.getTicker(), stock.getName(), TradingUtils.formatCurrency(stock.getCurrentPrice()));
        System.out.printf("Available Cash: %s\n", TradingUtils.formatCurrency(user.getPortfolio().getCashBalance()));

        int maxAffordable = (int) (user.getPortfolio().getCashBalance() / stock.getCurrentPrice());
        System.out.printf("Max shares you can afford: %d\n", maxAffordable);
        if (maxAffordable <= 0) {
            System.out.println("[ERROR] You do not have enough funds to purchase any shares of this stock.");
            return;
        }

        int shares = readIntPrompt("Enter number of shares to purchase: ", 1, maxAffordable);
        double estTotal = shares * stock.getCurrentPrice();

        System.out.printf("Confirm purchase of %d share(s) of %s for %s? (yes/no): ",
                shares, stock.getTicker(), TradingUtils.formatCurrency(estTotal));
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            try {
                Transaction txn = user.getPortfolio().buyStock(stock, shares);
                System.out.println("[SUCCESS] " + txn);
            } catch (Exception e) {
                System.out.println("[ERROR] Trade failed: " + e.getMessage());
            }
        } else {
            System.out.println("[CANCELLED] Buy order was not executed.");
        }
    }

    private void handleSellStock() {
        System.out.println("--- Sell Stock ---");
        Portfolio p = user.getPortfolio();
        if (p.getHoldings().isEmpty()) {
            System.out.println("[INFO] You currently do not own any stocks to sell.");
            return;
        }

        handleViewPortfolio();
        System.out.print("Enter Stock Ticker to sell: ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        Holding holding = p.getHolding(ticker);
        if (holding == null) {
            System.out.println("[ERROR] You do not own any shares of: " + ticker);
            return;
        }

        Stock stock = market.getStock(ticker);
        if (stock == null) {
            System.out.println("[ERROR] Market data unavailable for: " + ticker);
            return;
        }

        System.out.printf("You own %d shares of %s. Current market price: %s\n",
                holding.getShares(), ticker, TradingUtils.formatCurrency(stock.getCurrentPrice()));

        int shares = readIntPrompt("Enter number of shares to sell (1 - " + holding.getShares() + "): ", 1, holding.getShares());
        double estProceeds = shares * stock.getCurrentPrice();
        double costBasis = shares * holding.getAverageBuyPrice();
        double estPnL = estProceeds - costBasis;

        System.out.printf("Estimated proceeds: %s (Estimated P&L: %s)\n",
                TradingUtils.formatCurrency(estProceeds), TradingUtils.formatCurrency(estPnL));
        System.out.print("Confirm selling order? (yes/no): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            try {
                Transaction txn = user.getPortfolio().sellStock(stock, shares);
                System.out.println("[SUCCESS] " + txn);
            } catch (Exception e) {
                System.out.println("[ERROR] Trade failed: " + e.getMessage());
            }
        } else {
            System.out.println("[CANCELLED] Sell order was not executed.");
        }
    }

    private void handleViewPortfolio() {
        Portfolio p = user.getPortfolio();
        Map<String, Holding> holdings = p.getHoldings();

        System.out.println("=========================================================================================================");
        System.out.println("                                         PORTFOLIO SUMMARY                                               ");
        System.out.println("=========================================================================================================");

        if (holdings.isEmpty()) {
            System.out.println("No active positions held in portfolio. Choose option [2] to buy stocks.");
        } else {
            System.out.printf("| %-6s | %-7s | %-12s | %-12s | %-12s | %-12s | %-10s |\n",
                    "TICKER", "SHARES", "AVG COST", "CURR PRICE", "COST BASIS", "MKT VALUE", "UNREAL P&L");
            System.out.println("---------------------------------------------------------------------------------------------------------");

            for (Holding h : holdings.values()) {
                Stock s = market.getStock(h.getTicker());
                double currPrice = s != null ? s.getCurrentPrice() : h.getAverageBuyPrice();
                double mktVal = h.getMarketValue(currPrice);
                double pnl = h.getUnrealizedPnL(currPrice);
                double retPct = h.getUnrealizedReturnPercent(currPrice);

                System.out.printf("| %-6s | %7d | %12s | %12s | %12s | %12s | %10s (%+.1f%%) |\n",
                        h.getTicker(),
                        h.getShares(),
                        TradingUtils.formatCurrency(h.getAverageBuyPrice()),
                        TradingUtils.formatCurrency(currPrice),
                        TradingUtils.formatCurrency(h.getTotalCostBasis()),
                        TradingUtils.formatCurrency(mktVal),
                        TradingUtils.formatCurrency(pnl),
                        retPct);
            }
        }
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.printf(" Available Cash Balance       : %s\n", TradingUtils.formatCurrency(p.getCashBalance()));
        System.out.printf(" Total Stocks Market Value    : %s\n", TradingUtils.formatCurrency(p.getTotalHoldingsMarketValue(market.getAllStocks())));
        System.out.printf(" Total Portfolio Net Worth    : %s\n", TradingUtils.formatCurrency(p.getTotalPortfolioValue(market.getAllStocks())));
        System.out.printf(" Total Unrealized Gain/Loss   : %s\n", TradingUtils.formatCurrency(p.getTotalUnrealizedPnL(market.getAllStocks())));
        System.out.printf(" Total Realized Gain/Loss     : %s\n", TradingUtils.formatCurrency(p.getTotalRealizedPnL()));
        System.out.printf(" Cumulative Return on Capital : %+.2f%%\n", p.getOverallReturnPercent(market.getAllStocks()));
        System.out.println("=========================================================================================================");
    }

    private void handleViewTransactions() {
        List<Transaction> txns = user.getPortfolio().getTransactions();
        System.out.println("========================================================================================================");
        System.out.println("                                      TRANSACTION AUDIT LOG                                             ");
        System.out.println("========================================================================================================");
        System.out.printf("| %-12s | %-19s | %-5s | %-6s | %-6s | %-10s | %-10s | %-10s |\n",
                "TXN ID", "TIMESTAMP", "TYPE", "TICKER", "SHARES", "EXEC PRICE", "TOTAL AMT", "CASH AFTER");
        System.out.println("--------------------------------------------------------------------------------------------------------");

        for (Transaction t : txns) {
            System.out.printf("| %-12s | %-19s | %-5s | %-6s | %6d | %10s | %10s | %10s |\n",
                    t.getId(),
                    TradingUtils.formatDateTime(t.getTimestamp()),
                    t.getType(),
                    t.getTicker(),
                    t.getShares(),
                    TradingUtils.formatCurrency(t.getPricePerShare()),
                    TradingUtils.formatCurrency(t.getTotalAmount()),
                    TradingUtils.formatCurrency(t.getCashBalanceAfter()));
        }
        System.out.println("========================================================================================================");
        System.out.printf("Total transactions executed: %d\n", txns.size());
    }

    private void handleDeposit() {
        System.out.println("--- Deposit Cash ---");
        System.out.print("Enter amount to deposit: $");
        try {
            double amt = Double.parseDouble(scanner.nextLine().trim());
            if (amt <= 0) {
                System.out.println("[ERROR] Amount must be positive.");
                return;
            }
            user.getPortfolio().deposit(amt);
            System.out.printf("[SUCCESS] Deposited %s. New Cash Balance: %s\n",
                    TradingUtils.formatCurrency(amt), TradingUtils.formatCurrency(user.getPortfolio().getCashBalance()));
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid number format.");
        }
    }

    private void handleWithdraw() {
        System.out.println("--- Withdraw Cash ---");
        System.out.printf("Available Cash: %s\n", TradingUtils.formatCurrency(user.getPortfolio().getCashBalance()));
        System.out.print("Enter amount to withdraw: $");
        try {
            double amt = Double.parseDouble(scanner.nextLine().trim());
            user.getPortfolio().withdraw(amt);
            System.out.printf("[SUCCESS] Withdrawn %s. Remaining Cash: %s\n",
                    TradingUtils.formatCurrency(amt), TradingUtils.formatCurrency(user.getPortfolio().getCashBalance()));
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void handleSimulateMarketTick() {
        market.tickMarket();
        System.out.println("[SUCCESS] Market clock advanced! Prices have fluctuated based on simulated volatility.");
        handleViewMarket();
    }

    private void handleExportCSV() {
        File holdingsFile = new File("portfolio_holdings.csv");
        File txnsFile = new File("transaction_history.csv");
        try {
            StorageManager.exportHoldingsToCSV(user.getPortfolio(), holdingsFile);
            StorageManager.exportTransactionsToCSV(user.getPortfolio(), txnsFile);
            System.out.println("[SUCCESS] Exported holdings to: " + holdingsFile.getAbsolutePath());
            System.out.println("[SUCCESS] Exported transactions to: " + txnsFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("[ERROR] Export failed: " + e.getMessage());
        }
    }

    private void handleResetPortfolio() {
        System.out.print("Are you sure you want to reset your portfolio to $10,000 cash? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            user.getPortfolio().reset(10_000.00);
            market.resetMarket();
            System.out.println("[SUCCESS] Portfolio reset to default $10,000 cash balance.");
        } else {
            System.out.println("[CANCELLED] Reset cancelled.");
        }
    }

    private int readIntPrompt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("[WARNING] Number must be between %d and %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("[WARNING] Please enter a valid number.");
            }
        }
    }

    private void pausePrompt() {
        System.out.print("\nPress [Enter] to return to the menu...");
        scanner.nextLine();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
