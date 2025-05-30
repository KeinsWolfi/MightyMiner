package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.feature.impl.BazaarHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;

public class SellingInventoryState implements AutoShaftState {
    @Override
    public void onStart(ShaftMacro macro) {
        log("Selling Inventory state started");
        BazaarHandler.getInstance().sell(0, 1);
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if(BazaarHandler.getInstance().isRunning()) return this;
        if(BazaarHandler.getInstance().hasSucceeded()) {
            log("Inventory sold successfully.");
            return new StartingState();
        }
        if(BazaarHandler.getInstance().hasFailed()) {
            logError("Failed to sell inventory.");
            return new StartingState();
        }
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Selling Inventory state ended");
        BazaarHandler.getInstance().stop();
    }
}
