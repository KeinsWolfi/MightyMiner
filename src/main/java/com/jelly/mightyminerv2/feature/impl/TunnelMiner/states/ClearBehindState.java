package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.AngleUtil;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClearBehindState implements TunnelMinerState {
    private float startYaw;
    private float startPitch;
    private boolean rotating = false;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    @Setter
    private ClearBehindStateEnum clearBehindState = ClearBehindStateEnum.NONE;
    public enum ClearBehindStateEnum {
        CLEARING,
        ROTATING_BACK,
        FINISHED,
        NONE
    }

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering ClearBehind State");
        startYaw = mc.thePlayer.rotationYaw;
        startPitch = mc.thePlayer.rotationPitch;
        KeyBindUtil.releaseAllExcept();
        clearBehindState = ClearBehindStateEnum.CLEARING;
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        switch (clearBehindState) {
            case CLEARING:
                List<BlockPos> blocksBlocking = miner.getBlocksBlocking().stream()
                        .filter(
                                blockPos -> mc.theWorld.getBlockState(blockPos).getBlock() != Blocks.air
                        )
                        .sorted(Comparator.comparingDouble(block -> mc.thePlayer.getPositionEyes(1).distanceTo(new Vec3(block.getX(), block.getY(), block.getZ()))))
                        .collect(Collectors.toList());

                if (blocksBlocking.isEmpty()) {
                    clearBehindState = ClearBehindStateEnum.ROTATING_BACK;
                    miner.setTargetBlockPos(null);
                } else {
                    miner.setTargetBlockPos(blocksBlocking.get(0));
                }

                List<Vec3> points = BlockUtil.bestPointsOnBestSide(miner.getTargetBlockPos());

                // Handle case where no valid points are found
                if (points.isEmpty()) {
                    logError("Cannot find points to look at. Returning to STARTING state.");
                    break;
                }

                Vec3 point = points.get(0);

                rotating = true;

                Angle angle = AngleUtil.getRotation(point);
                if (!RotationHandler.getInstance().isEnabled()) {
                    rotateTo(angle.yaw, angle.pitch);
                }

                if (rotating && !RotationHandler.getInstance().isEnabled()) {
                    log("Rotation complete, breaking block.");
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
                }

                if (mc.theWorld.getBlockState(miner.getTargetBlockPos()).getBlock() == Blocks.air) {
                    log("Block is air, transitioning to next block.");
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, false);
                }
                break;
            case ROTATING_BACK:
                RotationHandler.getInstance().stop();
                RotationHandler.getInstance().queueRotation(
                        new RotationConfiguration(
                                new Angle(startYaw, startPitch),
                                MightyMinerConfig.getRandomRotationTime(),
                                null
                        )
                );
                RotationHandler.getInstance().start();
                clearBehindState = ClearBehindStateEnum.FINISHED;
                rotating = true;
                break;
            case FINISHED:
                if (rotating && !RotationHandler.getInstance().isEnabled()) {
                    clearBehindState = ClearBehindStateEnum.NONE;
                    rotating = false;
                    KeyBindUtil.releaseAllExcept();
                    return new RotatingState();
                }
                break;
        }
        return this;
    }

    private void rotateTo(float yaw, float pitch) {
        RotationHandler.getInstance().stop();
        RotationHandler.getInstance().queueRotation(
                new RotationConfiguration(
                        new Angle(yaw, pitch),
                        MightyMinerConfig.getRandomRotationTime(),
                        null
                )
        );
        RotationHandler.getInstance().start();
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting ClearBehind State");
        // KeyBindUtil.releaseAllExcept();
    }
}
