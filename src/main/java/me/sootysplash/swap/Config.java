package me.sootysplash.swap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Path file = FabricLoader.getInstance().getConfigDir().resolve("attribute-swap-indicator.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance;

    public boolean enabled = true;

    public double scale = 1;
    public int xOffset = 0;
    public int yOffset = 0;

    public float inputExpireSeconds = 3;
    public float keyExpireSeconds = 0.1f;

    public int standaloneSwaps = 2;
    public int sequentialSwaps = 3;


    public void save() {
        AttributeSwapIndicator.setupCleanupTick();
        try {
            Files.writeString(file, GSON.toJson(this));
        } catch (Throwable e) {
            AttributeSwapIndicator.LOGGER.warn("{} could not save the config.", AttributeSwapIndicator.LOGGER.getName());
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            try {
                instance = GSON.fromJson(Files.readString(file), Config.class);
            } catch (Throwable exception) {
                AttributeSwapIndicator.LOGGER.warn("{} couldn't load the config, using defaults.", AttributeSwapIndicator.LOGGER.getName());
                instance = new Config();
            }
            AttributeSwapIndicator.setupCleanupTick();
        }

        return instance;
    }
}
