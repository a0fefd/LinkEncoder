package com.nb.client;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static List<String> extractLinks(String text) {
        List<String> links = new ArrayList<>();
        // Regex matches http, https, ftp, and www links
        String urlRegex = "\\b(https?://|www\\.|ftp://)[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]";

        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String link = matcher.group();
            // Clean trailing punctuation if necessary
            if (link.endsWith(".") || link.endsWith(",") || link.endsWith("!")) {
                link = link.substring(0, link.length() - 1);
            }
            links.add(link);
        }
        return links;
    }

    private static final Pattern LINK = Pattern.compile(
            "\\b(https?://|www\\.|ftp://)[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$"
    );


    //    public static boolean isBase64(String text) {
//        if (text == null || text.isEmpty()) return false;
//        // Remove whitespace for robust checking
//        String sanitized = text.replaceAll("\\s", "");
//        Matcher matcher = BASE64_PATTERN.matcher(sanitized);
//        return matcher.matches();
//    }
    public static String B64Encode(String plaintext) {
        return Base64.getEncoder().encodeToString(plaintext.getBytes());
    }
    public static String B64Decode(String encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes);
    }

    public static boolean isEncodedLink(String token) {
        if (token == null || token.length() < 8 || token.length() % 4 != 0) return false;
        if (!BASE64_PATTERN.matcher(token).matches()) return false;

        try {
            return LINK.matcher(B64Decode(token)).matches();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
