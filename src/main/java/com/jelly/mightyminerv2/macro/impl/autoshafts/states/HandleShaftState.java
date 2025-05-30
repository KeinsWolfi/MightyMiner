package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;
import java.util.Objects;

public class HandleShaftState implements AutoShaftState {
    private final Clock timer = new Clock();

    @Getter
    @Setter
    private HandleShaftStateState handleShaftState = HandleShaftStateState.DETECTING_SHAFT;
    public enum HandleShaftStateState {
        DETECTING_SHAFT,
        PATHING_TO_VANGUARD,
        PATHING_TO_VANGUARD2,
        PATHING_TO_VANGUARD3,
        ROTATING_TO_VANGUARD,
        CONFIRM_ROTATION,
        OPENING_VANGUARD,
        REACTING_TO_VANGUARD,
        RETURNING_TO_BASE
    }

    private final RouteNavigator routeNavigator = RouteNavigator.getInstance();

    @Getter
    @Setter
    private EntityLivingBase vanguard = null;

    @Getter
    @Setter
    private boolean pathing = false;

    @Override
    public void onStart(ShaftMacro macro) {
        log("Handling shaft state");
        timer.schedule(15_000);
        handleShaftState = HandleShaftStateState.DETECTING_SHAFT;
        pathing = false;
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        switch (handleShaftState) {
            case DETECTING_SHAFT:
                if (timer.isScheduled() && timer.passed() && GameStateHandler.getInstance().getCurrentMineshaftType() == null) {
                    log("No mineshaft detected, disabling macro");
                    macro.disable("No mineshaft detected. Please ensure you are in a mineshaft.");
                }

                if (GameStateHandler.getInstance().getCurrentMineshaftType() != null) {
                    Logger.sendLog("Detected mineshaft type: " + GameStateHandler.getInstance().getCurrentMineshaftType());
                    swapState(HandleShaftStateState.PATHING_TO_VANGUARD, 3000);
                }
                break;
            case PATHING_TO_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                log("Pathing to Vanguard");
                if(GameStateHandler.getInstance().getCurrentMineshaftType() == GameStateHandler.MineshaftTypes.FAIR) {
                    KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward, true);
                    swapState(HandleShaftStateState.PATHING_TO_VANGUARD2, MightyMinerConfig.vanguardWalkForwardTime);
                } else {
                    Logger.sendMessage("Not a Vanguard mineshaft, returning to base");
                    swapState(HandleShaftStateState.RETURNING_TO_BASE, 4000);
                }
                break;
            case PATHING_TO_VANGUARD2:
                if (timer.isScheduled() && !timer.passed()) break;
                KeyBindUtil.releaseAllExcept();
                swapState(HandleShaftStateState.PATHING_TO_VANGUARD3, 1500);
                break;
            case PATHING_TO_VANGUARD3:
                if (timer.isScheduled() && !timer.passed()) break;
                RouteWaypoint vang = new RouteWaypoint(-141, 3, -169, TransportMethod.WALK);

                if (!routeNavigator.isRunning() && !pathing) {
                    List<RouteWaypoint> nodes = GraphHandler.instance.findPathFrom("Vanguard", PlayerUtil.getBlockStandingOn(), vang);

                    if (nodes.isEmpty()) {
                        logError("Starting block: " + PlayerUtil.getBlockStandingOn() + ", Ending block: " + vang);
                        Logger.sendError("Could not find a path to the target block! Please send the logs to the developer.");
                        return null;
                    }
                    routeNavigator.start(new Route(nodes));
                    pathing = true;
                }

                if (routeNavigator.succeeded() && pathing) {
                    routeNavigator.stop();
                    swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 200);
                }
                break;
            case ROTATING_TO_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                RotationHandler.getInstance().stop();

                vanguard = EntityUtil.getClosestVanguard();

                RotationHandler.getInstance().easeTo(new RotationConfiguration(
                        new Target(vanguard),
                        MightyMinerConfig.getRandomRotationTime(),
                        null
                ));
                swapState(HandleShaftStateState.CONFIRM_ROTATION, 1000);
                break;
            case CONFIRM_ROTATION:
                if (RotationHandler.getInstance().isEnabled()) {
                    log("Waiting for rotation to finish");
                    break;
                }
                vanguard = EntityUtil.getClosestVanguard();

                if(!Objects.equals(Minecraft.getMinecraft().objectMouseOver.entityHit, vanguard)) {
                    log("Failed to rotate to Vanguard, retrying...");
                    swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 0);
                }

                log("Rotation confirmed, opening Vanguard");
                swapState(HandleShaftStateState.OPENING_VANGUARD, 200);
                RotationHandler.getInstance().stop();
                break;
            case OPENING_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                log("Opening Vanguard");
                KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem, true);
                swapState(HandleShaftStateState.REACTING_TO_VANGUARD, 200);
                break;
            case REACTING_TO_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                log("Reacting to Vanguard");
                // Logic to handle Vanguard reaction, e.g., mining or combat
                // This is a placeholder for actual logic
                KeyBindUtil.releaseAllExcept();
                swapState(HandleShaftStateState.RETURNING_TO_BASE, 5000);
                break;
            case RETURNING_TO_BASE:
                if (timer.isScheduled() && !timer.passed()) break;
                log("Returning to base");
                macro.setWasInShaft(true);
                // Logic to return to base
                return new WarpingState();
        }
        return this;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting handle shaft state");
        pathing = false;
        routeNavigator.stop();
        RotationHandler.getInstance().stop();
    }

    private void swapState(HandleShaftStateState newState, int delay) {
        this.handleShaftState = newState;
        timer.schedule(delay);
        log("Switched to state: " + newState);
    }
}
