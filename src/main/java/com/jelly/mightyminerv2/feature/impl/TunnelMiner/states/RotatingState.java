package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import net.minecraft.client.Minecraft;

public class RotatingState implements TunnelMinerState {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering Rotating State");
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        return null;
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting Rotating State");
    }

    private void initializeRotation(TunnelMiner miner) {
        float yaw = mc.thePlayer.cameraYaw;
        float pitch = mc.thePlayer.cameraPitch;


    }
}
