package com.nb.client.imageviewer;

import com.nb.client.imageviewer.ImageCache;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public class ImageDownloader {


    public static void download(String url) {

        if (ImageCache.get(url) != null)
            return;


        Thread thread = new Thread(() -> {

            try {

                InputStream stream =
                        new URL(url).openStream();


                BufferedImage buffered =
                        ImageIO.read(stream);


                NativeImage image =
                        new NativeImage(
                                buffered.getWidth(),
                                buffered.getHeight(),
                                false
                        );


                for (int x = 0; x < buffered.getWidth(); x++) {
                    for (int y = 0; y < buffered.getHeight(); y++) {

                        int color =
                                buffered.getRGB(x, y);

                        image.setPixelRGBA(
                                x,
                                y,
                                color
                        );
                    }
                }


                ImageCache.add(
                        url,
                        new DynamicTexture(image)
                );


            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        thread.start();
    }
}