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

public class EnterShaftState implements AutoShaftState {
    private final Clock timer = new Clock();

    @Getter
    @Setter
    private EnteringShaftState enteringShaftState = EnteringShaftState.FINDING_SHAFT;
    public enum EnteringShaftState {
        FINDING_SHAFT,
        PATHING_TO_SHAFT,
        ROTATING_TO_SHAFT,
        CONFIRM_ROTATION,
        ENTERING_SHAFT,
        CLICKING_SHAFT_INV
    }

    private EntityLivingBase closestMineshaft = null;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void onStart(ShaftMacro macro) {
        enteringShaftState = EnteringShaftState.ENTERING_SHAFT;
        log("Entering shaft state");
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        /*
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
                if (this.timer.isScheduled() && !this.timer.passed())
                    break;

                BlockPos blockUnderShaft = EntityUtil.getBlockBelow(closestMineshaft);
                BlockPos target = BlockUtil.getWalkableBlocksAround(blockUnderShaft).remove(0);
                Pathfinder.getInstance().stopAndRequeue(target);
                log("Pathing to mineshaft at: " + target);

                if (!Pathfinder.getInstance().isRunning()) {
                    log("Pathfinder wasnt enabled. starting");
                    Pathfinder.getInstance().setInterpolationState(true);
                    Pathfinder.getInstance().start();
                    break;
                }

                if (PlayerUtil.getNextTickPosition().squareDistanceTo(this.closestMineshaft.getPositionVector()) < 1 && mc.thePlayer.canEntityBeSeen(this.closestMineshaft)) { // 8 cuz why not
                    Pathfinder.getInstance().stop();
                    KeyBindUtil.releaseAllExcept();
                    this.swapState(EnteringShaftState.ROTATING_TO_SHAFT, 0);
                    break;
                }
                break;
            case ROTATING_TO_SHAFT:
                if (!Pathfinder.getInstance().isRunning()) {
                    RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(closestMineshaft), MightyMinerConfig.getRandomRotationTime(), null));
                    this.swapState(EnteringShaftState.CONFIRM_ROTATION, 0);
                    log("Rotating to mineshaft: " + closestMineshaft.getName());
                }
                break;
            case CONFIRM_ROTATION:
                if(RotationHandler.getInstance().isEnabled()) {
                    log("Waiting for rotation to finish");
                    break;
                }

                log("Rotation confirmed, entering shaft");
                this.swapState(EnteringShaftState.ENTERING_SHAFT, 200);
                break;
            case ENTERING_SHAFT:
                if (this.timer.isScheduled() && this.timer.passed()) {
                    log("Entering shaft at: " + closestMineshaft.getPositionVector());
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindUseItem, true);
                    break;
                }

                if (InventoryUtil.getInventoryName().toLowerCase().contains("glacite mineshaft")) {
                    KeyBindUtil.releaseAllExcept();
                    log("Entered mineshaft inventory");
                    this.swapState(EnteringShaftState.CLICKING_SHAFT_INV, 500);
                    break;
                }
                break;
            case CLICKING_SHAFT_INV:
                if (this.timer.isScheduled() && this.timer.passed()) {
                    int slot = InventoryUtil.getSlotIdOfItemInContainer("Enter Mineshaft");
                    if (slot == -1) {
                        logError("No 'Enter' button found in mineshaft inventory!");
                        InventoryUtil.closeScreen();
                        this.swapState(EnteringShaftState.FINDING_SHAFT, 0);
                        break;
                    }

                    InventoryUtil.clickContainerSlot(slot, InventoryUtil.ClickType.LEFT, InventoryUtil.ClickMode.PICKUP);
                    log("Clicked 'Enter' button in mineshaft inventory");
                    this.swapState(EnteringShaftState.FINDING_SHAFT, 0);
                    KeyBindUtil.releaseAllExcept();
                    return new StartingState();
                }
        }
        */
        switch (enteringShaftState) {
            case FINDING_SHAFT:
                closestMineshaft = EntityUtil.getClosestMineshaft();
                if (closestMineshaft != null) {
                    Logger.sendLog("Found a mineshaft: " + closestMineshaft.getName());
                    swapState(EnteringShaftState.PATHING_TO_SHAFT, 200);
                } else {
                    Logger.sendLog("No mineshaft found, retrying...");
                }
                break;
            case PATHING_TO_SHAFT:
                if (this.timer.isScheduled() && !this.timer.passed())
                    break;

                BlockPos blockUnderShaft = EntityUtil.getBlockBelow(closestMineshaft);
                Logger.sendLog("Block under shaft: " + blockUnderShaft);
                break;
        }
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
        this.enteringShaftState = toState;
        this.timer.schedule(delay);
        log("Swapped state to: " + toState.name());
    }
}
