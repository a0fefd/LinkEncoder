package com.nb.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

public class ImageScreen extends Screen {

    private static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final int PADDING = 20;

    private final String url;

    private Identifier texture;
    private int imageWidth;
    private int imageHeight;

    private ImageScreen(String url) {
        super(Component.literal(url));
        this.url = url;
    }

    /**
     * Deferred: opening a screen mid-command would be clobbered by the chat screen closing.
     *
     * @return
     */
    public static ClickEvent.Action open(String url) {
        Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new ImageScreen(url)));
        return null;
    }

    @Override
    protected void init() {
        if (texture != null) return;

        Thread thread = new Thread(this::download, "image-view");
        thread.setDaemon(true);
        thread.start();
    }

    private void download() {
        try {
            byte[] bytes = fetch();
            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));

            // register() touches GL state, so it has to happen on the render thread
            Minecraft.getInstance().execute(() -> {
                Identifier id = LinkEncoderClient.id("view/" + Integer.toHexString(url.hashCode()));
                Minecraft.getInstance().getTextureManager()
                        .register(id, new DynamicTexture(() -> url, image));

                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
                texture = id;
            });
        } catch (Exception e) {
            LinkEncoderClient.LOGGER.warn("image load failed: {}", url, e);
        }
    }

    private byte[] fetch() throws Exception {
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

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        if (texture == null) return;

        float scale = Math.min(1.0f, Math.min(
                (float) (width - PADDING * 2) / imageWidth,
                (float) (height - PADDING * 2) / imageHeight));

        int w = Math.max(1, Math.round(imageWidth * scale));
        int h = Math.max(1, Math.round(imageHeight * scale));
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0,
                w, h, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}