# Stock Trading Platform (Task 2)

[![Java Version](https://img.shields.io/badge/Java-8%20%7C%2011%20%7C%2017%20%7C%2021%20%7C%2025-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](#license)
[![Internship](https://img.shields.io/badge/-Java%20Programming-orange.svg)](https://www.app.tech)

> **Task 2: Stock Trading Platform** for the ** Java Programming Internship**.  
> A Java simulated financial exchange application featuring both a **Modern Swing Graphical User Interface (GUI)** and an interactive **Command-Line Interface (CLI)** to simulate stock market dynamics, execute real-time buy and sell orders, calculate realized and unrealized Profit & Loss (P&L), monitor portfolio performance over time, and persist investment records.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Requirements Fulfilled](#-requirements-fulfilled)
- [Key Features](#-key-features)
- [Project Architecture](#-project-architecture)
- [Directory Structure](#-directory-structure)
- [Prerequisites](#-prerequisites)
- [How to Compile and Run](#-how-to-compile-and-run)
  - [Option 1: Windows Batch Scripts (Quickest)](#option-1-windows-batch-scripts-quickest)
  - [Option 2: PowerShell / Terminal](#option-2-powershell--terminal)
  - [Option 3: IDE (VS Code, IntelliJ IDEA, Eclipse)](#option-3-ide-vs-code-intellij-idea-eclipse)
- [Command-Line Arguments](#-command-line-arguments)
- [Automated Testing](#-automated-testing)
- [Sample Data & Demonstration](#-sample-data--demonstration)
- [Sample Output Preview](#-sample-output-preview)
- [Author & Submission Info](#-author--submission-info)

---

## 📖 Overview

The **Stock Trading Platform** simulates a stock trading terminal where investors can monitor real-time stock quotations, execute market buy and sell orders, manage liquid cash balances, and monitor portfolio valuations, cost bases, and returns across diverse market regimes.

---

## ✅ Requirements Fulfilled

| Requirement from  M1 | Implementation Detail | Status |
| :--- | :--- | :---: |
| **Simulate a basic stock trading environment** | `MarketEngine` simulates ticker catalogs (AAPL, MSFT, NVDA, GOOGL, AMZN, TSLA, META, NFLX) with stochastic Gaussian price volatility. | ✅ Complete |
| **Market data display and buy/sell operations** | Real-time quotation grids, weighted average price calculations, cash validation, share validation, and order execution. | ✅ Complete |
| **Track portfolio performance over time** | Dynamic tracking of Total Net Worth, Unrealized P&L, Realized P&L, Total Cost Basis, and Cumulative Return %. | ✅ Complete |
| **Use OOP to manage stocks, users and transactions** | Clean OOP domain model: `User`, `Portfolio`, `Holding`, `Stock`, `Transaction`, and `MarketEngine` with encapsulation and immutable logs. | ✅ Complete |
| **File I/O or database to persist portfolio data** | `StorageManager` implements Java binary object serialization (`.dat`) and structured CSV export for holdings and audit transactions. | ✅ Complete |
| **Console or GUI Interface** | **Both included:** Full interactive CLI menu + modern responsive Swing desktop trading terminal. | ✅ Complete |

---

## 🚀 Key Features

### 1. Dual Interface Support
- **Modern Desktop Trading Terminal (Swing GUI):**
  - **Live KPI Dashboard:** Instant visual cards displaying *Cash Balance*, *Holdings Value*, *Total Net Worth*, *Unrealized P&L*, and *Realized P&L*.
  - **Tabbed Workspace:**
    - 📈 *Market Watch:* Live prices, dollar changes, percentage returns, day highs, day lows, and trading volume (color-coded green/red).
    - 💼 *My Portfolio Positions:* Active stock positions, shares owned, weighted average purchase cost, current market value, unrealized gain/loss, and position return %.
    - 📜 *Transaction Audit Log:* Chronological ledger of all executed buy/sell/deposit/withdrawal transactions with timestamps and unique IDs.
  - **Interactive Trade Execution Station:** Select ticker, choose BUY or SELL, specify share quantity, and review live price quotes before instant one-click execution.
  - **Auto-Market Simulation Clock:** Toggleable market timer (ticks every 3.5 seconds) simulating live exchange trading hours.
- **Interactive Command-Line Interface (CLI):**
  - High-readability ASCII tables with color indicators.
  - Menu options with input validation preventing invalid tickers, negative shares, or overdrafts.

### 2. Comprehensive Financial & Portfolio Mathematics
- **Weighted Average Cost Basis:** Correctly recalculates average purchase price upon multiple incremental buy orders.
- **Realized vs. Unrealized P&L:**
  - Realized gains/losses calculated and locked in upon each sell transaction.
  - Unrealized gains/losses computed live against fluctuating market quotes.
- **Cash Management:** Deposit and withdraw funds with overdraft prevention.
- **Transaction History:** Immutable transaction logs with UUID identifiers and timestamps.

### 3. Persistence & Export
- **Binary State Serialization:** Save and restore complete investor account state including portfolio and transaction histories.
- **CSV Data Export:** One-click export of `portfolio_holdings.csv` and `transaction_history.csv`.

---

## 🏗 Project Architecture

```
com.app.stocktrading
 ├── model/
 │    ├── Stock.java          --> Entity representing equity with price history & volatility
 │    ├── Holding.java        --> Represents investor's active position in a specific ticker
 │    ├── Transaction.java    --> Immutable record of executed order (BUY/SELL/DEPOSIT/WITHDRAW)
 │    ├── Portfolio.java      --> Manages cash balance, holdings map, valuations, P&L
 │    ├── User.java           --> Investor account model holding identity & Portfolio
 │    └── MarketEngine.java   --> Catalog of stocks and market tick simulation
 ├── cli/
 │    └── ConsoleUI.java      --> Interactive terminal UI with menus and input sanitization
 ├── gui/
 │    └── TradingPlatformGUI.java --> Swing trading terminal with live cards, JTable, order station
 ├── util/
 │    ├── TradingUtils.java   --> Currency, percentage, rounding, and datetime formatters
 │    └── StorageManager.java --> Serialization & CSV persistence engine
 └── Main.java                --> Central launcher with auto-detection and flag routing
```

---

## 📁 Directory Structure

```
_StockTradingPlatform/
├── data/
│   └── sample_stocks.csv
├── src/
│   └── com/app/stocktrading/
│       ├── Main.java
│       ├── cli/
│       │   └── ConsoleUI.java
│       ├── gui/
│       │   └── TradingPlatformGUI.java
│       ├── model/
│       │   ├── Holding.java
│       │   ├── MarketEngine.java
│       │   ├── Portfolio.java
│       │   ├── Stock.java
│       │   ├── Transaction.java
│       │   └── User.java
│       └── util/
│           ├── StorageManager.java
│           └── TradingUtils.java
├── test/
│   └── com/app/stocktrading/
│       └── StockTradingPlatformTest.java
├── .gitignore
├── compile.bat
├── compile.ps1
├── README.md
├── run-console.bat
├── run-console.ps1
├── run-gui.bat
├── run-gui.ps1
├── run-tests.bat
└── run-tests.ps1
```

---

## ⚙️ Prerequisites

- **Java Development Kit (JDK):** Version 8 or higher (tested on Java 17, 21, and 25 LTS).
- **Operating System:** Windows, macOS, or Linux.
- Zero external libraries or heavy dependencies required (pure standard Java SE).

---

## 💻 How to Compile and Run

### Option 1: Windows Batch Scripts (Quickest)

Double-click or run from Command Prompt:
1. **Compile:** `compile.bat`
2. **Launch Swing GUI:** `run-gui.bat`
3. **Launch Terminal CLI:** `run-console.bat`
4. **Run Unit Tests:** `run-tests.bat`

### Option 2: PowerShell / Terminal

```powershell
# 1. Compile all source and test files
.\compile.ps1

# 2. Run the Graphical User Interface (GUI)
.\run-gui.ps1

# 3. Run the Command-Line Interface (CLI)
.\run-console.ps1

# 4. Run Automated Test Suite
.\run-tests.ps1
```

Or using standard commands:
```bash
# Compile
javac -d bin src/com/app/stocktrading/*.java src/com/app/stocktrading/*/*.java test/com/app/stocktrading/*.java

# Run GUI
java -cp bin com.app.stocktrading.Main --gui

# Run CLI
java -cp bin com.app.stocktrading.Main --cli

# Run Tests
java -cp bin com.app.stocktrading.StockTradingPlatformTest
```

---

## 🕹 Command-Line Arguments

The application auto-detects graphical display support. To force a specific mode:

```bash
java -cp bin com.app.stocktrading.Main --gui       # Force Swing Desktop GUI
java -cp bin com.app.stocktrading.Main --cli       # Force Terminal CLI
java -cp bin com.app.stocktrading.Main --help      # Show help information
```

---

## 🧪 Automated Testing

A dedicated test suite (`StockTradingPlatformTest.java`) tests all core business logic:

```powershell
.\run-tests.ps1
```

**Test Coverage Highlights (52 Assertions, 0 Failures):**
- Stock price volatility & daily high/low tracking.
- Holding weighted-average purchase cost calculations.
- Portfolio buy orders & cash deduction.
- Insufficient fund rejection & state preservation.
- Sell order execution, proceeds calculation, and realized P&L.
- Overdraft & invalid position short-selling prevention.
- Portfolio net worth, market valuation, and cumulative return metrics.
- Binary object serialization & deserialization.
- CSV export integrity for holdings and transactions.

---

## 📊 Sample Output Preview

```
==========================================================================
          JAVA PROGRAMMING INTERNSHIP - TASK 2: STOCK TRADING PLATFORM          
       Simulated Financial Exchange | Portfolio Management | Live Analytics 
==========================================================================
 Trader: Alex Morgan | Starting Cash: $10,000.00

+------------------------------------------------------------------------+
| CASH: $2,764.50    | TOTAL VALUE: $10,230.10  | UNREALIZED P&L: +$230.10 |
+------------------------------------------------------------------------+
| [1]  View Live Market Quotations                                       |
| [2]  Buy Stock (Market Order)                                          |
| [3]  Sell Stock (Market Order)                                         |
| [4]  View Portfolio & Performance Breakdown                            |
| [5]  View Transaction Execution History                                |
| [6]  Deposit Cash                                                      |
| [7]  Withdraw Cash                                                     |
| [8]  Simulate Market Tick (Advance Market Time)                        |
| [9]  Export Data to CSV (Holdings & Transactions)                      |
| [10] Reset Portfolio ($10,000 Capital)                                 |
| [0]  Exit Application                                                  |
+------------------------------------------------------------------------+
```

---

## 👨‍💻 Author & Submission Info

- **Intern:**  Java Programming Intern
- **Domain:** Java Development
- **Task:** Task 2 — Stock Trading Platform
- **Organization:** [](https://www.app.tech)
