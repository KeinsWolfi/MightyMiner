package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;

public class StartingState implements AutoShaftState {
    @Override
    public void onStart(ShaftMacro macro) {
        log("Entrring starting state");
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if (!InventoryUtil.areItemsInHotbar(macro.getNecessaryItems())) {
            logError("Not all necessary items are in the hotbar. Please put the following items in your hotbar: " + macro.getNecessaryItems());
        }
        return macro.getMiningSpeed() == 0 ? new GettingStatsState() : new PathingToVeinState();
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting starting state");
    }
}
