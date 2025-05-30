package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.Objects;

public class EnterShaftState implements AutoShaftState {
    private final Clock timer = new Clock();
    private final Clock pathTimer = new Clock();

    private int retryCount = 0;

    private int pathRetryCount = 0;

    @Getter
    @Setter
    private EnteringShaftState enteringShaftState = EnteringShaftState.FINDING_SHAFT;
    public enum EnteringShaftState {
        FINDING_SHAFT,
        PATHING_TO_SHAFT,
        WATING_FOR_PATH,
        ROTATING_TO_SHAFT,
        CONFIRM_ROTATION,
        ENTERING_SHAFT,
        CLICKING_SHAFT_INV
    }

    private EntityLivingBase closestMineshaft = null;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void onStart(ShaftMacro macro) {
        enteringShaftState = EnteringShaftState.FINDING_SHAFT;
        pathRetryCount = 0;
        retryCount = 0;
        log("Entering shaft state");
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        switch (enteringShaftState) {
            case FINDING_SHAFT:
                closestMineshaft = EntityUtil.getClosestMineshaft();
                if (closestMineshaft != null) {
                    log("Found a mineshaft: " + closestMineshaft.getName());
                    swapState(EnteringShaftState.PATHING_TO_SHAFT,0);
                } else {
                    log("No mineshaft found, retrying...");
                }
                break;
            case PATHING_TO_SHAFT:
                if (timer.isScheduled() && !timer.passed())
                    break;

                BlockPos blockUnderShaft = EntityUtil.getBlockBelow(closestMineshaft);
                List<BlockPos> target1 = BlockUtil.getWalkableBlocksAround(blockUnderShaft);
                BlockPos target = null;
                if (target1.isEmpty()) {
                    logError("No walkable blocks found around the mineshaft, using block below");
                    target = blockUnderShaft;
                } else {
                    target = target1.remove(0);
                    log("Using walkable block: " + target);
                }
                Pathfinder.getInstance().stopAndRequeue(target);
                log("Pathing to mineshaft at: " + target);

                if (!Pathfinder.getInstance().isRunning()) {
                    log("Pathfinder wasnt enabled. starting");
                    Pathfinder.getInstance().setInterpolationState(true);
                    Pathfinder.getInstance().start();
                    swapState(EnteringShaftState.WATING_FOR_PATH,0);
                    break;
                }
                break;
            case WATING_FOR_PATH:
                if ((PlayerUtil.getNextTickPosition().squareDistanceTo(this.closestMineshaft.getPositionVector()) < 6 || Pathfinder.getInstance().succeeded())) { // 8 cuz why not
                    Pathfinder.getInstance().stop();
                    KeyBindUtil.releaseAllExcept();
                    this.swapState(EnteringShaftState.ROTATING_TO_SHAFT, 0);
                    break;
                }

                if (Pathfinder.getInstance().failed()) {
                    logError("Pathfinder failed to find a path to the mineshaft, retrying...");
                    pathRetryCount++;
                    if (pathRetryCount > 3) {
                        return new StartingState();
                    }
                }
                break;
            case ROTATING_TO_SHAFT:
                if (!Pathfinder.getInstance().isRunning()) {
                    RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(closestMineshaft), MightyMinerConfig.getRandomRotationTime(), null));
                    swapState(EnteringShaftState.CONFIRM_ROTATION, 0);
                    InventoryUtil.holdItem(MightyMinerConfig.altMiningTool);
                    log("Rotating to mineshaft: " + closestMineshaft.getName());
                }
                break;
            case CONFIRM_ROTATION:
                if(RotationHandler.getInstance().isEnabled()) {
                    log("Waiting for rotation to finish");
                    break;
                }

                if(!Objects.equals(mc.objectMouseOver.entityHit, closestMineshaft)) {
                    if (++retryCount < 3) {
                        logError("Failed to rotate to mineshaft, retrying...");
                        swapState(EnteringShaftState.ROTATING_TO_SHAFT, 0);
                        retryCount++;
                        break;
                    }
                }

                log("Rotation confirmed, entering shaft");
                swapState(EnteringShaftState.ENTERING_SHAFT, 500);
                break;
            case ENTERING_SHAFT:
                if (timer.isScheduled() && timer.passed()) {
                    log("Entering shaft at: " + closestMineshaft.getPositionVector());
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindUseItem, true);
                }

                if (InventoryUtil.getInventoryName().toLowerCase().contains("glacite mineshaft")) {
                    KeyBindUtil.releaseAllExcept();
                    log("Entered mineshaft inventory");
                    swapState(EnteringShaftState.CLICKING_SHAFT_INV, 800);
                    break;
                }
                break;
            case CLICKING_SHAFT_INV:
                if (timer.isScheduled() && timer.passed()) {
                    if (System.currentTimeMillis() - macro.getLastShaftEntranceTime() < 31_000) {
                        break;
                    }

                    int slot = InventoryUtil.getSlotIdOfItemInContainer("Enter Mineshaft");
                    if (slot == -1) {
                        logError("No 'Enter' button found in mineshaft inventory!");
                        InventoryUtil.closeScreen();
                        swapState(EnteringShaftState.FINDING_SHAFT, 0);
                        break;
                    }

                    InventoryUtil.clickContainerSlot(slot, InventoryUtil.ClickType.LEFT, InventoryUtil.ClickMode.PICKUP);
                    log("Clicked 'Enter' button in mineshaft inventory");
                    swapState(EnteringShaftState.FINDING_SHAFT, 0);
                    macro.setLastShaftEntranceTime(System.currentTimeMillis());
                    return new HandleShaftState();
                }
        }
        /*
        switch (enteringShaftState) {
            case FINDING_SHAFT:
                Logger.sendLog("Getting closest mineshaft...");
                closestMineshaft = EntityUtil.getClosestMineshaft();
                if (closestMineshaft != null) {
                    Logger.sendLog("Found a mineshaft: " + closestMineshaft.getName());
                    swapState(EnteringShaftState.PATHING_TO_SHAFT, 200);
                } else {
                    Logger.sendLog("No mineshaft found, retrying...");
                }
                break;
            case PATHING_TO_SHAFT:
                if (timer.isScheduled() && !timer.passed())
                    break;

                BlockPos blockUnderShaft = EntityUtil.getBlockBelow(closestMineshaft);
                Logger.sendLog("Block under shaft: " + blockUnderShaft);
                break;
        }
         */
        return this;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        closestMineshaft = null;
        Pathfinder.getInstance().stop();
        KeyBindUtil.releaseAllExcept();
        log("Exiting shaft state");
    }

    public void swapState(final EnteringShaftState toState, final int delay) {
        enteringShaftState = toState;
        timer.schedule(delay);
        log("Swapped state to: " + toState.name());
    }
}
