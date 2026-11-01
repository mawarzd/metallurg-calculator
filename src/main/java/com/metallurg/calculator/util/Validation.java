package com.metallurg.calculator.util;

/**
 * Валидация пользовательского ввода.
 */
public final class Validation {

    private Validation() {
    }

    public static double requirePositive(String raw, String fieldName) {
        double value = parseDouble(raw, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " должно быть больше нуля");
        }
        return value;
    }

    public static double requireNonNegative(String raw, String fieldName) {
        double value = parseDouble(raw, fieldName);
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " не может быть отрицательным");
        }
        return value;
    }

    public static double parseDouble(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Заполните поле: " + fieldName);
        }
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число в поле: " + fieldName);
        }
    }

    public static int requirePositiveInt(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Заполните поле: " + fieldName);
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                throw new IllegalArgumentException(fieldName + " должно быть больше нуля");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное целое число в поле: " + fieldName);
        }
    }

    public static String requireText(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Заполните поле: " + fieldName);
        }
        return raw.trim();
    }
}
