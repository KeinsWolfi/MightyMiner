package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.event.WormSpawnEvent;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.macro.impl.ScathaMacro.states.MiningState;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.SystemNotificationUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.List;

public class BreakingState implements TunnelMinerState {

    private static final int FAILSAFE_TICKS = 40;         // Safety mechanism if we've been trying to break for too long
    private static final int CHEST_TICkS = 7;

    private final Minecraft mc = Minecraft.getMinecraft();

    private int breakAttemptTime;  // Tracks how long we've been trying to break the block (in ticks)
    private int chestAttemptTime;  // Tracks how long we've been trying to open the chest (in ticks)

    private MINING_STATE miningState;
    private enum MINING_STATE {
        BREAKING,
        OPENING_CHEST,
        SHOULD_KILL
    }

    private boolean shouldKill;

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering Breaking State");
        breakAttemptTime = 0;
        chestAttemptTime = 0;
        miningState = MINING_STATE.BREAKING;
        shouldKill = false;
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        BlockPos targetBlockPos = mc.thePlayer.rayTrace(4, 1).getBlockPos();
        IBlockState state = mc.theWorld.getBlockState(targetBlockPos);
        if ((state.getBlock() == Blocks.bedrock || state.getBlock() == Blocks.air) || (mc.thePlayer.posX == mc.thePlayer.lastTickPosX && mc.thePlayer.posY == mc.thePlayer.lastTickPosY && mc.thePlayer.posZ == mc.thePlayer.lastTickPosZ && miner.getTunnelMinerState() == TunnelMiner.TunnelMinerStateEnum.BACKWARD)) {
            breakAttemptTime++;
        } else {
            breakAttemptTime = 0;
        }

        if (state.getBlock() == Blocks.chest) {
            if (chestAttemptTime == 0) Logger.sendLog("Chest detected, attempting to open...");
            chestAttemptTime++;
            if (chestAttemptTime >= CHEST_TICkS) {
                miningState = MINING_STATE.OPENING_CHEST;
            }
        } else {
            chestAttemptTime = 0;
            if (miningState == MINING_STATE.OPENING_CHEST) {
                Logger.sendLog("No chest detected, resuming mining...");
                miningState = MINING_STATE.BREAKING;
            }
        }

        if (miningState == MINING_STATE.BREAKING) {
            if (miner.getTunnelMinerState() == TunnelMiner.TunnelMinerStateEnum.FORWARD) {
                if (atBorder() && facingBorder()) {
                    miner.setTunnelMinerState(TunnelMiner.TunnelMinerStateEnum.BACKWARD);
                    Logger.sendMessage("At border, switching to BACKWARD state");
                    KeyBindUtil.releaseAllExcept();
                    return new DisablePerksState();
                }
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindUseItem, false);
            } else if (miner.getTunnelMinerState() == TunnelMiner.TunnelMinerStateEnum.BACKWARD) {
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindBack, true);
                if (atBorder() && !facingBorder()) {
                    Logger.sendMessage("At border, switching lanes");
                    KeyBindUtil.releaseAllExcept();
                    return new EndOfPathState();
                }
            }
        } else if (miningState == MINING_STATE.OPENING_CHEST) {
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, false);
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindUseItem, true);
        } else if (miningState == MINING_STATE.SHOULD_KILL) {
            KeyBindUtil.releaseAllExcept();
            Logger.sendLog("Worm spawned");
            return null;
        }

        if (breakAttemptTime >= FAILSAFE_TICKS) {
            if(mc.theWorld.getBlockState(targetBlockPos).getBlock() == Blocks.bedrock) {
                Logger.sendError("Failed to break block after " + FAILSAFE_TICKS + " ticks. Stopping.");
                KeyBindUtil.releaseAllExcept();
                return null;
            } else {
                Logger.sendError("Failed to break block after " + FAILSAFE_TICKS + " ticks. Looking at: " + state.getBlock().toString());
                List<BlockPos> blocksBlocking = BlockUtil.getBlocksBlocking(true);
                miner.setBlocksBlocking(blocksBlocking);
                SystemNotificationUtil.systemNotification("Blocked", "Blocked by: " + blocksBlocking.size() + " blocks");
                return new ClearBehindState();
            }
        }

        if (miner.getPickaxeAbilityState() == BlockMiner.PickaxeAbilityState.AVAILABLE) {
            Logger.sendLog("Pickaxe ability available, transitioning to ApplyAbilityState");
            KeyBindUtil.releaseAllExcept();
            return new ApplyAbilityState();
        }

        if (shouldKill) {
            Logger.sendLog("Worm spawned, transitioning to MiningState");
            KeyBindUtil.releaseAllExcept();
            return new ShouldKillScathaState();
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

    // §7§oYou hear the sound of something approaching...

    @Override
    public void onWormSpawn(WormSpawnEvent event) {
        SystemNotificationUtil.systemNotification("Worm spawned", "A worm has spawned nearby!");
        shouldKill = true;
    }
}
