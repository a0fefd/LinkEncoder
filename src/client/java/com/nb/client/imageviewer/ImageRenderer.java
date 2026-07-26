package com.nb.client.imageviewer;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;


public class ImageRenderer {


    private static String hovered;


    private static final Pattern IMAGE =
            Pattern.compile(
                    "https?://\\S+\\.(png|jpg|jpeg|webp)"
            );


    public static void setHovered(String url) {
        hovered = url;

        if(url != null)
            ImageDownloader.download(url);
    }



    public static void register() {


        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,

                Identifier.fromNamespaceAndPath(
                        "link-encryptor",
                        "preview"
                ),

                (guiGraphics, tickCounter) -> {


                    if(hovered == null)
                        return;


                    Identifier texture =
                            ImageCache.get(hovered);


                    if(texture == null)
                        return;



                    Minecraft client =
                            Minecraft.getInstance();


                    int x =
                            client.getWindow()
                                    .getGuiScaledWidth()
                                    / 2;


                    int y = 20;


                    guiGraphics.blit(
                            texture,
                            x,
                            y,
                            0,
                            0,
                            200,
                            200,
                            200,
                            200
                    );
                }
        );
    }


    public static String find(String text) {

        var matcher =
                IMAGE.matcher(text);

        if(matcher.find())
            return matcher.group();


        return null;
    }
}