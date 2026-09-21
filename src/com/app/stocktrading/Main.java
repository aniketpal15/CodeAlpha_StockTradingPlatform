package com.app.stocktrading;

import com.app.stocktrading.cli.ConsoleUI;
import com.app.stocktrading.gui.TradingPlatformGUI;
import com.app.stocktrading.model.MarketEngine;
import com.app.stocktrading.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Main application launcher for Stock Trading Platform.
 */
public class Main {

    public static void main(String[] args) {
        MarketEngine market = new MarketEngine();
        User user = new User("USR-101", "Alex Morgan", 10_000.00);

        // Pre-populate sample starter positions for rich initial state
        try {
            user.getPortfolio().buyStock(market.getStock("AAPL"), 15);
            user.getPortfolio().buyStock(market.getStock("NVDA"), 25);
            user.getPortfolio().buyStock(market.getStock("MSFT"), 8);
        } catch (Exception ignored) {}

        boolean forceCli = false;
        boolean forceGui = false;

        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg) || "--console".equalsIgnoreCase(arg)) {
                forceCli = true;
            } else if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                forceGui = true;
            } else if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printHelp();
                return;
            }
        }

        if (forceCli) {
            new ConsoleUI(market, user).start();
            return;
        }

        if (forceGui) {
            launchGUI(market, user);
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[INFO] Headless environment detected. Launching Console Interface...");
            new ConsoleUI(market, user).start();
        } else {
            System.out.println("==========================================================================");
            System.out.println("          JAVA PROGRAMMING INTERNSHIP - TASK 2: STOCK TRADING PLATFORM          ");
            System.out.println("==========================================================================");
            System.out.println("[INFO] Launching Swing Graphical User Interface (GUI)...");
            System.out.println("[TIP]  To launch Console (CLI) mode instead, run: java -jar app.jar --cli");
            System.out.println("==========================================================================");
            launchGUI(market, user);
        }
    }

    private static void launchGUI(MarketEngine market, User user) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new TradingPlatformGUI(market, user).setVisible(true);
        });
    }

    private static void printHelp() {
        System.out.println("Stock Trading Platform");
        System.out.println("Usage: java -cp bin com.app.stocktrading.Main [OPTIONS]");
        System.out.println("Options:");
        System.out.println("  --gui, -g       Launch Graphical User Interface");
        System.out.println("  --cli, -c       Launch Command-Line Interface");
        System.out.println("  --help, -h      Show help information");
    }
}
