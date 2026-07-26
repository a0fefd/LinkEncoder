package com.nb.client.mixins;

import com.nb.client.ImagePreview;
import com.nb.client.Utils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
    @Inject(method = "componentHoverEffect", at = @At("HEAD"))
    private void linkEncoder$hover(Font font, Style hoveredStyle, int xMouse, int yMouse, CallbackInfo ci) {
        if (hoveredStyle == null) return;

        String url = hoveredStyle.getInsertion();
        if (url != null && Utils.looksLikeImage(url)) ImagePreview.hover(url);
    }
}