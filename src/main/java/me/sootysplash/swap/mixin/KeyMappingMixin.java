package me.sootysplash.swap.mixin;

import me.sootysplash.swap.object.KeyPressData;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.sootysplash.swap.AttributeSwapIndicator.*;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

    @Inject(
            method = "lambda$click$0",
            at = @At(value = "HEAD")
    )
    private static void onHandleHead(KeyMapping keyMapping, CallbackInfo ci) {
        for (int i = 0; i < mc.options.keyHotbarSlots.length; i++) {
            KeyMapping hotbarI = mc.options.keyHotbarSlots[i];
            if (hotbarI == keyMapping) {
                hotbarKey2PressTime.put(i, new KeyPressData(i));
            }
        }

        if (keyMapping == mc.options.keyAttack) {
            attack2PressTime.add(System.currentTimeMillis());
        }
    }
}
