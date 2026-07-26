package com.nb.client.imageviewer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageViewer {
    private static final Pattern IMAGE_URL =
            Pattern.compile("https?://\\S+\\.(png|jpg|jpeg|webp|gif)");

    public static String findImage(String message) {
        Matcher matcher = IMAGE_URL.matcher(message);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    private static BufferedImage currentImage;

    public static void load(String url) {

        Thread thread = new Thread(() -> {
            try {
                BufferedImage image =
                        ImageIO.read(new URL(url));

                currentImage = image;

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }


    public static BufferedImage getImage() {
        return currentImage;
    }
}
