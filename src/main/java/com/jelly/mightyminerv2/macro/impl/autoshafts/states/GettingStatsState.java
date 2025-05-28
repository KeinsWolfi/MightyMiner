package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.feature.impl.AutoInventory;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;

public class GettingStatsState implements AutoShaftState {

    private  final AutoInventory autoInventory = AutoInventory.getInstance();

    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering getting stats state");
        autoInventory.retrieveSpeedBoost();
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting getting stats state");
    }
}
