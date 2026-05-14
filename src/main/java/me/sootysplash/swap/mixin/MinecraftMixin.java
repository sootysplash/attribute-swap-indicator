package me.sootysplash.swap.mixin;

import me.sootysplash.swap.AttributeSwapIndicator;
import me.sootysplash.swap.object.AttackKeyPressData;
import me.sootysplash.swap.object.ItemSwapSequence;
import me.sootysplash.swap.object.HotbarKeyPressData;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.sootysplash.swap.AttributeSwapIndicator.*;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
            method = "handleKeybinds",
            at = @At("HEAD")
    )
    private void onInputs(CallbackInfo ci) {
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

        AttackKeyPressData attackTime = attack2PressTime.remove(attack2PressTime.size() - 1);
        attack2PressTime.clear();
        hotbarKey2PressTime.clear();

        itemSwaps.add(new ItemSwapSequence(
                selectKPD.lastKey(),
                selectKPD.key(),
                getForSlot(selectKPD.lastKey()),
                getForSlot(selectKPD.key()),
                selectKPD.time(),
                selectKPD.tick(),
                attackTime.time(),
                attackTime.tick(),
                System.currentTimeMillis(),
                getCurrentTick()
        ));
    }

}
