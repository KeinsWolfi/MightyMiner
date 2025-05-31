package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.Logger;

public interface AutoShaftState {
    void onStart(ShaftMacro macro);

    AutoShaftState onTick(ShaftMacro macro);

    default void onMotionUpdate(ShaftMacro macro) {
        // Default implementation does nothing
    }

    default void onChat(ShaftMacro macro, String message) {
    }

    void onEnd(ShaftMacro macro);

    default void logMessage(String message) {
        Logger.sendLog(message);
    }

    default void logError(String message) {
        Logger.sendError(message);
    }

    default void send(String message) {
        Logger.sendMessage(message);
    }

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }
}
