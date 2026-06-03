package me.sootysplash.swap.object;

import me.sootysplash.swap.AttributeSwapIndicator;
import net.minecraft.world.item.ItemStack;

import static me.sootysplash.swap.AttributeSwapIndicator.*;

public record ItemSwapSequence(int lastKey,
                               int newKey,
                               ItemStack lastStack,
                               ItemStack newStack,
                               long hotbarTime,
                               int hotbarTick,
                               long attackTime,
                               int attackTick,
                               long addTime,
                               int addTick,
                               long addCutoff,
                               int combo,
                               boolean isHit) {
    public ItemSwapSequence(int lastKey,
                            int newKey,
                            long hotbarTime,
                            int hotbarTick,
                            long attackTime,
                            int attackTick,
                            int combo,
                            boolean isHit) {
        this(lastKey, newKey, getForSlot(lastKey), getForSlot(newKey), hotbarTime, hotbarTick, attackTime, attackTick, System.currentTimeMillis(), getCurrentTick(), tickToTime.getOrDefault(getCurrentTick() - 1, 0L), combo, isHit);
    }
    public ItemSwapSequence(int lastKey,
                            int newKey,
                            ItemStack lastStack,
                            ItemStack newStack,
                            int hotbarTick,
                            int attackTick,
                            int addTick,
                            int combo,
                            boolean isHit) {
        this(lastKey,
                newKey,
                lastStack,
                newStack,
                getApproximateTimeForTick(hotbarTick),
                hotbarTick,
                getApproximateTimeForTick(attackTick),
                attackTick,
                getApproximateTimeForTick(addTick),
                addTick,
                getApproximateTimeForTick(addTick - 1),
                combo,
                isHit
        );
    }

    public static long getApproximateTimeForTick(int tick) {
        int nowTick = getCurrentTick();
        return tickToTime.getOrDefault(nowTick, System.currentTimeMillis()) -
                (nowTick - tick) * 50L; // 1000 ms in a second / 20 ticks a second == 50 ms per tick
    }

    public boolean successfulSwap() {
        return hotbarTick() == addTick() && attackTick() == addTick();
    }
}
