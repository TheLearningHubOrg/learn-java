package com.learninghub.catalog.model;

import java.util.Objects;

/**
 * An ISBN-13 value object. The compact constructor normalises the input
 * (drops hyphens and spaces) and rejects anything with a bad checksum, so an
 * {@code Isbn} instance is valid by construction.
 */
public record Isbn(String value) {

    public Isbn {
        Objects.requireNonNull(value, "value");
        String digits = value.replace("-", "").replace(" ", "");
        if (!digits.matches("\\d{13}")) {
            throw new IllegalArgumentException("ISBN-13 needs exactly 13 digits: '" + value + "'");
        }
        int expected = checkDigit(digits.substring(0, 12));
        int actual = digits.charAt(12) - '0';
        if (expected != actual) {
            throw new IllegalArgumentException(
                    "ISBN-13 checksum failed for '" + value + "': expected check digit " + expected);
        }
        value = digits;
    }

    /** Weights the 12 leading digits 1,3,1,3... and returns the digit that makes the sum a multiple of 10. */
    public static int checkDigit(String first12) {
        if (first12.length() != 12) {
            throw new IllegalArgumentException("need 12 digits, got " + first12.length());
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.digit(first12.charAt(i), 10);
            if (digit < 0) {
                throw new IllegalArgumentException("not a digit at position " + i);
            }
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return (10 - sum % 10) % 10;
    }

    @Override
    public String toString() {
        return value;
    }
}
