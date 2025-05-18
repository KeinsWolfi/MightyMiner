package com.jelly.mightyminerv2.macro.impl.ScathaMacro.states;

import com.jelly.mightyminerv2.macro.impl.ScathaMacro.ScathaMacro;
import com.jelly.mightyminerv2.util.Logger;

public interface ScathaMacroState {
    void onStart(ScathaMacro macro);

    ScathaMacroState onTick(ScathaMacro macro);

    void onEnd(ScathaMacro macro);

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }

    default void logError(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    }

    default void send(String message) {
        Logger.addMessage("[" + this.getClass().getSimpleName() + "] " + message);
    }
}
