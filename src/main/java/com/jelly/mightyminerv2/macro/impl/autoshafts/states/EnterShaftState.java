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
    private final Clock timeoutTimer = new Clock();

    private int retryCount = 0;

    private int pathRetryCount = 0;

    private boolean rotating = false;

    @Getter
    @Setter
    private EnteringShaftState enteringShaftState = EnteringShaftState.FINDING_SHAFT;
    public enum EnteringShaftState {
        FINDING_SHAFT,
        PATHING_TO_SHAFT,
        WAITING_FOR_PATH,
        WALK_FORWARD,
        TIMEOUT,
        ROTATING_TO_SHAFT,
        CONFIRM_ROTATION,
        ENTERING_SHAFT,
        CLICKING_SHAFT_INV
    }

    private EntityLivingBase closestMineshaft = null;

    private EntityLivingBase fullClosestMineshaft = null;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void onStart(ShaftMacro macro) {
        swapState(EnteringShaftState.FINDING_SHAFT, 3000);
        pathRetryCount = 0;
        retryCount = 0;
        rotating = false;
        log("Entering shaft state");
        timer.reset();
        timeoutTimer.reset();
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        switch (enteringShaftState) {
            case FINDING_SHAFT:
                if (timer.isScheduled() && !timer.passed()) break;
                closestMineshaft = EntityUtil.getClosestMineshaft();
                fullClosestMineshaft = EntityUtil.getSecondMineshaftEntity(closestMineshaft);
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
                BlockPos target;
                if (target1.isEmpty()) {
                    logError("No walkable blocks found around the mineshaft, using block below");
                    target = blockUnderShaft;
                } else {
                    target = target1.remove(0);
                    log("Using walkable block: " + target);
                }
                Pathfinder.getInstance().stopAndRequeue(target);
                log("Pathing to mineshaft at: " + target);

                InventoryUtil.holdItem(MightyMinerConfig.altMiningTool);

                if (!Pathfinder.getInstance().isRunning()) {
                    log("Pathfinder wasnt enabled. starting");
                    Pathfinder.getInstance().setInterpolationState(true);
                    Pathfinder.getInstance().start();
                    swapState(EnteringShaftState.WAITING_FOR_PATH,0);
                    break;
                }
                break;
            case WAITING_FOR_PATH:
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
                        this.swapState(EnteringShaftState.WALK_FORWARD, 0);
                    }
                }
                break;
            case WALK_FORWARD:
                if (timer.isScheduled() && timer.passed() && !rotating) {
                    RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(closestMineshaft), MightyMinerConfig.getRandomRotationTime(), null).followTarget(true));
                    InventoryUtil.holdItem(MightyMinerConfig.altMiningTool);
                    rotating = true;
                }

                if (RotationHandler.getInstance().isEnabled() && rotating && RotationHandler.getInstance().isFollowingTarget()) {
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump, true);
                    swapState(EnteringShaftState.TIMEOUT, 5000);
                    rotating = false;
                }
                break;
            case TIMEOUT:
                if (timer.isScheduled() && timer.passed()) {
                    log("Timeout reached while walking forward");
                    return new StartingState();
                }

                if (PlayerUtil.getNextTickPosition().squareDistanceTo(this.closestMineshaft.getPositionVector()) < 6) {
                    KeyBindUtil.releaseAllExcept();
                    swapState(EnteringShaftState.ROTATING_TO_SHAFT, 0);
                    log("Reached mineshaft while walking forward, rotating now");
                }
                break;
            case ROTATING_TO_SHAFT:
                if (!Pathfinder.getInstance().isRunning()) {
                    if (mc.thePlayer.getDistanceSqToEntity(closestMineshaft) < 1 && (Objects.equals(mc.objectMouseOver.entityHit, closestMineshaft) || Objects.equals(mc.objectMouseOver.entityHit, fullClosestMineshaft))) {
                        log("Already close to the mineshaft, skipping rotation");
                        swapState(EnteringShaftState.ENTERING_SHAFT, 500);
                        break;
                    }

                    RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(closestMineshaft), MightyMinerConfig.getRandomAotvLookDelay(), null));
                    swapState(EnteringShaftState.CONFIRM_ROTATION, 0);
                    log("Rotating to mineshaft: " + closestMineshaft.getName());
                }
                break;
            case CONFIRM_ROTATION:
                if(RotationHandler.getInstance().isEnabled()) {
                    log("Waiting for rotation to finish");
                    break;
                }

                if(!Objects.equals(mc.objectMouseOver.entityHit, closestMineshaft) && !Objects.equals(mc.objectMouseOver.entityHit, fullClosestMineshaft)) {
                    if (retryCount < 3) {
                        logError("Failed to rotate to mineshaft, retrying...");
                        swapState(EnteringShaftState.ROTATING_TO_SHAFT, 0);
                        retryCount++;
                        break;
                    } else if (retryCount >= 3 && retryCount < 7) {
                        swapState(EnteringShaftState.WALK_FORWARD, 300);
                        KeyBindUtil.releaseAllExcept();
                        break;
                    } else {
                        logError("Failed to rotate to mineshaft after 7 attempts, giving up");
                        return new StartingState();
                    }
                }

                log("Rotation confirmed, entering shaft");
                swapState(EnteringShaftState.ENTERING_SHAFT, 500);
                break;
            case ENTERING_SHAFT:
                if (timer.isScheduled() && timer.passed()) {
                    log("Entering shaft at: " + closestMineshaft.getPositionVector());
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindUseItem, true);
                    if (!timeoutTimer.isScheduled()) {
                        timeoutTimer.schedule(5_000);
                    }
                }

                if (timeoutTimer.isScheduled() && timeoutTimer.passed()) {
                    logError("Timeout while entering shaft, retrying...");
                    KeyBindUtil.releaseAllExcept();
                    return new StartingState();
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

                    if (!InventoryUtil.getInventoryName().toLowerCase().contains("glacite mineshaft")) {
                        logError("Not in mineshaft inventory, retrying...");
                        InventoryUtil.closeScreen();
                        swapState(EnteringShaftState.FINDING_SHAFT, 0);
                        break;
                    }

                    if (!InventoryUtil.isInventoryLoaded()) {
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
