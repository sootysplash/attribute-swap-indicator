package me.sootysplash.swap;

import me.sootysplash.swap.mixin.DeltaTracker$TimerAccessor;
import me.sootysplash.swap.object.AttackKeyPressData;
import me.sootysplash.swap.object.ItemSwapSequence;
import me.sootysplash.swap.object.HotbarKeyPressData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;

// todo, record video for showcase
public class AttributeSwapIndicator implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("AttributeSwapIndicator");

    public static int getInputExpireTime() {
        return Math.round(Config.getInstance().inputExpireSeconds * 1000);
    }

    public static int getKeyExpireTime() {
        return Math.round(Config.getInstance().keyExpireSeconds * 1000);
    }

    public static void setupCleanupTick() {
        double msPerTick = 50;
        if (mc.getDeltaTracker() instanceof DeltaTracker.Timer timer) {
            msPerTick = ((DeltaTracker$TimerAccessor) timer).getMsPerTick();
        }
        tickToTime.clear();
        cleanupTick = (int) (getCurrentTick() - Math.ceil(getInputExpireTime() / msPerTick) * 2);
    }

    public static final Minecraft mc = Minecraft.getInstance();
    public static final String MOD_ID = "attribute-swap-indicator";

    public static int currentTick = 0;
    public static int cleanupTick = 0;
    public static final Map<Integer, Long> tickToTime = new HashMap<>();

    public static final Map<Integer, HotbarKeyPressData> hotbarKey2PressTime = new HashMap<>();
    public static final List<AttackKeyPressData> attack2PressTime = new ArrayList<>();

    public static final List<ItemSwapSequence> itemSwaps = new ArrayList<>();

    public static final int widthI = 0;
    public static final int standaloneI = 1;
    public static final int sequenceI = 2;
    public static final int lastCountedTypeI = 3;
    public static final int[] invertStandAndSeq = {0, sequenceI, standaloneI, 0};

    @Override
    public void onInitialize() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR,
                Identifier.fromNamespaceAndPath(MOD_ID, "before_crosshair"),
                AttributeSwapIndicator::extract);
        setupCleanupTick();
    }

    private static void extract(GuiGraphics graphics, DeltaTracker tickCounter) {
        internalExtract(graphics);
    }

    private static void internalExtract(GuiGraphics graphics) {
        Config config = Config.getInstance();
        if (!config.enabled) {
            return;
        }
        double scale = config.scale;
        double inverseScale = 1 / scale;
        graphics.pose().pushMatrix();
        applyTransforms(graphics.pose(), (float) scale);
        int renderWidth = getWidth(config, 0, 0, itemSwaps, true, Optional.empty())[0];
        int[] xy = getOriginXY(graphics);
        getWidth(config,
                getXAfterWidth(config.xOffset * inverseScale + xy[0] * inverseScale, renderWidth), (int) (config.yOffset * inverseScale + xy[1] * inverseScale),
                itemSwaps, true, Optional.of(graphics));
        graphics.pose().popMatrix();
    }

    public static void applyTransforms(Matrix3x2fStack stack, float scale) {
        stack.scale(scale, scale);
    }

    public static final int magicNumberOffsetForCentering = 4;

    public static int getXAfterWidth(double x, int width) {
        return (int) (x) - width / 2 + magicNumberOffsetForCentering;
    }

    public static int[] getOriginXY(GuiGraphics graphics) {
        return new int[]{graphics.guiWidth() / 2, graphics.guiHeight() / 2 + 24};
    }

    public static int[] getWidth(Config config,
                                  int startX,
                                  int y,
                                  List<ItemSwapSequence> listISS,
                                  boolean useLimits,
                                  Optional<GuiGraphics> doRender) {
        int successColor = config.successColor;
        int failureColor = config.failureColor;
        int[] lastKey = {-1};
        Font font = mc.font;
        int itemRenderStride = 28;
        int arrowRenderStride = 20;
        int[] counters = {startX, 0, 0, standaloneI};

        for (int i = 0; i < listISS.size(); i++) {
            ItemSwapSequence iss = listISS.get(i);
//            java.util.concurrent.atomic.AtomicBoolean isSequence = new AtomicBoolean();
            if (lastKey[0] != iss.lastKey()) {
                int currentType = standaloneI;
                if (i < listISS.size() - 1 && iss.newKey() == listISS.get(i + 1).lastKey()) {
                    currentType = sequenceI;
//                    isSequence.set(true);
                }
                counters[currentType] += 1;
                counters[lastCountedTypeI] = currentType;
                if (counters[standaloneI] - 1 == config.standaloneSwaps && useLimits) {
                    break;
                }
                doRender.ifPresent(graphics -> {
                    graphics.renderItem(iss.lastStack(), counters[widthI], y);
                    graphics.renderItemDecorations(font, iss.lastStack(), counters[widthI], y);
                });
                counters[widthI] += itemRenderStride;
            } else {
//                isSequence.set(true);
                counters[sequenceI] += 1;
                counters[lastCountedTypeI] = sequenceI;
            }

            boolean goodAttack = iss.attackTick() == iss.addTick();
            boolean successfulSwap = iss.successfulSwap();
            doRender.ifPresent(graphics -> {
                String divider = "->";
                int dividerWidth = font.width(divider);
                int currWidth = counters[widthI];
                graphics.drawString(font, divider, currWidth, y, -1
                        /*(isSequence.get() ? Color.YELLOW : Color.CYAN).getRGB()*/);

                int downwardsStride = 10;

                String topText = "✔";
                String bottomText = null;

                if (!successfulSwap) {
                    topText = "";
                    bottomText = !goodAttack ? "⚔" : "→";
                    if (config.showTimings) {
                        bottomText += ":+" + (!goodAttack
                                ? iss.addCutoff() - iss.attackTime()
                                : iss.addCutoff() - iss.hotbarTime()) + "ms";

                    }
                }

                if (config.showHitIndicator) {
                    // every other icon looks so bad
                    topText += "⌖";
                }

                int targetStart = 0;
                if (!topText.isBlank()) {
                    String firstChar = topText.substring(0, 1);
                    int initialTopTextW = dividerWidth / 2 - font.width(topText) / 2;
                    if (successfulSwap) {
                        targetStart = 1;
                        graphics.drawString(
                                font,
                                firstChar,
                                currWidth + initialTopTextW,
                                y + downwardsStride,
                                successColor
                        );
                    }
                    if (config.showHitIndicator) {
                        graphics.drawString(
                                font,
                                topText.substring(targetStart, targetStart + 1),
                                currWidth + initialTopTextW + (successfulSwap ? font.width(firstChar) : 0),
                                y + downwardsStride,
                                iss.isHit() ? successColor : failureColor
                        );
                    }
                }


                if (bottomText != null) {
                    graphics.drawString(
                            font,
                            bottomText,
                            currWidth + dividerWidth / 2 - font.width(bottomText) / 2,
                            y + downwardsStride * 2,
                            failureColor
                    );
                }

                if (iss.combo() > 1 && successfulSwap) {
                    String comboStr = "x" + iss.combo();
                    graphics.drawString(
                            font,
                            comboStr,
                            currWidth + dividerWidth / 2 - font.width(comboStr) / 2,
                            y + downwardsStride * 2,
                            successColor
                    );
                }

            });
            counters[widthI] += arrowRenderStride;

            doRender.ifPresent(graphics -> {
                graphics.renderItem(iss.newStack(), counters[widthI], y);
                graphics.renderItemDecorations(font, iss.newStack(), counters[widthI], y);
            });
            lastKey[0] = iss.newKey();
            counters[widthI] += itemRenderStride;

            if (counters[sequenceI] == config.sequentialSwaps && useLimits) {
                break;
            }
        }

        counters[widthI] -= startX;
        return counters;
    }

    public static int getCurrentTick() {
        return currentTick;
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
        return stack.copy();
    }
}
