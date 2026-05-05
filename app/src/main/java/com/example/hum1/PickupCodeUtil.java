package com.example.hum1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Утилита для создания короткого числового кода получения.
 * Код строится из хеша идентификатора заявки, поэтому пользователь не видит
 * прямой Firebase-id заявки, но центр всё равно может быстро найти запись.
 */
public final class PickupCodeUtil {

    private static final String SALT = "hum1-pickup-code-v1";

    private PickupCodeUtil() {
    }

    /**
     * Создаёт стабильный 9-значный код для заявки.
     *
     * @param applicationId идентификатор заявки в Firebase
     * @return числовой код получения
     */
    public static String generate(String applicationId) {
        if (applicationId == null || applicationId.trim().isEmpty()) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((SALT + ":" + applicationId).getBytes(StandardCharsets.UTF_8));

            long value = 0;
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                value = (value << 8) | (hash[i] & 0xffL);
            }
            if (value < 0) value = -value;

            long code = 100_000_000L + (value % 900_000_000L);
            return String.format(Locale.US, "%09d", code);
        } catch (Exception ignored) {
            int fallback = Math.abs(applicationId.hashCode());
            return String.format(Locale.US, "%09d", 100_000_000 + (fallback % 900_000_000));
        }
    }

    /**
     * Проверяет, похож ли текст на код получения.
     *
     * @param value текст из QR-кода или ручного ввода
     * @return true, если строка содержит только цифры и имеет разумную длину
     */
    public static boolean isPickupCode(String value) {
        return value != null && value.matches("\\d{6,12}");
    }
}
