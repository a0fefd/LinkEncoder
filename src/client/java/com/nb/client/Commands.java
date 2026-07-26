package com.nb.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;

public class Commands {
    public static int test_command(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("Success!"));
        return 1;
    }
    public static int encode(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal(
                Encryptor.B64Encode(StringArgumentType.getString(context, "plaintext"))
        ));
        return 1;
    }
    public static int decode(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal(
                Encryptor.B64Decode(StringArgumentType.getString(context, "encoded"))
        ));
        return 1;
    }
}
