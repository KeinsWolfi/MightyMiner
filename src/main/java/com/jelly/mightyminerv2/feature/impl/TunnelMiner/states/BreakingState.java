package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class BreakingState implements TunnelMinerState {

    private static final int FAILSAFE_TICKS = 20;         // Safety mechanism if we've been trying to break for too long

    private final Minecraft mc = Minecraft.getMinecraft();

    private int breakAttemptTime;  // Tracks how long we've been trying to break the block (in ticks)

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering Breaking State");
        breakAttemptTime = 0;
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        BlockPos targetBlockPos = mc.thePlayer.rayTrace(6, 1).getBlockPos();
        if (mc.theWorld.getBlockState(targetBlockPos).getBlock() == Blocks.bedrock || mc.theWorld.getBlockState(targetBlockPos).getBlock() == Blocks.chest) {
            breakAttemptTime++;
        } else {
            breakAttemptTime = 0;
        }

        if (miner.getTunnelMinerState() == TunnelMiner.TunnelMinerStateEnum.FORWARD) {
            if (atBorder() && facingBorder()) {
                miner.setTunnelMinerState(TunnelMiner.TunnelMinerStateEnum.BACKWARD);
                Logger.sendMessage("At border, switching to BACKWARD state");
                KeyBindUtil.releaseAllExcept();
                return new DisablePerksState();
            }
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
        } else if (miner.getTunnelMinerState() == TunnelMiner.TunnelMinerStateEnum.BACKWARD) {
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindBack, true);
            if (atBorder() && !facingBorder()) {
                miner.setTunnelMinerState(TunnelMiner.TunnelMinerStateEnum.FORWARD);
                Logger.sendMessage("At border, switching to FORWARD state");
                KeyBindUtil.releaseAllExcept();
                return new DisablePerksState();
            }
        }

        if (breakAttemptTime >= FAILSAFE_TICKS) {
            Logger.sendError("Failed to break block after " + FAILSAFE_TICKS + " ticks. Stopping.");
            KeyBindUtil.releaseAllExcept();
            return null;
        }

        return this;
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting Breaking State");
    }

    public static boolean atBorder() {
        int radius = 3; // Adjust this value as needed
        final Minecraft mc1 = Minecraft.getMinecraft();
        double x = mc1.thePlayer.posX;
        double z = mc1.thePlayer.posZ;

        double minX = 201, maxX = 824;
        double minZ = 201, maxZ = 824;

        boolean nearXEdge = (x >= minX && x <= minX + radius) || (x <= maxX && x >= maxX - radius);
        boolean nearZEdge = (z >= minZ && z <= minZ + radius) || (z <= maxZ && z >= maxZ - radius);

        return (nearXEdge || nearZEdge);
    }

    public static boolean facingBorder() {
        final Minecraft mc1 = Minecraft.getMinecraft();
        double x = mc1.thePlayer.posX;
        double z = mc1.thePlayer.posZ;
        float yaw = mc1.thePlayer.rotationYaw;

        // Yaw normalisieren auf [0, 360)
        yaw = (yaw % 360 + 360) % 360;

        int radius = 3;

        double minX = 201, maxX = 824;
        double minZ = 201, maxZ = 824;

        // Süden (z nimmt zu)
        if (yaw >= 315 || yaw < 45) {
            return z >= maxZ - radius && z <= maxZ;
        }
        // Westen (x nimmt ab)
        else if (yaw >= 45 && yaw < 135) {
            return x >= minX && x <= minX + radius;
        }
        // Norden (z nimmt ab)
        else if (yaw >= 135 && yaw < 225) {
            return z >= minZ && z <= minZ + radius;
        }
        // Osten (x nimmt zu)
        else if (yaw >= 225 && yaw < 315) {
            return x >= maxX - radius && x <= maxX;
        }
        return false;
    }
}
