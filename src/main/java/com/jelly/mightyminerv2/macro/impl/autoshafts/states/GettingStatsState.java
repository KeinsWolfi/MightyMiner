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
        if (autoInventory.isRunning()) {
            return this;
        }

        if (autoInventory.sbSucceeded()) {
            int[] sb = autoInventory.getSpeedBoostValues();
            macro.setMiningSpeed(sb[0]);
            return new StartingState();
        }

        switch (autoInventory.getSbError()) {
            case NONE:
                throw new IllegalStateException("AutoInventory failed but no error is detected! Please contact the developer");
            case CANNOT_OPEN_INV:
                macro.disable("Cannot open player's inventory to get statistics!");
                break;
            case CANNOT_GET_VALUE:
                macro.disable("Cannot get the value of statistics! Please contact the developer");
                break;
        }
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        autoInventory.stop();
        log("Exiting getting stats state");
    }
}
