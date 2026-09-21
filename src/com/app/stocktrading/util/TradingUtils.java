package com.app.stocktrading.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility helper methods for currency formatting, rounding, and datetime strings.
 */
public final class TradingUtils {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("+#,##0.00%;-#,##0.00%");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TradingUtils() {
        // Prevent instantiation
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be >= 0");
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(round(amount, 2));
    }

    public static String formatPercent(double percentValue) {
        // percentValue expected as 0.05 for 5% or pass percentage decimal
        return PERCENT_FORMAT.format(percentValue / 100.0);
    }

    public static String formatChange(double changeAmount, double changePercent) {
        String sign = changeAmount >= 0 ? "+" : "";
        return String.format("%s$%.2f (%s%.2f%%)", sign, changeAmount, sign, changePercent);
    }

    public static String formatDateTime(LocalDateTime time) {
        if (time == null) return "N/A";
        return time.format(DATE_TIME_FORMATTER);
    }
}
