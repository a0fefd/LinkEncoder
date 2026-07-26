package com.nb.client;

import java.util.Base64;

public class Encryptor {
    public static String XOR(String plaintext, String key) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < plaintext.length(); i++) {
            out.append(plaintext.charAt(i) ^ key.charAt(i % key.length()));
        }

        return out.toString();
    }

    public static String B64Encode(String plaintext) {
        return Base64.getEncoder().encodeToString(plaintext.getBytes());
    }
    public static String B64Decode(String encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes);
    }
}
