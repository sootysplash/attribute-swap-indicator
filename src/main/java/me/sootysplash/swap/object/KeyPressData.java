package me.sootysplash.swap.object;

import static me.sootysplash.swap.AttributeSwapIndicator.mc;

public record KeyPressData(long time, int key, int lastKey) {

    public KeyPressData(int key) {
        this(System.currentTimeMillis(), key, mc.player != null ? mc.player.getInventory().getSelectedSlot() : -1);
    }

    public boolean otherWasLater(KeyPressData other) {
        return other.time > this.time;
    }

}
