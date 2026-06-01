package me.sootysplash.swap.mixin;

import me.sootysplash.swap.Config;
import me.sootysplash.swap.object.AttackKeyPressData;
import me.sootysplash.swap.object.HotbarKeyPressData;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.sootysplash.swap.AttributeSwapIndicator.*;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

    @Unique
    private static boolean hadRightClick = false;

    @Inject(
            method = "lambda$click$0",
            at = @At(value = "HEAD")
    )
    private static void onHandleHead(KeyMapping keyMapping, CallbackInfo ci) {
        if (!Config.getInstance().enabled) {
            return;
        }
        for (int i = 0; i < mc.options.keyHotbarSlots.length; i++) {
            KeyMapping hotbarI = mc.options.keyHotbarSlots[i];
            if (hotbarI == keyMapping) {
                if (hadRightClick) {
                    hadRightClick = false;
                    return;
                }
                hotbarKey2PressTime.put(i, new HotbarKeyPressData(i));
            }
        }

        if (keyMapping == mc.options.keyAttack) {
            attack2PressTime.add(new AttackKeyPressData(System.currentTimeMillis(),
                    getCurrentTick()));
        }

        if (keyMapping == mc.options.keyUse) {// prevent latest swap
            hadRightClick = true;
        }
    }
}
