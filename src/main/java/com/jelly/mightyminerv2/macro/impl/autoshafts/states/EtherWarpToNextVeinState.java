package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class EtherWarpToNextVeinState implements AutoShaftState {
    @Getter
    private static final List<RouteWaypoint> VEINS = new ArrayList<>();

    RouteWaypoint vein1 = new RouteWaypoint(57, 141, 268, TransportMethod.ETHERWARP);
    RouteWaypoint vein2 = new RouteWaypoint(36, 149, 296, TransportMethod.ETHERWARP);

    @Getter
    @Setter
    private EWState state = EWState.STARTING;
    public enum EWState {
        STARTING,
        ROTATING,
        VERIFYING_ROTATION,
        WARPING,
        WAITING_FOR_WARP,
        SWITCHING_TO_MINING
    }

    private final Clock timer = new Clock();

    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering Ether Warp to next vein state");
        VEINS.add(vein1);
        VEINS.add(vein2);

        if (macro.getNextVeinIndex() >= VEINS.size()) {
            log("Next vein index out of bounds, resetting to 0");
            macro.setNextVeinIndex(0);
        }

        state = EWState.STARTING;
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        switch (state) {
            case STARTING:
                log("Starting Ether Warp to next vein");
                InventoryUtil.holdItem("Aspect of the Void");
                KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak, true);
                swapState(EWState.ROTATING, 200);
                break;
            case ROTATING:
                if (timer.isScheduled() && timer.passed()) {
                    log("Rotating to face the next vein");
                    // Implement rotation logic here
                    int rotTime = MightyMinerConfig.getRandomAotvLookDelay();
                    RotationHandler.getInstance().easeTo(new RotationConfiguration(
                            new Target(VEINS.get(macro.getNextVeinIndex()).toVec3().addVector(0.5, 0.5, 0.5)),
                            rotTime,
                            null
                    ));
                    swapState(EWState.VERIFYING_ROTATION, rotTime);
                }
                break;
            case VERIFYING_ROTATION:
                if (timer.isScheduled() && timer.passed()) {
                    log("Verifying rotation to the next vein");
                    // Implement verification logic here
                    if (RotationHandler.getInstance().isEnabled()) {
                        log("Rotation underway, waiting for it to finish");
                        break;
                    }

                    swapState(EWState.WARPING, 250);
                }
                break;
            case WARPING:
                if(timer.isScheduled() && !timer.passed()) break;

                log("Warping to the next vein: " + VEINS.get(macro.getNextVeinIndex()));
                KeyBindUtil.rightClick();
                swapState(EWState.SWITCHING_TO_MINING, 500);
                break;
            case WAITING_FOR_WARP:
                if (timer.isScheduled() && timer.passed()) {
                    log("Warp completed, switching to mining state");
                    return new WarpingState();
                }
                break;
            case SWITCHING_TO_MINING:
                if (timer.isScheduled() && !timer.passed()) break;
                return new MiningState(); // Switch to mining state
        }
        return this;
    }

    private void swapState(EWState newState, int delay) {
        state = newState;
        timer.schedule(delay);
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting Ether Warp to next vein state");
        timer.reset();
        state = EWState.STARTING; // Reset state for next use
        macro.setNextVeinIndex(macro.getNextVeinIndex() + 1);
    }
}
