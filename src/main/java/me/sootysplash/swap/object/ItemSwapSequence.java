package me.sootysplash.swap.object;

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
                               int combo) {
    public ItemSwapSequence(int lastKey,
                            int newKey,
                            long hotbarTime,
                            int hotbarTick,
                            long attackTime,
                            int attackTick,
                            int combo) {
        this(lastKey, newKey, getForSlot(lastKey), getForSlot(newKey), hotbarTime, hotbarTick, attackTime, attackTick, System.currentTimeMillis(), getCurrentTick(), combo);
    }

    public boolean successfulSwap() {
        return hotbarTick() == addTick() && attackTick() == addTick();
    }
}
