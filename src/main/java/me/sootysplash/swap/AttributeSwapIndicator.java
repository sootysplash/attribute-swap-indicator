package me.sootysplash.swap;

import me.sootysplash.swap.object.ItemSwapSequence;
import me.sootysplash.swap.object.KeyPressData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
                Identifier.fromNamespaceAndPath(MOD_ID, "before_crosshair"),
                AttributeSwapIndicator::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (itemSwaps.isEmpty()) {
            return;
        }
        int lastKey = -1;
        int currentX = 20;
        int y = 20;
        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < itemSwaps.size(); i++) {
            ItemSwapSequence iss = itemSwaps.get(i);

            if (lastKey != iss.lastKey()) {
                graphics.item(getForSlot(iss.lastKey()), currentX, y);
                currentX += 32;
            }


            graphics.text(font, "->" , currentX, y, -1);
            currentX += 32;

            graphics.item(getForSlot(lastKey = iss.newKey()), currentX, y);
            currentX += 32;
        }

    }

    private static ItemStack getForSlot(int slot) {
        Item emptyItem = Items.BARRIER;
        if (mc.player == null) {
            return new ItemStack(emptyItem);
        }
        SlotAccess itemSlot = mc.player.getInventory().getSlot(slot);
        if (itemSlot == null) {
            return new ItemStack(emptyItem);
        }
        ItemStack stack = itemSlot.get();
        if (stack.isEmpty()) {
            return new ItemStack(emptyItem);
        }
        return stack;

    }
}
