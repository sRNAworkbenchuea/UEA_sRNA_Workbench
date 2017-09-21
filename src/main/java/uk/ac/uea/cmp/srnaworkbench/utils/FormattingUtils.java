/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.text.*;
import java.util.*;

/**
 * Class to store number formats in one place
 *
 * @author prb07qmu
 */
public class FormattingUtils {

    private static Map<String, NumberFormat> NUMBER_FORMAT_MAP;

    static {
        NUMBER_FORMAT_MAP = new HashMap<String, NumberFormat>();

        addNumberFormat("#0.0");
        addNumberFormat("#0.#");
        addNumberFormat("#0.00");
        addNumberFormat("#0.##");
        addNumberFormat("#,##0");
    }

    public static NumberFormat addNumberFormat(String formatString) {
        return getNumberFormat(formatString, true);
    }

    public static NumberFormat getNumberFormat(String formatString, boolean addIfMissing) {
        NumberFormat fmt = NUMBER_FORMAT_MAP.get(formatString);

        if (fmt == null && addIfMissing) {
            fmt = new DecimalFormat(formatString);
            NUMBER_FORMAT_MAP.put(formatString, fmt);
        }

        return fmt;
    }

    public static NumberFormat getNumberFormat(String formatString) {
        return getNumberFormat(formatString, false);
    }

    public static String format(String format, int i) {
        return getNumberFormat(format, true).format(i);
    }

    public static String format(String format, long l) {
        return getNumberFormat(format, true).format(l);
    }

    public static String format(String format, float f) {
        return getNumberFormat(format, true).format(f);
    }

    public static String format(String formatString, double d) {
        return getNumberFormat(formatString, true).format(d);
    }

    public static String formatWithThresholds(int n, double lower, double upper, String internalFormat, String externalFormat) {
        return formatWithThresholds((double) n, lower, upper, internalFormat, externalFormat);
    }

    public static String formatWithThresholds(double n, double lower, double upper, String internalFormat, String externalFormat) {
        if (Math.abs(n) > upper || Math.abs(n) < lower) {
            return String.format(internalFormat, n);
        } else {
            return String.format(externalFormat, n);
        }
    }

    public static String formatSRNACounts(int n) {
        return formatSRNACounts((double) n);
    }

    public static String formatSRNACounts(double n) {
        
        String result = formatWithThresholds(n, 0.01, 9999, "%.2e", "%.2f");
        return n > 0.0 ? result : "0";
    }
}
