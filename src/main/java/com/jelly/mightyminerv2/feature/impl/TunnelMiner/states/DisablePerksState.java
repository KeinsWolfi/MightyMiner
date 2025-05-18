package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;

public class DisablePerksState implements TunnelMinerState {

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering DisablePerksState");
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        return new RotatingState();
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting DisablePerksState");
    }
}
