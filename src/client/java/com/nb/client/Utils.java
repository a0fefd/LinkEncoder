package com.nb.client;

import net.minecraft.client.Minecraft;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {


    private static final String TAIL = "[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]";

    public static final Pattern LINK = Pattern.compile(
            "\\b(?:(?:https?|ftp)://|www\\.)" + TAIL
                    + "|\\b[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.(?:com|net|org|io|dev|gg|edu|gov)\\b(?:/" + TAIL + ")?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$"
    );


    public static List<String> extractLinks(String text) {
        List<String> links = new ArrayList<>();
        // Regex matches http, https, ftp, and www links
//        String urlRegex = "\\b(https?://|www\\.|ftp://)[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|](\\.com|\\.net|\\.org)";

        Matcher matcher = LINK.matcher(text);

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

    private static final Pattern IMAGE_EXT = Pattern.compile(
            "\\.(png|jpe?g|gif|bmp|tga)$", Pattern.CASE_INSENSITIVE);

    public static boolean looksLikeImage(String url) {
        try {
            URI uri = URI.create(url.contains("://") ? url : "https://" + url);
            String path = uri.getPath();
            return path != null && IMAGE_EXT.matcher(path).find();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static final Map<String, Boolean> IMAGE_CACHE = new ConcurrentHashMap<>();

    public static void isImage(String url, Consumer<Boolean> callback) {
        Boolean cached = IMAGE_CACHE.get(url);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        Thread thread = new Thread(() -> {
            boolean result = false;

            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
                connection.setRequestMethod("HEAD");
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(5_000);
                connection.setReadTimeout(5_000);
                connection.setRequestProperty("User-Agent", "link-encoder");

                String type = connection.getContentType();
                result = type != null && type.toLowerCase(Locale.ROOT).startsWith("image/");
                connection.disconnect();
            } catch (Exception e) {
                LinkEncoderClient.LOGGER.debug("HEAD failed: {}", url, e);
            }

            IMAGE_CACHE.put(url, result);
            boolean finalResult = result;
            Minecraft.getInstance().execute(() -> callback.accept(finalResult));
        }, "image-check");

        thread.setDaemon(true);
        thread.start();
    }

    public static String normalize(String url) {
        return url.contains("://") ? url : "https://" + url;
    }

}
