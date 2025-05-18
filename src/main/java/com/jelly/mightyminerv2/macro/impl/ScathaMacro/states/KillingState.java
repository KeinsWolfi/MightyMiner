package com.jelly.mightyminerv2.macro.impl.ScathaMacro.states;

import com.jelly.mightyminerv2.macro.impl.ScathaMacro.ScathaMacro;

public class KillingState implements ScathaMacroState {
    @Override
    public void onStart(ScathaMacro macro) {
        log("Entering Killing State");
    }

    @Override
    public ScathaMacroState onTick(ScathaMacro macro) {
        return null;
    }

    @Override
    public void onEnd(ScathaMacro macro) {
        log("Exiting Killing State");
    }
}
