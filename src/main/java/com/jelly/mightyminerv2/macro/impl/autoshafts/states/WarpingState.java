package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.feature.impl.AutoWarp;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;

public class WarpingState implements AutoShaftState {

    AutoWarp autoWarp = AutoWarp.getInstance();

    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering warping state");
        autoWarp.start(null, SubLocation.DWARVEN_BASE_CAMP);
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if (AutoWarp.getInstance().isRunning()) {
            return this;
        }

        if (AutoWarp.getInstance().hasSucceeded()) {
            log("Auto Warp Completed");
            macro.setWasInShaft(false);
            return new StartingState();
        }

        switch (AutoWarp.getInstance().getFailReason()) {
            case NONE:
                macro.disable("Auto Warp failed, but no error is detected. Please contact the developer.");
                break;
            case FAILED_TO_WARP:
                log("Retrying Auto Warp");
                autoWarp.start(null, SubLocation.DWARVEN_BASE_CAMP);
                break;
            case NO_SCROLL:
                macro.disable("You don't have the /warp base scroll!");
                break;
        }
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        autoWarp.stop();
        log("Exiting warping state");
    }
}
