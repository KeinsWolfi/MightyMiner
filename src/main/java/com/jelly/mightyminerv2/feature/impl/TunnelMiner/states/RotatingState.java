package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import net.minecraft.client.Minecraft;

public class RotatingState implements TunnelMinerState {

    private final Minecraft mc = Minecraft.getMinecraft();

    private float targetYaw;
    private float targetPitch;

    private boolean rotating = false;

    @Override
    public void onStart(TunnelMiner miner) {
        log("Entering Rotating State");
        initializeRotation(miner);
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        if (rotating && !RotationHandler.getInstance().isEnabled()) {
            Logger.sendMessage("Rotation complete, transitioning to Breaking State. New yaw: " + mc.thePlayer.rotationYaw + ", pitch: " + mc.thePlayer.rotationPitch);
            miner.setDirection(getDirection());
            log("New direction: " + miner.getDirection());
            return new BreakingState();
        }

        return this;
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        log("Exiting Rotating State");
        RotationHandler.getInstance().stop();
        rotating = false;
    }

    private void initializeRotation(TunnelMiner miner) {
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;

        targetYaw = getClosestYaw(yaw, 90);
        targetPitch = miner.getTunnelMinerState() == TunnelMiner.TunnelMinerStateEnum.FORWARD ? 56 : -45;

        RotationHandler.getInstance().stop();
        RotationHandler.getInstance().queueRotation(
            new RotationConfiguration(
                new Target(new Angle(targetYaw, targetPitch)),
                MightyMinerConfig.getRandomRotationTime(),
                null
            )
        );
        RotationHandler.getInstance().start();

        rotating = true;
    }

    private float getClosestYaw(float currentYaw, float targetStep) {
        // Normalisiere currentYaw auf [0, 360)
        float normalizedYaw = ((currentYaw % 360) + 360) % 360;
        // Runde auf das nächste Vielfache von targetStep
        float closest = Math.round(normalizedYaw / targetStep) * targetStep;
        // Optional: Normalisiere zurück auf [-180, 180)
        if (closest > 180) closest -= 360;
        return closest;
    }

    private TunnelMiner.DIRECTION getDirection() {
        final Minecraft mc1 = Minecraft.getMinecraft();
        double x = mc1.thePlayer.posX;
        double z = mc1.thePlayer.posZ;
        float yaw = mc1.thePlayer.rotationYaw;

        // Yaw normalisieren auf [0, 360)
        yaw = (yaw % 360 + 360) % 360;

        if (yaw >= 315 || yaw < 45) {
            return TunnelMiner.DIRECTION.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return TunnelMiner.DIRECTION.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return TunnelMiner.DIRECTION.NORTH;
        } else {
            return TunnelMiner.DIRECTION.EAST;
        }
    }
}
