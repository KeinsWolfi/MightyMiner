package com.jelly.mightyminerv2.macro.impl.ScathaMacro.states;

import com.google.common.collect.ImmutableSet;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller;
import com.jelly.mightyminerv2.macro.impl.ScathaMacro.ScathaMacro;

import java.util.Set;

public class KillingState implements ScathaMacroState {
    @Override
    public void onStart(ScathaMacro macro) {
        log("Entering Killing State");
        Set<String> mobName = ImmutableSet.of("Scatha", "Worm");

        AutoMobKiller.getInstance().start(
                mobName,
                MightyMinerConfig.slayerWeapon
        );
    }

    @Override
    public ScathaMacroState onTick(ScathaMacro macro) {
        if (AutoMobKiller.getInstance().isRunning() || AutoMobKiller.getInstance().succeeded()) {
            return this;
        }

        switch (AutoMobKiller.getInstance().getMkError()) {
            case NONE:
                macro.disable("Mob killer failed, but no error is detected. Please contact the developer.");
                break;
            case NO_ENTITIES:
                log("No entities found in Mob Killer. Restarting");
                return new MiningState();
        }

        return null;
    }

    @Override
    public void onEnd(ScathaMacro macro) {
        AutoMobKiller.getInstance().stop();
        log("Exiting Killing State");
    }
}
