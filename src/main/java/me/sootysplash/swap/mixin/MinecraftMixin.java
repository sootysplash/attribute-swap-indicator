package me.sootysplash.swap.mixin;

import me.sootysplash.swap.object.ItemSwapSequence;
import me.sootysplash.swap.object.KeyPressData;
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
            if (System.currentTimeMillis() - itemSwaps.get(i).addTime() > inputExpireTime) {
                itemSwaps.remove(i);
            }
        }

        KeyPressData selectKPD = new KeyPressData(0, -1, -1);
        for (int i = 0; i < mc.options.keyHotbarSlots.length; i++) {
            KeyPressData keyPressData = hotbarKey2PressTime.get(i);
            if (keyPressData != null) {
                if (selectKPD.otherWasLater(keyPressData)) {
                    selectKPD = keyPressData;
                }
                if (System.currentTimeMillis() - keyPressData.time() > inputExpireTime) {
                    hotbarKey2PressTime.remove(i);
                }
            }
        }

        for (int i = attack2PressTime.size() - 1; i >= 0; i--) {
            if (System.currentTimeMillis() - attack2PressTime.get(i) > inputExpireTime) {
                attack2PressTime.remove(i);
            }
        }

        if (selectKPD.lastKey() == -1
                || selectKPD.lastKey() == selectKPD.key()
                || attack2PressTime.isEmpty()) {
            return;
        }

        long attackTime = attack2PressTime.remove(attack2PressTime.size() - 1);
        attack2PressTime.clear();
        hotbarKey2PressTime.clear();

        itemSwaps.add(new ItemSwapSequence(
                selectKPD.lastKey(),
                selectKPD.key(),
                selectKPD.time(),
                attackTime,
                System.currentTimeMillis()
        ));
    }

}
