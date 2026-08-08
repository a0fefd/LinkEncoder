package com.nb.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.a0fefd.ConfigLib.Config;
import com.a0fefd.ConfigLib.ConfigOption;
import com.a0fefd.ConfigLib.ConfigOptionType;

public class LinkEncoderClient implements ClientModInitializer {



	public static final String MOD_ID = "link-encoder";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static String dump(Component c, int depth) {
		StringBuilder sb = new StringBuilder();
		sb.append("  ".repeat(depth))
				.append(c.getContents().getClass().getSimpleName())
				.append(" text=")
				.append(c.getContents() instanceof PlainTextContents p ? "\"" + p.text() + "\"" : "-")
				.append(" style=").append(c.getStyle())
				.append('\n');
		for (Component s : c.getSiblings()) sb.append(dump(s, depth + 1));
		return sb.toString();
	}

	@Override
	public void onInitializeClient() {
		Config.load();
		ImagePreview.register();

		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, timestamp) -> {
			if (!Config.get().decode || !checkMessage(message)) return true;
			Minecraft.getInstance().gui.getChat()
					.addPlayerMessage(transform(message), null, null);
			return false;
		});

		ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) ->
				overlay || !Config.get().decode ? message : transform(message));

		ClientSendMessageEvents.ALLOW_CHAT.register(message ->
				!Config.get().encode || withinLimit(encodeLinks(message)));

		ClientSendMessageEvents.MODIFY_CHAT.register(message ->
				Config.get().encode ? encodeLinks(message) : message);

		ClientSendMessageEvents.ALLOW_COMMAND.register(command ->
				!Config.get().encode || !Utils.isMessageCommand(command)
						|| withinLimit(encodeLinks(command)));

		ClientSendMessageEvents.MODIFY_COMMAND.register(command ->
				Config.get().encode && Utils.isMessageCommand(command) ? encodeLinks(command) : command);

		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, buildContext) -> {
			dispatcher.register(ClientCommands.literal("encode")
					.then(ClientCommands.argument("plaintext", StringArgumentType.greedyString())
							.executes(com.nb.client.Commands::encode))
			);
			dispatcher.register(ClientCommands.literal("decode")
					.then(ClientCommands.argument("encoded", StringArgumentType.greedyString())
							.executes(com.nb.client.Commands::decode))
			);
			dispatcher.register(ClientCommands.literal("view")
					.executes(ctx -> { ImagePreview.clear(); return 1; })
					.then(ClientCommands.argument("url", StringArgumentType.greedyString())
							.executes(ctx -> {
								ImagePreview.show(Utils.normalize(StringArgumentType.getString(ctx, "url")));
								return 1;
							})));

			ConfigCommand.register(dispatcher);
		}));
	}

	private static final int MAX_LENGTH = 256;

	private static boolean withinLimit(String encoded) {
		if (encoded.length() <= MAX_LENGTH) return true;

		Minecraft.getInstance().gui.getChat().addClientSystemMessage(
				Component.literal("[link-encoder] encoded to %d characters, %d over the %d limit, please reduce length!"
								.formatted(encoded.length(), encoded.length() - MAX_LENGTH, MAX_LENGTH))
						.withStyle(errorStyle));
		return false;
	}

	private static final Style errorStyle = Style.EMPTY
				.withColor(0xff4040)
				.withUnderlined(true)
				.withBold(true);

	private static Style linkStyle(String url) {
		Style style = Style.EMPTY
				.withColor(Config.get().linkColour)
				.withUnderlined(true)
				.withHoverEvent(new HoverEvent.ShowText(Component.literal(url)))
				.withInsertion(url);

		if (Utils.looksLikeImage(url)) {
			return style.withClickEvent(new ClickEvent.RunCommand("/view " + Utils.normalize(url)));
		}

		try {
			return style.withClickEvent(new ClickEvent.OpenUrl(URI.create(Utils.normalize(url))));
		} catch (IllegalArgumentException e) {
			return style;
		}
	}

	private static boolean checkMessage(Component message) {
		String[] tokens = message.getString().split(" ");
		for (String token : tokens) {
			if (Utils.isEncodedLink(token)) return true;
		}
		return false;
	}

    private static String encodeLinks(String text) {
		String encoded = text;
		for (String link : Utils.extractLinks(text)) {
			encoded = encoded.replace(link, Utils.B64Encode(link));
		}
		return encoded;
	}

	private static final Pattern TOKEN = Pattern.compile("\\S+");

	private static String decode(String text) {
		Matcher matcher = TOKEN.matcher(text);
		StringBuilder out = new StringBuilder();

		while (matcher.find()) {
			String token = matcher.group();
			matcher.appendReplacement(out, Matcher.quoteReplacement(
					Utils.isEncodedLink(token) ? Utils.B64Decode(token) : token));
		}

		matcher.appendTail(out);
		return out.toString();
	}

	private static Component transform(Component source) {
		MutableComponent result;

		if (source.getContents() instanceof PlainTextContents plain) {
			result = rewrite(plain.text(), source.getStyle());
		} else if (source.getContents() instanceof TranslatableContents translatable) {
			Object[] args = translatable.getArgs();
			Object[] rewritten = new Object[args.length];

			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Component c) rewritten[i] = transform(c);
				else if (args[i] instanceof String s) rewritten[i] = decode(s);
				else rewritten[i] = args[i];
			}

			result = Component.translatableWithFallback(
							translatable.getKey(), translatable.getFallback(), rewritten)
					.setStyle(source.getStyle());
		} else {
			result = source.plainCopy().setStyle(source.getStyle());
		}

		for (Component sibling : source.getSiblings()) {
			result.append(transform(sibling));
		}

		return result;
	}

	private static MutableComponent rewrite(String text, Style style) {
		String decoded = decode(text);
		Matcher matcher = Utils.LINK.matcher(decoded);

		if (!matcher.find()) return Component.literal(decoded).setStyle(style);
		matcher.reset();

		MutableComponent root = Component.empty().setStyle(style);
		int cursor = 0;

		while (matcher.find()) {
			if (matcher.start() > cursor) {
				root.append(Component.literal(decoded.substring(cursor, matcher.start())));
			}

//			root.append(Component.literal(matcher.group()).withStyle(s -> s
//					.withColor(ChatFormatting.BLUE)
//					.withUnderlined(true)
//			));

			root.append(Component.literal(matcher.group()).setStyle(linkStyle(matcher.group())));

			cursor = matcher.end();
		}

		if (cursor < decoded.length()) {
			root.append(Component.literal(decoded.substring(cursor)));
		}

		return root;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

}
