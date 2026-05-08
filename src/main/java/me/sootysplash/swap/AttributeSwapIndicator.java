package me.sootysplash.swap;

import me.sootysplash.swap.object.ItemSwapSequence;
import me.sootysplash.swap.object.KeyPressData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeSwapIndicator implements ModInitializer {
    public static final long inputExpireTime = 500;

    public static final Minecraft mc = Minecraft.getInstance();
    public static final String MOD_ID = "attribute-swap-indicator";

    public static final Map<Integer, KeyPressData> hotbarKey2PressTime = new HashMap<>();
    public static final List<Long> attack2PressTime = new ArrayList<>();

    public static final List<ItemSwapSequence> itemSwaps = new ArrayList<>();

    @Override
    public void onInitialize() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR,
                Identifier.fromNamespaceAndPath(MOD_ID, "before_chat"),
                AttributeSwapIndicator::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        for (int i = 0; i < itemSwaps.size(); i++) {
            boolean hasNext = i < itemSwaps.size() - 1;
            ItemSwapSequence iss = itemSwaps.get(i);
        }
    }
}
