package me.sootysplash.swap.mixin;

import me.sootysplash.swap.AttributeSwapIndicator;
import me.sootysplash.swap.Config;
import me.sootysplash.swap.object.AttackKeyPressData;
import me.sootysplash.swap.object.ItemSwapSequence;
import me.sootysplash.swap.object.HotbarKeyPressData;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static me.sootysplash.swap.AttributeSwapIndicator.*;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
            method = "handleKeybinds",
            at = @At("RETURN")
    )
    private void onInputs(CallbackInfo ci) {
        tickToTime.remove(cleanupTick++);
        tickToTime.put(getCurrentTick(), System.currentTimeMillis());
        Config config = Config.getInstance();
        if (!config.enabled) {
            return;
        }
        for (int i = itemSwaps.size() - 1; i >= 0; i--) {
            if (System.currentTimeMillis() - itemSwaps.get(i).addTime() > AttributeSwapIndicator.getInputExpireTime()) {
                itemSwaps.remove(i);
            }
        }

        HotbarKeyPressData selectKPD = new HotbarKeyPressData(0, 0, -1, -1);
        for (int i = 0; i < mc.options.keyHotbarSlots.length; i++) {
            HotbarKeyPressData keyPressData = hotbarKey2PressTime.get(i);
            if (keyPressData != null) {
                if (selectKPD.otherWasLater(keyPressData)) {
                    selectKPD = keyPressData;
                }
                if (System.currentTimeMillis() - keyPressData.time() > AttributeSwapIndicator.getKeyExpireTime()) {
                    hotbarKey2PressTime.remove(i);
                }
            }
        }

        for (int i = attack2PressTime.size() - 1; i >= 0; i--) {
            if (System.currentTimeMillis() - attack2PressTime.get(i).time() > AttributeSwapIndicator.getKeyExpireTime()) {
                attack2PressTime.remove(i);
            }
        }

        if (selectKPD.lastKey() == -1
                || selectKPD.lastKey() == selectKPD.key()
                || attack2PressTime.isEmpty()) {
            return;
        }

        int combo = 1;
        for (int i = itemSwaps.size() - 1; i >= 0; i--) {
            ItemSwapSequence current = itemSwaps.get(i);
            if (current.lastKey() == selectKPD.lastKey()
            && current.newKey() == selectKPD.key()
            && current.successfulSwap()) {
                combo = current.combo() + 1;
                itemSwaps.remove(i);
            }
        }

        AttackKeyPressData attackTime = attack2PressTime.remove(attack2PressTime.size() - 1);
        attack2PressTime.clear();
        hotbarKey2PressTime.clear();

        itemSwaps.add(new ItemSwapSequence(
                selectKPD.lastKey(),
                selectKPD.key(),
                selectKPD.time(),
                selectKPD.tick(),
                attackTime.time(),
                attackTime.tick(),
                combo
        ));

        int[] counters = AttributeSwapIndicator.getWidth(
                config,
                0,
                0,
                itemSwaps,
                false,
                Optional.empty()
        );

        int lastCountedType = counters[lastCountedTypeI];
        int invertLastCountedType = invertStandAndSeq[lastCountedType];
        int countToRemove = counters[invertLastCountedType];
        counters[invertLastCountedType] = 0;
        if (countToRemove > 0) {
            itemSwaps.subList(0, countToRemove).clear();
        }

        int[] configOptions = new int[]{0, config.standaloneSwaps, config.sequentialSwaps};
        for (int i : new int[]{standaloneI, sequenceI}) {
            while (configOptions[i] < counters[i]) {
                itemSwaps.remove(0);
                counters[i]--;
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void onTick(CallbackInfo ci) {
        currentTick++;
        me.sootysplash.swap.Testing.onInputs();
    }

}
