package com.nb.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {

    public enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    /**
     * Mutable with field initialisers on purpose: Gson leaves fields absent from the JSON
     * at their initialised value, so adding an option later cannot silently switch off a
     * feature for anyone with an existing config file. A record would default them to false.
     */
    public static final class Data {
        public boolean encode = true;
        public boolean decode = true;
        public boolean preview = true;

        public int linkColour = 0x4f89d9;

        public Corner corner = Corner.BOTTOM_RIGHT;
        public int offsetX = 4;
        public int offsetY = 4;

        public int sizeVertical = 300;
        public int sizeHorizontal = 300;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve(LinkEncoderClient.MOD_ID + ".json");

    private static Data data = new Data();

    private Config() {}

    public static Data get() {
        return data;
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }

        try {
            Data read = GSON.fromJson(Files.readString(PATH), Data.class);
            if (read != null) data = sanitise(read);
        } catch (Exception e) {
            LinkEncoderClient.LOGGER.warn("config unreadable, keeping defaults", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(data));
        } catch (IOException e) {
            LinkEncoderClient.LOGGER.warn("could not write config", e);
        }
    }

    /** A hand-edited file can still contain nulls or nonsense. */
    private static Data sanitise(Data read) {
        read.linkColour &= 0xFFFFFF;
        if (read.corner == null) read.corner = Corner.BOTTOM_RIGHT;
        read.offsetX = Math.max(0, read.offsetX);
        read.offsetY = Math.max(0, read.offsetY);
        return read;
    }
}