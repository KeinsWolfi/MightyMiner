package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;

public class EnterShaftState implements AutoShaftState {
    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering shaft state");
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting shaft state");
    }
}
