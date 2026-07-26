package com.nb.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.nb.client.imageviewer.ImageViewer;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LinkEncryptorClient implements ClientModInitializer {
	public static final String MOD_ID = "link-encryptor";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		ClientSendMessageEvents.MODIFY_CHAT.register((message) -> {
			if (message.contains("https://")) {
				String newMessage = message;
//				LOGGER.info("Modifying link message: " + message);
				List<String> links = Utils.extractLinks(newMessage);
				for (String link : links) {
					newMessage = newMessage.replace(link, Encryptor.B64Encode(link));
				}
				return newMessage;
			}

			return message;
		});

//		ClientReceiveMessageEvents.MODIFY_GAME.register(((message, overlay) -> {
//			String newMessage = message.getString();
//			List<String> tokens = List.of(newMessage.split(" "));
//			for (String token : tokens) {
//				if (Utils.isBase64(token)) {
//					newMessage = newMessage.replace(token, Encryptor.B64Decode(token));
//				}
//			}
//			return Component.literal(newMessage);
//		}));
		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, timestamp) -> {
			String text = message.getString();

			for (String token : text.split(" ")) {
				if (Utils.isBase64(token)) {
					text = text.replace(token, Encryptor.B64Decode(token));
				}
			}
            String image = ImageViewer.findImage(text);

			if (image != null) {
				Component component =
						Component.literal(text)
								.withStyle(style ->
										style
												.withColor(ChatFormatting.BLUE)
												.withUnderlined(true)
												.withClickEvent(
														new ClickEvent(
																ClickEvent.Action.OPEN_URL,
																image
														)
												)
								);
			}

			Minecraft.getInstance().gui.getChat().addPlayerMessage(Component.literal(text),null,null);

			// Prevent the original message from being shown.
			return false;
		});

//		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
//			dispatcher.register(Commands.literal("test_command").executes(com.nb.client.Commands::test_command));
//			dispatcher.register(Commands.literal("encode")
//					.then(Commands.argument("plaintext", StringArgumentType.string())
//						.executes(com.nb.client.Commands::encode))
//			);
//			dispatcher.register(Commands.literal("decode")
//					.then(Commands.argument("encoded", StringArgumentType.string())
//							.executes(com.nb.client.Commands::decode))
//			);
//		});
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, buildContext) -> {
			dispatcher.register(ClientCommands.literal("test_command").executes(com.nb.client.Commands::test_command));
			dispatcher.register(ClientCommands.literal("encode")
					.then(ClientCommands.argument("plaintext", StringArgumentType.string())
							.executes(com.nb.client.Commands::encode))
			);
			dispatcher.register(ClientCommands.literal("decode")
					.then(ClientCommands.argument("encoded", StringArgumentType.string())
							.executes(com.nb.client.Commands::decode))
			);
		}));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

}
