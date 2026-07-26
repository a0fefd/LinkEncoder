package com.nb.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

public final class ImagePreview {

    private static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 130;

    /** How long /view keeps the preview up. Hover ignores this entirely. */
    private static final long HIDE_AFTER_MS = 2_000;

    // one fixed id: re-registering replaces the previous texture instead of accumulating them
    private static final Identifier TEXTURE_ID = LinkEncoderClient.id("preview");

    private static volatile int generation;

    private static Identifier texture;
    private static int imageWidth;
    private static int imageHeight;

    private static String currentUrl;
    private static boolean hoveredThisFrame;
    private static long pinnedUntil;

    private ImagePreview() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                LinkEncoderClient.id("image_preview"),
                ImagePreview::draw);
    }

    /** Hover: visible only while the cursor stays on the link. Never pins. */
    public static void hover(String url) {
        if (!Config.get().preview) return;

        hoveredThisFrame = true;
        load(url);
    }

    /** Command: visible for HIDE_AFTER_MS regardless of the cursor. */
    public static void show(String url) {
        if (!Config.get().preview) return;

        pinnedUntil = System.currentTimeMillis() + HIDE_AFTER_MS;
        load(url);
    }

    public static void clear() {
        generation++;
        pinnedUntil = 0;
        currentUrl = null;
        texture = null;
    }

    /** Same URL means already loaded, still downloading, or previously failed - all no-ops. */
    private static void load(String url) {
        if (url.equals(currentUrl)) return;

        currentUrl = url;
        int token = ++generation;
        texture = null;

        Thread thread = new Thread(() -> download(url, token), "image-preview");
        thread.setDaemon(true);
        thread.start();
    }

    private static void download(String url, int token) {
        try {
            byte[] bytes = fetch(url);
            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));

            Minecraft.getInstance().execute(() -> {
                // a slower earlier request must not overwrite a newer one
                if (token != generation) {
                    image.close();
                    return;
                }

                Minecraft.getInstance().getTextureManager()
                        .register(TEXTURE_ID, new DynamicTexture(() -> url, image));

                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
                texture = TEXTURE_ID;
            });
        } catch (Exception e) {
            LinkEncoderClient.LOGGER.warn("preview failed: {}", url, e);
        }
    }

    private static byte[] fetch(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(5_000);
        connection.setReadTimeout(10_000);
        connection.setRequestProperty("User-Agent", "link-encoder");

        try (InputStream stream = connection.getInputStream()) {
            return stream.readNBytes(MAX_BYTES);
        } finally {
            connection.disconnect();
        }
    }

    private static void draw(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        boolean visible = hoveredThisFrame || System.currentTimeMillis() < pinnedUntil;
        hoveredThisFrame = false;

        if (!visible || texture == null) return;

        float scale = Math.min(1.0f, Math.min(
                (float) MAX_WIDTH / imageWidth,
                (float) MAX_HEIGHT / imageHeight));

        int w = Math.max(1, Math.round(imageWidth * scale));
        int h = Math.max(1, Math.round(imageHeight * scale));
        Config.Data cfg = Config.get();
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int x = switch (cfg.corner) {
            case TOP_LEFT, BOTTOM_LEFT -> cfg.offsetX;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - w - cfg.offsetX;
        };

        int y = switch (cfg.corner) {
            case TOP_LEFT, TOP_RIGHT -> cfg.offsetY;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - h - cfg.offsetY;
        };

        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xC0000000);

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f,
                w, h, imageWidth, imageHeight, imageWidth, imageHeight);
    }

}