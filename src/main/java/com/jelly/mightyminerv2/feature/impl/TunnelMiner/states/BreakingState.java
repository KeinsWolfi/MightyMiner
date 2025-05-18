package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import net.minecraft.client.Minecraft;

public class BreakingState implements TunnelMinerState {

    private static final int FAILSAFE_TICKS = 40;         // Safety mechanism if we've been trying to break for too long

    private final Minecraft mc = Minecraft.getMinecraft();

    private int breakAttemptTime;  // Tracks how long we've been trying to break the block (in ticks)

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering Breaking State");
        breakAttemptTime = 0;
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {



        return this;
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting Breaking State");
    }

    public boolean atBorder() {
        int radius = 3; // Adjust this value as needed
        double x = mc.thePlayer.posX;
        double z = mc.thePlayer.posZ;

        double minX = 201, maxX = 824;
        double minZ = 201, maxZ = 824;

        boolean nearXEdge = (x >= minX && x <= minX + radius) || (x <= maxX && x >= maxX - radius);
        boolean nearZEdge = (z >= minZ && z <= minZ + radius) || (z <= maxZ && z >= maxZ - radius);

        return (nearXEdge || nearZEdge);
    }
}
