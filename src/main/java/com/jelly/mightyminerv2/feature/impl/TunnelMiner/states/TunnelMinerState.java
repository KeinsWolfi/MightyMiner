package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import cc.polyfrost.oneconfig.events.event.ChatReceiveEvent;
import com.jelly.mightyminerv2.event.WormSpawnEvent;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

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

    default void onChatMessage(ClientChatReceivedEvent event) {
    }

    default void onWormSpawn(WormSpawnEvent event) {
    }
}
