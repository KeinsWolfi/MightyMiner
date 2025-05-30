package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BazaarHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;

public class BuyingSkeletonKeyState implements AutoShaftState {
    @Override
    public void onStart(ShaftMacro macro) {
        log("Buying Skeleton Key state started");
        BazaarHandler.getInstance().buy("Skeleton Key", MightyMinerConfig.skeletonKeyBuyAmount);
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if(BazaarHandler.getInstance().isRunning()) return this;
        if(BazaarHandler.getInstance().hasSucceeded()) {
            log("Skeleton Key purchased successfully");
            return new StartingState();
        }
        if(BazaarHandler.getInstance().hasFailed()) {
            logError("Failed to purchase Skeleton Key.");
            return new StartingState();
        }
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Buying Skeleton Key state ended");
        BazaarHandler.getInstance().stop();
    }
}

