package com.app.stocktrading.gui;

import com.app.stocktrading.model.*;
import com.app.stocktrading.util.StorageManager;
import com.app.stocktrading.util.TradingUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Modern Swing Desktop GUI for the Stock Trading Platform.
 * Simulates a real-time trading terminal with live market ticks, order execution,
 * portfolio tracking, and transaction history.
 */
public class TradingPlatformGUI extends JFrame {

    private final MarketEngine market;
    private final User user;

    // KPI Stat Labels
    private JLabel cashLabel;
    private JLabel equityLabel;
    private JLabel netWorthLabel;
    private JLabel unrealizedPnlLabel;
    private JLabel realizedPnlLabel;

    // Table Models
    private DefaultTableModel marketTableModel;
    private DefaultTableModel portfolioTableModel;
    private DefaultTableModel historyTableModel;

    // Order Execution Controls
    private JComboBox<String> stockSelector;
    private JSpinner sharesSpinner;
    private JLabel quotePriceLabel;
    private JLabel estimatedTotalLabel;
    private JRadioButton buyRadio;
    private JRadioButton sellRadio;

    // Timer for auto-market simulation
    private Timer autoTickTimer;
    private JToggleButton autoTickBtn;

    // Colors
    private static final Color BULL_GREEN = new Color(34, 139, 34);
    private static final Color BEAR_RED = new Color(200, 40, 40);
    private static final Color HEADER_NAVY = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(248, 250, 252);

    public TradingPlatformGUI(MarketEngine market, User user) {
        this.market = market;
        this.user = user;

        initUI();
        refreshAllData();
    }

    private void initUI() {
        setTitle("Stock Trading Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 750);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 14, 12, 14));
        root.setBackground(Color.WHITE);

        // 1. Top Panel: Header + KPI Dashboard
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);
        topContainer.add(createHeader(), BorderLayout.NORTH);
        topContainer.add(createKpiPanel(), BorderLayout.CENTER);
        root.add(topContainer, BorderLayout.NORTH);

        // 2. Center: Tabbed Tables
        root.add(createTabbedTables(), BorderLayout.CENTER);

        // 3. Bottom: Trade Station & Controls
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 8));
        bottomContainer.setOpaque(false);
        bottomContainer.add(createTradeExecutionPanel(), BorderLayout.NORTH);
        bottomContainer.add(createToolbarPanel(), BorderLayout.SOUTH);
        root.add(bottomContainer, BorderLayout.SOUTH);

        setContentPane(root);

        // Setup auto-tick timer (ticks every 3.5 seconds when active)
        autoTickTimer = new Timer(3500, e -> {
            market.tickMarket();
            refreshAllData();
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_NAVY);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("STOCK TRADING TERMINAL");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(String.format("Investor Account: %s (%s) | Virtual Trading Sandbox",
                user.getName(), user.getUserId()));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(203, 213, 225));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createKpiPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 8, 0));
        panel.setOpaque(false);

        cashLabel = new JLabel("$0.00", SwingConstants.CENTER);
        equityLabel = new JLabel("$0.00", SwingConstants.CENTER);
        netWorthLabel = new JLabel("$0.00", SwingConstants.CENTER);
        unrealizedPnlLabel = new JLabel("$0.00", SwingConstants.CENTER);
        realizedPnlLabel = new JLabel("$0.00", SwingConstants.CENTER);

        panel.add(buildCard("Cash Balance", cashLabel, new Color(14, 116, 144)));
        panel.add(buildCard("Holdings Value", equityLabel, new Color(79, 70, 229)));
        panel.add(buildCard("Total Net Worth", netWorthLabel, new Color(15, 23, 42)));
        panel.add(buildCard("Unrealized P&L", unrealizedPnlLabel, BULL_GREEN));
        panel.add(buildCard("Realized P&L", realizedPnlLabel, new Color(217, 119, 6)));

        return panel;
    }

    private JPanel buildCard(String title, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(2, 4));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel t = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(new Color(100, 116, 139));

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valLabel.setForeground(accent);

        card.add(t, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    private JTabbedPane createTabbedTables() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: Market Watch
        String[] marketCols = {"Ticker", "Company Name", "Current Price", "Change ($)", "Change (%)", "Day High", "Day Low", "Volume"};
        marketTableModel = new DefaultTableModel(marketCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable marketTable = new JTable(marketTableModel);
        styleTable(marketTable);

        // Renderers for change columns (color green/red)
        marketTable.getColumnModel().getColumn(3).setCellRenderer(new ChangeCellRenderer());
        marketTable.getColumnModel().getColumn(4).setCellRenderer(new ChangeCellRenderer());

        tabs.addTab("📈 Market Watch", new JScrollPane(marketTable));

        // Tab 2: Portfolio Holdings
        String[] portCols = {"Ticker", "Shares Owned", "Avg Buy Price", "Current Price", "Total Cost Basis", "Market Value", "Unrealized P&L", "Return (%)"};
        portfolioTableModel = new DefaultTableModel(portCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable portfolioTable = new JTable(portfolioTableModel);
        styleTable(portfolioTable);
        portfolioTable.getColumnModel().getColumn(6).setCellRenderer(new PnLCellRenderer());
        portfolioTable.getColumnModel().getColumn(7).setCellRenderer(new PnLCellRenderer());

        tabs.addTab("💼 My Portfolio Positions", new JScrollPane(portfolioTable));

        // Tab 3: Transaction Log
        String[] histCols = {"Transaction ID", "Timestamp", "Type", "Ticker", "Shares", "Execution Price", "Total Amount", "Cash After", "Realized P&L"};
        historyTableModel = new DefaultTableModel(histCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable histTable = new JTable(historyTableModel);
        styleTable(histTable);

        tabs.addTab("📜 Transaction Audit Log", new JScrollPane(histTable));

        return tabs;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
    }

    private JPanel createTradeExecutionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Trade Execution Station"));
        panel.setOpaque(false);

        panel.add(new JLabel("Stock:"));
        stockSelector = new JComboBox<>();
        for (String ticker : market.getAllStocks().keySet()) {
            stockSelector.addItem(ticker);
        }
        stockSelector.addActionListener(e -> updateOrderCalculations());
        panel.add(stockSelector);

        quotePriceLabel = new JLabel("Price: $0.00");
        quotePriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(quotePriceLabel);

        panel.add(new JSeparator(SwingConstants.VERTICAL));

        buyRadio = new JRadioButton("BUY", true);
        sellRadio = new JRadioButton("SELL");
        ButtonGroup bg = new ButtonGroup();
        bg.add(buyRadio);
        bg.add(sellRadio);
        buyRadio.addActionListener(e -> updateOrderCalculations());
        sellRadio.addActionListener(e -> updateOrderCalculations());

        panel.add(buyRadio);
        panel.add(sellRadio);

        panel.add(new JLabel("Shares:"));
        sharesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100_000, 1));
        sharesSpinner.addChangeListener(e -> updateOrderCalculations());
        panel.add(sharesSpinner);

        estimatedTotalLabel = new JLabel("Estimated Total: $0.00");
        estimatedTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(estimatedTotalLabel);

        JButton executeBtn = new JButton("Execute Order");
        executeBtn.setBackground(new Color(37, 99, 235));
        executeBtn.setForeground(Color.BLACK);
        executeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        executeBtn.addActionListener(e -> handleExecuteOrder());
        panel.add(executeBtn);

        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        JButton tickBtn = new JButton("Simulate Next Tick");
        tickBtn.addActionListener(e -> {
            market.tickMarket();
            refreshAllData();
        });

        autoTickBtn = new JToggleButton("Auto-Market Clock (Off)");
        autoTickBtn.addActionListener(e -> {
            if (autoTickBtn.isSelected()) {
                autoTickBtn.setText("Auto-Market Clock (ON)");
                autoTickBtn.setForeground(BULL_GREEN);
                autoTickTimer.start();
            } else {
                autoTickBtn.setText("Auto-Market Clock (Off)");
                autoTickBtn.setForeground(Color.BLACK);
                autoTickTimer.stop();
            }
        });

        JButton depositBtn = new JButton("Deposit Cash");
        depositBtn.addActionListener(e -> handleDeposit());

        JButton withdrawBtn = new JButton("Withdraw Cash");
        withdrawBtn.addActionListener(e -> handleWithdraw());

        JButton exportBtn = new JButton("Export CSV Data");
        exportBtn.addActionListener(e -> handleExportCSV());

        JButton resetBtn = new JButton("Reset Portfolio");
        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset portfolio to initial $10,000 capital?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                user.getPortfolio().reset(10_000.00);
                market.resetMarket();
                refreshAllData();
            }
        });

        toolbar.add(tickBtn);
        toolbar.add(autoTickBtn);
        toolbar.add(depositBtn);
        toolbar.add(withdrawBtn);
        toolbar.add(exportBtn);
        toolbar.add(resetBtn);

        return toolbar;
    }

    private void updateOrderCalculations() {
        String ticker = (String) stockSelector.getSelectedItem();
        if (ticker == null) return;
        Stock stock = market.getStock(ticker);
        if (stock == null) return;

        int shares = (Integer) sharesSpinner.getValue();
        double total = shares * stock.getCurrentPrice();

        quotePriceLabel.setText("Price: " + TradingUtils.formatCurrency(stock.getCurrentPrice()));
        estimatedTotalLabel.setText("Estimated Total: " + TradingUtils.formatCurrency(total));
    }

    private void handleExecuteOrder() {
        String ticker = (String) stockSelector.getSelectedItem();
        Stock stock = market.getStock(ticker);
        int shares = (Integer) sharesSpinner.getValue();

        try {
            Transaction txn;
            if (buyRadio.isSelected()) {
                txn = user.getPortfolio().buyStock(stock, shares);
                JOptionPane.showMessageDialog(this,
                        String.format("Successfully bought %d shares of %s for %s!",
                                shares, ticker, TradingUtils.formatCurrency(txn.getTotalAmount())),
                        "Order Executed", JOptionPane.INFORMATION_MESSAGE);
            } else {
                txn = user.getPortfolio().sellStock(stock, shares);
                JOptionPane.showMessageDialog(this,
                        String.format("Successfully sold %d shares of %s for %s! (Realized P&L: %s)",
                                shares, ticker, TradingUtils.formatCurrency(txn.getTotalAmount()),
                                TradingUtils.formatCurrency(txn.getRealizedGainLoss())),
                        "Order Executed", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshAllData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Order Rejected", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeposit() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to deposit:", "Deposit Funds", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amt = Double.parseDouble(input.trim());
                user.getPortfolio().deposit(amt);
                refreshAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleWithdraw() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to withdraw:", "Withdraw Funds", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amt = Double.parseDouble(input.trim());
                user.getPortfolio().withdraw(amt);
                refreshAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportCSV() {
        File holdingsFile = new File("holdings_export.csv");
        File txnsFile = new File("transactions_export.csv");
        try {
            StorageManager.exportHoldingsToCSV(user.getPortfolio(), holdingsFile);
            StorageManager.exportTransactionsToCSV(user.getPortfolio(), txnsFile);
            JOptionPane.showMessageDialog(this,
                    "Export completed!\n" + holdingsFile.getAbsolutePath() + "\n" + txnsFile.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshAllData() {
        Portfolio p = user.getPortfolio();

        // 1. Refresh KPI cards
        cashLabel.setText(TradingUtils.formatCurrency(p.getCashBalance()));
        equityLabel.setText(TradingUtils.formatCurrency(p.getTotalHoldingsMarketValue(market.getAllStocks())));
        netWorthLabel.setText(TradingUtils.formatCurrency(p.getTotalPortfolioValue(market.getAllStocks())));

        double unPnl = p.getTotalUnrealizedPnL(market.getAllStocks());
        unrealizedPnlLabel.setText(TradingUtils.formatCurrency(unPnl));
        unrealizedPnlLabel.setForeground(unPnl >= 0 ? BULL_GREEN : BEAR_RED);

        double rePnl = p.getTotalRealizedPnL();
        realizedPnlLabel.setText(TradingUtils.formatCurrency(rePnl));
        realizedPnlLabel.setForeground(rePnl >= 0 ? BULL_GREEN : BEAR_RED);

        // 2. Refresh Market Table
        marketTableModel.setRowCount(0);
        for (Stock s : market.getAllStocks().values()) {
            marketTableModel.addRow(new Object[]{
                    s.getTicker(),
                    s.getName(),
                    TradingUtils.formatCurrency(s.getCurrentPrice()),
                    TradingUtils.formatCurrency(s.getChangeAmount()),
                    String.format("%+.2f%%", s.getChangePercent()),
                    TradingUtils.formatCurrency(s.getDayHigh()),
                    TradingUtils.formatCurrency(s.getDayLow()),
                    String.format("%,d", s.getVolume())
            });
        }

        // 3. Refresh Portfolio Table
        portfolioTableModel.setRowCount(0);
        for (Holding h : p.getHoldings().values()) {
            Stock s = market.getStock(h.getTicker());
            double currPrice = s != null ? s.getCurrentPrice() : h.getAverageBuyPrice();
            double pnl = h.getUnrealizedPnL(currPrice);
            double retPct = h.getUnrealizedReturnPercent(currPrice);

            portfolioTableModel.addRow(new Object[]{
                    h.getTicker(),
                    h.getShares(),
                    TradingUtils.formatCurrency(h.getAverageBuyPrice()),
                    TradingUtils.formatCurrency(currPrice),
                    TradingUtils.formatCurrency(h.getTotalCostBasis()),
                    TradingUtils.formatCurrency(h.getMarketValue(currPrice)),
                    TradingUtils.formatCurrency(pnl),
                    String.format("%+.2f%%", retPct)
            });
        }

        // 4. Refresh History Table
        historyTableModel.setRowCount(0);
        List<Transaction> txns = p.getTransactions();
        for (int i = txns.size() - 1; i >= 0; i--) {
            Transaction t = txns.get(i);
            historyTableModel.addRow(new Object[]{
                    t.getId(),
                    TradingUtils.formatDateTime(t.getTimestamp()),
                    t.getType(),
                    t.getTicker(),
                    t.getShares(),
                    TradingUtils.formatCurrency(t.getPricePerShare()),
                    TradingUtils.formatCurrency(t.getTotalAmount()),
                    TradingUtils.formatCurrency(t.getCashBalanceAfter()),
                    t.getType() == Transaction.Type.SELL ? TradingUtils.formatCurrency(t.getRealizedGainLoss()) : "-"
            });
        }

        updateOrderCalculations();
    }

    // Cell renderers for gain/loss colors
    private static class ChangeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (value != null) {
                String str = value.toString();
                if (str.startsWith("+") || (!str.startsWith("-") && !str.equals("$0.00") && !str.equals("+0.00%"))) {
                    setForeground(isSelected ? Color.WHITE : BULL_GREEN);
                } else if (str.startsWith("-")) {
                    setForeground(isSelected ? Color.WHITE : BEAR_RED);
                } else {
                    setForeground(isSelected ? Color.WHITE : Color.BLACK);
                }
            }
            return c;
        }
    }

    private static class PnLCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (value != null) {
                String str = value.toString();
                if (str.startsWith("+") || (!str.startsWith("-") && !str.equals("$0.00"))) {
                    setForeground(isSelected ? Color.WHITE : BULL_GREEN);
                } else {
                    setForeground(isSelected ? Color.WHITE : BEAR_RED);
                }
            }
            return c;
        }
    }
}
