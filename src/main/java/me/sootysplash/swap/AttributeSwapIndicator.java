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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;

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

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        internalExtract(graphics);
    }

    private static void internalExtract(GuiGraphicsExtractor graphics) {
        Config config = Config.getInstance();
        if (!config.enabled) {
            return;
        }
        double scale = config.scale;
        double inverseScale = 1 / scale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(config.xOffset, config.yOffset);
        graphics.pose().scale((float) scale, (float) scale);
        int renderWidth = getWidth(config, 0, 0, itemSwaps, true, Optional.empty())[0];
        int y = graphics.guiHeight() / 2 + 24;
        int originX = graphics.guiWidth() / 2;
        getWidth(config, (int) (originX * inverseScale) - renderWidth / 2 + 4, (int) (y * inverseScale), itemSwaps, true, Optional.of(graphics));
        graphics.pose().popMatrix();
    }

    public static int[] getWidth(Config config,
                                  int startX,
                                  int y,
                                  List<ItemSwapSequence> listISS,
                                  boolean useLimits,
                                  Optional<GuiGraphicsExtractor> doRender) {
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
                    graphics.item(iss.lastStack(), counters[widthI], y);
                });
                counters[widthI] += itemRenderStride;
            } else {
//                isSequence.set(true);
                counters[sequenceI] += 1;
                counters[lastCountedTypeI] = sequenceI;
            }

            boolean goodHotbar = iss.hotbarTick() == iss.addTick();
            boolean goodAttack = iss.attackTick() == iss.addTick();
            boolean successfulSwap = goodAttack && goodHotbar;
            doRender.ifPresent(graphics -> {
                graphics.text(font, "--", counters[widthI], y, -1
                        /*(isSequence.get() ? Color.YELLOW : Color.CYAN).getRGB()*/);

                int goodRGB = new Color(0, 255, 0).getRGB();
                int badRGB = new Color(255, 0, 0).getRGB();
                int downwardsStride = 10;
                int centerTextX = counters[widthI] - (int) (arrowRenderStride * 0.8);
                long addCutOff = tickToTime.getOrDefault(iss.addTick() - 1, 0L);

                String text = successfulSwap ? "✔" : ((!goodAttack
                                ? "⚔:+" + (addCutOff - iss.attackTime())
                                : "→:+" + (addCutOff - iss.hotbarTime())) + "ms");

                graphics.text(
                        font,
                        text,
                        successfulSwap ? counters[widthI] : centerTextX,
                        y + downwardsStride * (successfulSwap ? 1 : 2),
                        successfulSwap ? goodRGB : badRGB
                );

            });
            counters[widthI] += arrowRenderStride;

            doRender.ifPresent(graphics -> {
                graphics.item(iss.newStack(), counters[widthI], y);
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
        return stack;
    }
}
