package me.sootysplash.swap;

import me.sootysplash.swap.object.AttackKeyPressData;
import me.sootysplash.swap.object.ItemSwapSequence;
import me.sootysplash.swap.object.HotbarKeyPressData;
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

import java.awt.*;
import java.util.*;
import java.util.List;

public class AttributeSwapIndicator implements ModInitializer {
    public static long getInputExpireTime() {
        return 3000;
    }

    public static long getKeyExpireTime() {
        return 1000;
    }

    public static final Minecraft mc = Minecraft.getInstance();
    public static final String MOD_ID = "attribute-swap-indicator";

    public static final Map<Integer, HotbarKeyPressData> hotbarKey2PressTime = new HashMap<>();
    public static final List<AttackKeyPressData> attack2PressTime = new ArrayList<>();

    public static final List<ItemSwapSequence> itemSwaps = new ArrayList<>();

    @Override
    public void onInitialize() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR,
                Identifier.fromNamespaceAndPath(MOD_ID, "before_crosshair"),
                AttributeSwapIndicator::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        internalExtract(graphics);
    }

    private static void internalExtract(GuiGraphicsExtractor graphics) {
//        if (itemSwaps.isEmpty()) {
//            return;
//        }
        graphics.pose().pushMatrix();
//        graphics.pose().scale(0.2f, 0.2f);
        int renderWidth = getWidth(0, 0, Optional.empty());
        int y = graphics.guiHeight() / 2 + 24;
        int originX = graphics.guiWidth() / 2 - renderWidth / 2;
        getWidth(originX, y, Optional.of(graphics));
        graphics.pose().popMatrix();
    }

    private static int getWidth(int startX, int y, Optional<GuiGraphicsExtractor> doRender) {
        int[] lastKey = {-1};
        int[] currentX = {startX};
        Font font = mc.font;
        int itemRenderStride = 18;
        int arrowRenderStride = 14;

        for (ItemSwapSequence iss : itemSwaps) {
            if (lastKey[0] != iss.lastKey()) {
                doRender.ifPresent(graphics -> {
                    graphics.item(iss.lastStack(), currentX[0], y);
                });
                currentX[0] += itemRenderStride;
            }

            boolean goodHotbar = iss.hotbarTick() == iss.addTick();
            boolean goodAttack = iss.attackTick() == iss.addTick();
            boolean successfulSwap = goodAttack && goodHotbar;
            doRender.ifPresent(graphics -> {
                graphics.text(font, "->", currentX[0], y, -1);
                int goodRGB = new Color(0, 255, 0).getRGB();
                int midRGB = new Color(255, 255, 0).getRGB();
                int badRGB = new Color(255, 0, 0).getRGB();
                int downwardsStride = 8;
                int centerTextX = currentX[0] - (int) (arrowRenderStride * 0.75);
                graphics.text(font,
                        successfulSwap ? "✔" : (goodHotbar ? "⚠" : "❌"),
                        currentX[0] - (!successfulSwap && goodHotbar ? 3 : 0),
                        y + downwardsStride,
                        successfulSwap ? goodRGB : (goodHotbar ? midRGB : badRGB));
                graphics.text(font,
                        (iss.addTime() - iss.attackTime()) + "ms",
                        centerTextX,
                        y + downwardsStride * 2,
                        goodAttack ? goodRGB : badRGB);
                graphics.text(font,
                        (iss.addTime() - iss.hotbarTime()) + "ms",
                        centerTextX,
                        y + downwardsStride * 3,
                        goodHotbar ? goodRGB : badRGB);
            });
            currentX[0] += arrowRenderStride;

            doRender.ifPresent(graphics -> {
                graphics.item(iss.newStack(), currentX[0], y);
            });
            lastKey[0] = iss.newKey();
            currentX[0] += itemRenderStride;
        }


        currentX[0] -= startX;
        return currentX[0];
    }

    public static int getCurrentTick() {
        return mc.player != null ? mc.player.tickCount : 0;
    }

    public static ItemStack getForSlot(int slot) {
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
