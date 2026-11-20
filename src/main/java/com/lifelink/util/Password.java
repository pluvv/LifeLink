package com.lifelink.util;

import org.mindrot.jbcrypt.BCrypt;

public final class Password {
    private Password() {}

    public static String hash(String plainText) {
        if (plainText == null || plainText.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        return BCrypt.hashpw(plainText, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainText, String hash) {
        return plainText != null && hash != null && BCrypt.checkpw(plainText, hash);
    }
}
