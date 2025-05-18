package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;

public class ApplyAbilityState implements TunnelMinerState {

    private final Clock timer = new Clock();
    private final Clock timer2 = new Clock();

    private final long COOLDOWN = 1000; // 1-second cooldown for activating ability

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering Apply Ability State");

        // Start the cooldown timer
        timer2.reset();
        timer.reset();
        timer.schedule(COOLDOWN);

        // Release all keys to prepare for the right click
        if(Minecraft.getMinecraft().currentScreen == null) {
            KeyBindUtil.releaseAllExcept();
        }
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        // If the first timer has ended, press right click
        if (timer.isScheduled() && timer.passed()) {
            timer.reset();
            timer2.reset();
            timer2.schedule(COOLDOWN);
            KeyBindUtil.rightClick();
        }

        // If the second timer has ended, transition back to the starting state
        if (timer2.isScheduled() && timer2.passed()) {
            return new StartingState();
        }

        // Wait for the timer to expire
        return this;
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting Apply Ability State");
    }
}
