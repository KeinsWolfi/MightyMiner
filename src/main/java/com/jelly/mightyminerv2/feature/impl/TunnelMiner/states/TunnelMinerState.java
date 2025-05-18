package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;

public interface TunnelMinerState {
    void onStart(TunnelMiner miner);

    TunnelMinerState onTick(TunnelMiner miner);

    void onEnd(TunnelMiner miner);

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }

    default void logError(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    }
}
