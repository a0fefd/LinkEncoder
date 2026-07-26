package com.nb.client.mixin;


import com.nb.client.imageviewer.ImageRenderer;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ChatScreen.class)
public class ChatScreenMixin {


    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void render(
            CallbackInfo ci
    ) {


        Minecraft client =
                Minecraft.getInstance();


        Style style =
                client.gui
                        .getChat()
                        .getClickedComponentStyleAt(
                                client.mouseHandler.xpos(),
                                client.mouseHandler.ypos()
                        );


        if(style == null) {
            ImagePreviewRenderer.setHovered(null);
            return;
        }


        String text =
                style.getString();


        String image =
                ImagePreviewRenderer.find(text);


        ImagePreviewRenderer.setHovered(image);
    }
}