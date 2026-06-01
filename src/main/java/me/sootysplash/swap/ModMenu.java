package me.sootysplash.swap;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.sootysplash.swap.object.ItemSwapSequence;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.sootysplash.swap.AttributeSwapIndicator.*;

public class ModMenu implements ModMenuApi {

    private static double[] getScaleLimits() {
        return new double[]{0.2, 5};
    }

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
                    .setTooltip(Component.nullToEmpty("Render the HUD widget?"))
                    .setSaveConsumer(newValue -> config.enabled = newValue)
                    .build());


            behavior.addEntry(cfgent.startFloatField(Component.nullToEmpty("Input Expire Seconds"), config.inputExpireSeconds)
                    .setDefaultValue(3)
                    .setMin(0)
                    .setTooltip(Component.nullToEmpty("Seconds for inputs to expire"))
                    .setSaveConsumer(newValue -> config.inputExpireSeconds = newValue)
                    .build());


            behavior.addEntry(cfgent.startFloatField(Component.nullToEmpty("Swap Expire Seconds"), config.keyExpireSeconds)
                    .setDefaultValue(1)
                    .setMin(0)
                    .setTooltip(Component.nullToEmpty("Seconds for swaps to expire visually"))
                    .setSaveConsumer(newValue -> config.keyExpireSeconds = newValue)
                    .build());



            ConfigCategory limits = builder.getOrCreateCategory(Component.nullToEmpty("Limits"));


            limits.addEntry(cfgent.startIntField(Component.nullToEmpty("Standalone Swaps"), config.standaloneSwaps)
                    .setMin(1)
                    .setDefaultValue(2)
                    .setTooltip(Component.nullToEmpty("Maximum standalone (separate) attribute swaps to display"))
                    .setSaveConsumer(newValue -> config.standaloneSwaps = newValue)
                    .build());


            limits.addEntry(cfgent.startIntField(Component.nullToEmpty("Sequential Swaps"), config.sequentialSwaps)
                    .setMin(1)
                    .setDefaultValue(3)
                    .setTooltip(Component.nullToEmpty("Maximum sequential (chained) attribute swaps to display"))
                    .setSaveConsumer(newValue -> config.sequentialSwaps = newValue)
                    .build());


            ConfigCategory display = builder.getOrCreateCategory(Component.nullToEmpty("Display"));

            display.addEntry(new BooleanListEntry(Component.nullToEmpty("Edit HUD"), false, cfgent.getResetButtonKey(), null, null, () ->
                    Optional.of(mc.level == null
                            ? new Component[]{Component.nullToEmpty("You can only edit the HUD in a world")}
                            : new Component[]{Component.nullToEmpty("Press Escape to cancel"), Component.nullToEmpty("Press Enter to save")}
                    )) {
                @Override
                public Component getYesNoText(boolean bool) {
                    if (bool) {
                        ((Button) this.children().get(0)).onPress(null); // click buttonWidget for true -> false
                        if (mc.level != null) {
                            openHudEditor();
                        }
                    }
                    return Component.literal("Open Editor");
                }
            });

            display.addEntry(cfgent.startDoubleField(Component.nullToEmpty("Scale"), config.scale)
                    .setMin(getScaleLimits()[0])
                    .setMax(getScaleLimits()[1])
                    .setDefaultValue(1)
                    .setTooltip(Component.nullToEmpty("The multiplier for the size of the widget"))
                    .setSaveConsumer(newValue -> config.scale = newValue)
                    .build());

            display.addEntry(cfgent.startIntField(Component.nullToEmpty("X Offset"), config.xOffset)
                    .setDefaultValue(0)
                    .setTooltip(Component.nullToEmpty("The horizontal offset for the widget"))
                    .setSaveConsumer(newValue -> config.xOffset = newValue)
                    .build());

            display.addEntry(cfgent.startIntField(Component.nullToEmpty("Y Offset"), config.yOffset)
                    .setDefaultValue(0)
                    .setTooltip(Component.nullToEmpty("The vertical offset for the widget"))
                    .setSaveConsumer(newValue -> config.yOffset = newValue)
                    .build());

            return builder.build();
        };
    }

    private static void openHudEditor() {
        Config config = Config.getInstance();
        mc.execute(() -> {
            int[] currentOffset = new int[]{config.xOffset, config.yOffset};
            double[] currentScale = new double[]{config.scale};
            mc.setScreen(new Screen(Component.nullToEmpty("attribute-swap-indicator.hud-editor")) {
                {
                    List<ItemSwapSequence> iss = new ArrayList<>();

                    ItemStack oneA = new ItemStack(Items.DIAMOND_SWORD);
                    ItemStack twoA = new ItemStack(Items.NETHERITE_SPEAR);
                    ItemStack threeA = new ItemStack(Items.MACE);
                    int nowTick = getCurrentTick() - cleanupTick;


                    iss.add(new ItemSwapSequence(0, 1, oneA, twoA, nowTick - 4, nowTick - 4, nowTick - 4, 2));
                    iss.add(new ItemSwapSequence(1, 4, twoA, threeA, nowTick - 4, nowTick - 2, nowTick - 2, 1));

                    addRenderableOnly((graphics, _, _, _) -> {
                        int x = graphics.guiWidth() / 2;
                        int y = graphics.guiHeight() / 2;
                        int col = Color.GRAY.getRGB();
                        graphics.verticalLine(x, 0, y * 2, col);
                        graphics.horizontalLine(0, x * 2, y, col);
                    });

                    DragWidget[] scaleWidget = new DragWidget[1];

                    DragWidget mainWidget = addRenderableWidget(new DragWidget(0, 0, 0, 0) {

                        @Override
                        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                            if (isDraggingMovement) {
                                currentOffset[0] = (int) (startingPos[0] + mouseX - beganDragAt[0]);
                                currentOffset[1] = (int) (startingPos[1] + mouseY - beganDragAt[1]);
                            }

                            double scale = currentScale[0];
                            double inverseScale = 1 / currentScale[0];

                            int[] origXY = getOriginXY(graphics);
                            int width = AttributeSwapIndicator.getWidth(4, 4, 0, 0, iss, false, Optional.empty())[0];
                            int[] drawXY = new int[]{
                                    getXAfterWidth(currentOffset[0] * inverseScale + origXY[0] * inverseScale, width),
                                    (int) (currentOffset[1] * inverseScale + origXY[1] * inverseScale)};
                            int height = 36;

                            int white = Color.WHITE.getRGB();
                            int bgX, bgY, bgW, bgH;

                            this.setX(bgX = getXAfterWidth(currentOffset[0] + origXY[0] - width * 0.05 // looked 10% off, so half to center
                                    , (int) (width * scale)));
                            this.setY(bgY = (int) (currentOffset[1] + origXY[1] - height * 0.1) // looked 20% off, so half to center
                            );
                            this.setWidth(bgW = (int) (width * scale));
                            this.setHeight(bgH = (int) (height * scale));

                            graphics.fill(bgX, bgY, bgX + bgW, bgY + bgH, new Color(255, 255, 255, 90).getRGB());
                            graphics.outline(bgX, bgY, bgW, bgH, white);

                            graphics.pose().pushMatrix();
                            applyTransforms(graphics.pose(), (float) currentScale[0]);
                            AttributeSwapIndicator.getWidth(4, 4,
                                    drawXY[0],
                                    drawXY[1],
                                    iss, false, Optional.of(graphics));
                            graphics.pose().popMatrix();

                            DragWidget sw = scaleWidget[0];
                            graphics.fill(sw.getX(), sw.getY(), sw.getX() + sw.getWidth(), sw.getY() + sw.getHeight(), new Color(255, 0, 0, 128).getRGB());

                            /*if (currentScale[0] < 0) {
                                this.setX(bgX + bgW);
                                this.setY(bgY + bgH);
                                this.setWidth(-bgW);
                                this.setHeight(-bgH);
                            }*/
                        }

                        @Override
                        protected boolean allowPreClick(MouseButtonEvent event) {
                            DragWidget sw = scaleWidget[0];
                            double mx = event.x();
                            double my = event.y();
                            if (sw.getX() < mx && sw.getY() < my &&
                            sw.getX() + sw.getWidth() > mx && sw.getY() + sw.getHeight() > my) {
                                sw.onClick(event, false);
                                return false;
                            }
                            return true;
                        }

                        @Override
                        protected void afterClick(final MouseButtonEvent event) {
                            startingPos[0] = currentOffset[0];
                            startingPos[1] = currentOffset[1];
                        }

                        @Override
                        public void onRelease(final MouseButtonEvent event) {
                            scaleWidget[0].onRelease(event);
                            super.onRelease(event);
                        }

                        private final double[] startingPos = new double[2];

                    });

                    scaleWidget[0] = addRenderableWidget(new DragWidget(0, 0, 0, 0) {

                        private final double[] startingScale = new double[1];

                        @Override
                        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                            if (isDraggingMovement) {
                                double rawNewScale = startingScale[0] + (mouseX - beganDragAt[0] + mouseY - beganDragAt[1]) / 100.0;
                                currentScale[0] = Math.max(getScaleLimits()[0], Math.min(getScaleLimits()[1], rawNewScale));
                            }
                            int x = mainWidget.getX();
                            int y = mainWidget.getY();
                            int w = mainWidget.getWidth();
                            int h = mainWidget.getHeight();

//                            graphics.fill(x, y, x + w, y + h, new Color(0, 0, 0, 190).getRGB());

                            int longerSide = (int) Math.max(Math.max(Math.abs(w) * 0.1, Math.abs(h) * 0.1), 5); // 0.1 -> 10% of the corner
                            int padding = 1;
                            int rX = x + w - longerSide - padding;
                            int rY = y + h - longerSide - padding;
                            /*if (currentScale[0] < 0) {
                                rX = x;
                                rY = y;
                            }*/
                            this.setX(rX);
                            this.setY(rY);
                            this.setWidth(longerSide);
                            this.setHeight(longerSide);

                            graphics.fill(rX, rY, rX + longerSide, rY + longerSide, Color.CYAN.getRGB());
                        }

                        @Override
                        protected boolean allowPreClick(MouseButtonEvent event) {
                            return true;
                        }

                        @Override
                        protected void afterClick(final MouseButtonEvent event) {
                            startingScale[0] = currentScale[0];
                        }
                    });
                }

                @Override
                public boolean keyPressed(final KeyEvent event) {
                    if (event.isEscape()) {
                        this.onClose();
                        return true;
                    }
                    if (event.isConfirmation()) {
                        this.onClose();
                        config.xOffset = currentOffset[0];
                        config.yOffset = currentOffset[1];
                        config.scale = currentScale[0];
                        return true;
                    }
                    return super.keyPressed(event);
                }

                @Override
                public void onClose() {
                    mc.setScreen(new ModMenu().getModConfigScreenFactory().create(null));
                }

                @Override
                public boolean isPauseScreen() {
                    return false;
                }
            });
        });
    }

    private abstract static class DragWidget extends AbstractWidget {
        public DragWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) {}

        protected boolean isDraggingMovement = false;
        protected double[] beganDragAt = new double[2];

        @Override
        protected abstract void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a);

        protected abstract boolean allowPreClick(final MouseButtonEvent event);

        protected abstract void afterClick(final MouseButtonEvent event);

        @Override
        public void onClick(final MouseButtonEvent event, final boolean doubleClick) {
            if (!allowPreClick(event)) {
                return;
            }
            isDraggingMovement = true;
            beganDragAt[0] = event.x();
            beganDragAt[1] = event.y();
            afterClick(event);
        }

        @Override
        public void onRelease(final MouseButtonEvent event) {
            isDraggingMovement = false;
        }

    }
}
