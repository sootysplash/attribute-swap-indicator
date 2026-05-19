package me.sootysplash.swap;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Config config = Config.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.nullToEmpty("Config"))
                    .setSavingRunnable(config::save);

            ConfigEntryBuilder cfgent = builder.entryBuilder();
            ConfigCategory behavior = builder.getOrCreateCategory(Component.nullToEmpty("Behavior"));


            behavior.addEntry(cfgent.startBooleanToggle(Component.nullToEmpty("Enabled"), config.enabled)
                    .setDefaultValue(true)
                    .setTooltip(Component.nullToEmpty("Modify hitbox rendering?"))
                    .setSaveConsumer(newValue -> config.enabled = newValue)
                    .build());


            behavior.addEntry(cfgent.startFloatField(Component.nullToEmpty("Input Expire Seconds"), config.inputExpireSeconds)
                    .setDefaultValue(3)
                    .setMin(0)
                    .setTooltip(Component.nullToEmpty("The base hitbox's color"))
                    .setSaveConsumer(newValue -> config.inputExpireSeconds = newValue)
                    .build());


            behavior.addEntry(cfgent.startFloatField(Component.nullToEmpty("Key Expire Seconds"), config.keyExpireSeconds)
                    .setDefaultValue(1)
                    .setMin(0)
                    .setTooltip(Component.nullToEmpty("The hitbox eye height color"))
                    .setSaveConsumer(newValue -> config.keyExpireSeconds = newValue)
                    .build());



            ConfigCategory limits = builder.getOrCreateCategory(Component.nullToEmpty("Limits"));


            limits.addEntry(cfgent.startIntField(Component.nullToEmpty("Standalone Swaps"), config.standaloneSwaps)
                    .setMin(1)
                    .setDefaultValue(2)
                    .setTooltip(Component.nullToEmpty("The base hitbox's color"))
                    .setSaveConsumer(newValue -> config.standaloneSwaps = newValue)
                    .build());


            limits.addEntry(cfgent.startIntField(Component.nullToEmpty("Sequential Swaps"), config.sequentialSwaps)
                    .setMin(1)
                    .setDefaultValue(3)
                    .setTooltip(Component.nullToEmpty("The hitbox eye height color"))
                    .setSaveConsumer(newValue -> config.sequentialSwaps = newValue)
                    .build());


            ConfigCategory display = builder.getOrCreateCategory(Component.nullToEmpty("Display"));


            display.addEntry(cfgent.startDoubleField(Component.nullToEmpty("Scale"), config.scale)
                    .setMin(0.1)
                    .setMax(2)
                    .setDefaultValue(1)
                    .setTooltip(Component.nullToEmpty("The width of the hitbox lines"))
                    .setSaveConsumer(newValue -> config.scale = newValue)
                    .build());

            display.addEntry(cfgent.startIntField(Component.nullToEmpty("X Offset"), config.xOffset)
                    .setMin(0)
                    .setMax(100)
                    .setDefaultValue(0)
                    .setTooltip(Component.nullToEmpty("The distance for Line Width 2 to be used"))
                    .setSaveConsumer(newValue -> config.xOffset = newValue)
                    .build());

            display.addEntry(cfgent.startIntField(Component.nullToEmpty("Y Offset"), config.yOffset)
                    .setMin(0)
                    .setMax(100)
                    .setDefaultValue(0)
                    .setTooltip(Component.nullToEmpty("The width of the hitbox lines beyond the set distance"))
                    .setSaveConsumer(newValue -> config.yOffset = newValue)
                    .build());

            return builder.build();
        };
    }
}
