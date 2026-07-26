package com.nb.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ConfigCommand {

    private ConfigCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommands.literal("linkencoder").executes(ctx -> {
            Config.Data cfg = Config.get();
            reply(ctx.getSource(), "encode %s, decode %s, preview %s"
                    .formatted(cfg.encode, cfg.decode, cfg.preview));
            reply(ctx.getSource(), "colour #%06X, %s +%d,%d"
                    .formatted(cfg.linkColour, cfg.corner, cfg.offsetX, cfg.offsetY));
            return 1;
        });

        var toggle = ClientCommands.literal("toggle");
        toggle.then(flag("encode", () -> Config.get().encode = !Config.get().encode, () -> Config.get().encode));
        toggle.then(flag("decode", () -> Config.get().decode = !Config.get().decode, () -> Config.get().decode));
        toggle.then(flag("preview", () -> Config.get().preview = !Config.get().preview, () -> Config.get().preview));
        root.then(toggle);

        root.then(ClientCommands.literal("colour")
                .then(ClientCommands.argument("hex", StringArgumentType.word())
                        .executes(ctx -> {
                            String hex = StringArgumentType.getString(ctx, "hex").replace("#", "");

                            try {
                                Config.get().linkColour = Integer.parseInt(hex, 16) & 0xFFFFFF;
                            } catch (NumberFormatException e) {
                                reply(ctx.getSource(), "not a hex colour: " + hex);
                                return 0;
                            }

                            Config.save();
                            reply(ctx.getSource(), "colour #%06X".formatted(Config.get().linkColour));
                            return 1;
                        })));

        var position = ClientCommands.literal("position");
        for (Config.Corner corner : Config.Corner.values()) {
            position.then(ClientCommands.literal(corner.name().toLowerCase())
                    .executes(ctx -> {
                        Config.get().corner = corner;
                        Config.save();
                        reply(ctx.getSource(), "position " + corner);
                        return 1;
                    }));
        }
        root.then(position);

        root.then(ClientCommands.literal("offset")
                .then(ClientCommands.argument("x", IntegerArgumentType.integer(0, 4000))
                        .then(ClientCommands.argument("y", IntegerArgumentType.integer(0, 4000))
                                .executes(ctx -> {
                                    Config.get().offsetX = IntegerArgumentType.getInteger(ctx, "x");
                                    Config.get().offsetY = IntegerArgumentType.getInteger(ctx, "y");
                                    Config.save();
                                    reply(ctx.getSource(), "offset %d,%d"
                                            .formatted(Config.get().offsetX, Config.get().offsetY));
                                    return 1;
                                }))));

        dispatcher.register(root);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> flag(
            String name, Runnable flip, BooleanSupplier read) {

        return ClientCommands.literal(name).executes(ctx -> {
            flip.run();
            Config.save();
            reply(ctx.getSource(), name + " " + (read.getAsBoolean() ? "on" : "off"));
            return 1;
        });
    }

    private static void reply(FabricClientCommandSource source, String text) {
        source.sendFeedback(Component.literal("[link-encoder] " + text));
    }
}