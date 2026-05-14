package me.sootysplash.swap.object;

import static me.sootysplash.swap.AttributeSwapIndicator.*;

public record HotbarKeyPressData(long time, int tick, int key, int lastKey) {

    public HotbarKeyPressData(int key) {
        this(
                System.currentTimeMillis(),
                getCurrentTick(),
                key,
                mc.player != null ? mc.player.getInventory().getSelectedSlot() : -1
        );
    }

    public boolean otherWasLater(HotbarKeyPressData other) {
        return other.time > this.time;
    }

}
