package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;

public class ShouldKillScathaState implements TunnelMinerState {
    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering ShouldKillScatha State");
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        return this;
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting ShouldKillScatha State");
    }
}
