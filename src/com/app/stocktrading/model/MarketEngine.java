package com.app.stocktrading.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simulates financial market dynamics, catalog of active stocks,
 * and periodic price fluctuations.
 */
public class MarketEngine {

    private final Map<String, Stock> stocks;

    public MarketEngine() {
        this.stocks = new LinkedHashMap<>();
        initializeDefaultMarket();
    }

    private void initializeDefaultMarket() {
        addStock(new Stock("AAPL", "Apple Inc.", 185.50));
        addStock(new Stock("MSFT", "Microsoft Corporation", 420.25));
        addStock(new Stock("GOOGL", "Alphabet Inc.", 175.80));
        addStock(new Stock("AMZN", "Amazon.com Inc.", 180.40));
        addStock(new Stock("NVDA", "NVIDIA Corporation", 125.60));
        addStock(new Stock("TSLA", "Tesla Inc.", 210.00));
        addStock(new Stock("META", "Meta Platforms Inc.", 505.30));
        addStock(new Stock("NFLX", "Netflix Inc.", 650.00));
    }

    public void addStock(Stock stock) {
        stocks.put(stock.getTicker().toUpperCase(), stock);
    }

    public Stock getStock(String ticker) {
        if (ticker == null) return null;
        return stocks.get(ticker.toUpperCase());
    }

    public Map<String, Stock> getAllStocks() {
        return Collections.unmodifiableMap(stocks);
    }

    /**
     * Advances market time by one tick, fluctuating prices across all stocks.
     */
    public void tickMarket() {
        for (Stock s : stocks.values()) {
            // Fluctuate between -3.0% and +3.0% with normal distribution
            s.simulateFluctuation(3.0);
        }
    }

    public void resetMarket() {
        stocks.clear();
        initializeDefaultMarket();
    }
}
