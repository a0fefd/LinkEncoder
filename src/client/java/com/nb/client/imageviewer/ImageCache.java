package com.nb.client.imageviewer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ImageCache {

    private static final Map<String, Identifier> TEXTURES = new HashMap<>();


    public static void add(
            String url,
            DynamicTexture texture
    ) {

        Identifier id =
                Identifier.fromNamespaceAndPath(
                        "link-encryptor",
                        "image/" + TEXTURES.size()
                );


        Minecraft.getInstance()
                .getTextureManager()
                .register(id, texture);


        TEXTURES.put(url, id);
    }


    public static Identifier get(String url) {
        return TEXTURES.get(url);
    }
}