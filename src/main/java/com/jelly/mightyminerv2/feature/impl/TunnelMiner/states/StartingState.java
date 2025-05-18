package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;

public class StartingState implements TunnelMinerState {
    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering Starting State");
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        return MightyMinerConfig.usePickaxeAbility && miner.getPickaxeAbilityState() == BlockMiner.PickaxeAbilityState.AVAILABLE?
                new ApplyAbilityState() : new DisablePerksState();
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting Starting State");
    }
}
