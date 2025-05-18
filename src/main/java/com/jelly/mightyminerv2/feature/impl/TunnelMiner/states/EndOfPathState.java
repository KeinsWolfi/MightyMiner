package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;

public class EndOfPathState implements TunnelMinerState {
    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering EndOfPath State");
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        return null;
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting EndOfPath State");
    }
}
