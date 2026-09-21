package com.app.stocktrading.model;

import java.io.Serializable;

/**
 * Represents an individual investor using the trading platform.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private String name;
    private final Portfolio portfolio;

    public User(String userId, String name) {
        this(userId, name, Portfolio.DEFAULT_INITIAL_BALANCE);
    }

    public User(String userId, String name, double initialBalance) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.userId = userId.trim();
        this.name = name.trim();
        this.portfolio = new Portfolio(initialBalance);
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    @Override
    public String toString() {
        return String.format("User[ID=%s, Name=%s, Cash=%s]",
                userId, name, portfolio.getCashBalance());
    }
}
