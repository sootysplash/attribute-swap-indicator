package me.sootysplash.swap.mixin;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DeltaTracker.Timer.class)
public interface DeltaTracker$TimerAccessor {

    @Accessor
    float getMsPerTick();

}
