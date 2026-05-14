package me.sootysplash.swap.object;

import net.minecraft.world.item.ItemStack;

public record ItemSwapSequence(int lastKey,
                               int newKey,
                               ItemStack lastStack,
                               ItemStack newStack,
                               long hotbarTime,
                               int hotbarTick,
                               long attackTime,
                               int attackTick,
                               long addTime,
                               int addTick) {
}
