package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;

import java.util.List;
import java.util.Objects;
import java.util.Random;

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
        RETURNING_TO_BASE,
        PATHING_TO_VANGUARD_AFTER_FAIL
    }

    private final RouteNavigator routeNavigator = RouteNavigator.getInstance();

    @Getter
    @Setter
    private EntityLivingBase vanguard = null;

    @Getter
    @Setter
    private boolean pathing = false;

    private boolean overLadder = false;
    private boolean overAir = false;

    private boolean vanguardFound = false;

    private int vangRetry = 0;

    private Random random = new Random();

    @Override
    public void onStart(ShaftMacro macro) {
        log("Handling shaft state");
        timer.schedule(10_000);
        handleShaftState = HandleShaftStateState.DETECTING_SHAFT;
        pathing = false;
        overLadder = false;
        vanguardFound = false;
        overAir = false;
        vangRetry = 0;
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if (InventoryUtil.getInventoryName() != null) {
            InventoryUtil.closeScreen();
        }
        switch (handleShaftState) {
            case DETECTING_SHAFT:
                if (timer.isScheduled() && timer.passed() && GameStateHandler.getInstance().getCurrentMineshaftType() == null) {
                    log("No mineshaft detected, disabling macro");
                    return new StartingState();
                }

                if (GameStateHandler.getInstance().getCurrentMineshaftType() != null) {
                    Logger.sendLog("Detected mineshaft type: " + GameStateHandler.getInstance().getCurrentMineshaftType());
                    macro.setWasInShaft(true);
                    if (GameStateHandler.getInstance().getCurrentMineshaftType() == GameStateHandler.MineshaftTypes.FAIR) {
                        swapState(HandleShaftStateState.PATHING_TO_VANGUARD, 3000 + random.nextInt(5000));
                    } else {
                        Logger.sendMessage("Not a Vanguard mineshaft, returning to base");
                        swapState(HandleShaftStateState.RETURNING_TO_BASE, 3000 + random.nextInt(1000));
                    }
                }
                break;
            case PATHING_TO_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                log("Pathing to Vanguard");
                KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward, true);
                swapState(HandleShaftStateState.PATHING_TO_VANGUARD2, 400);
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc !ptme");
                break;
            case PATHING_TO_VANGUARD2:
                if (overAir) {
                    KeyBindUtil.releaseAllExcept();
                    KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack, true);
                }

                if (timer.isScheduled() && !timer.passed() && !overAir) break;
                KeyBindUtil.releaseAllExcept();

                swapState(HandleShaftStateState.PATHING_TO_VANGUARD3, 1500);
                break;
            case PATHING_TO_VANGUARD3:
                if (timer.isScheduled() && !timer.passed()) break;
                RouteWaypoint vang = new RouteWaypoint(-141, 3, -169, TransportMethod.WALK);

                if (!routeNavigator.isRunning() && !pathing) {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/p warp");
                    List<RouteWaypoint> nodes = GraphHandler.instance.findPathFrom("Vanguard", PlayerUtil.getBlockStandingOn(), vang);

                    if (nodes.isEmpty()) {
                        logError("Starting block: " + PlayerUtil.getBlockStandingOn() + ", Ending block: " + vang);
                        Logger.sendError("Could not find a path to the target block! Please send the logs to the developer.");
                        return null;
                    }
                    routeNavigator.start(new Route(nodes));
                    pathing = true;
                }

                vanguard = EntityUtil.getClosestVanguard();

                if (routeNavigator.succeeded() && pathing && Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(vanguard) < 6) {
                    routeNavigator.stop();
                    Pathfinder.getInstance().stop();
                    swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 400);
                } else if (routeNavigator.succeeded() && pathing && Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(vanguard) >= 6) {
                    log("Reached Vanguard, but too far away, still rotating...");
                    routeNavigator.stop();
                    Pathfinder.getInstance().stop();
                    swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 400);
                }

                switch (routeNavigator.getNavError()) {
                    case PATHFIND_FAILED:
                    case TIME_FAIL:
                        logError("Pathfinding failed, trying to rotate to vang and opening anyways.");
                        routeNavigator.stop();
                        Pathfinder.getInstance().stop();
                        swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 400);
                        break;
                }
                break;
            case ROTATING_TO_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                RotationHandler.getInstance().stop();

                RotationHandler.getInstance().easeTo(new RotationConfiguration(
                        new Target(vanguard.getPositionVector().addVector(0, -0.5, 0)),
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
                    if (++vangRetry >= 3) {
                        log("failed to rotate completely, restarting pathfinder to vang entity");
                        Pathfinder.getInstance().stopAndRequeue(EntityUtil.getBlockBelow(vanguard));
                        if (!Pathfinder.getInstance().isRunning()) {
                            log("pathfinder not enabled, starting");
                            Pathfinder.getInstance().setInterpolationState(true);
                            Pathfinder.getInstance().start();
                        }
                    }
                    swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 0);
                }

                if (!Objects.equals(Minecraft.getMinecraft().thePlayer.rayTrace(2, 0).entityHit, vanguard)) {
                    KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward, true);
                }

                log("Rotation confirmed, opening Vanguard");
                swapState(HandleShaftStateState.OPENING_VANGUARD, MightyMinerConfig.vanguardWalkForwardTime);
                RotationHandler.getInstance().stop();
                break;
            case OPENING_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                KeyBindUtil.releaseAllExcept();
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/p warp");
                log("Opening Vanguard");
                KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem, true);
                swapState(HandleShaftStateState.REACTING_TO_VANGUARD, 200);
                break;
            case REACTING_TO_VANGUARD:
                if (timer.isScheduled() && !timer.passed()) break;
                log("Reacting to Vanguard");
                if (vanguardFound) {
                    swapState(HandleShaftStateState.RETURNING_TO_BASE, MightyMinerConfig.vanguardReactionTime);
                } else {
                    swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 0);
                }
                break;
            case RETURNING_TO_BASE:
                if (timer.isScheduled() && !timer.passed()) break;
                log("Returning to base");
                // Logic to return to base
                return new WarpingState();
            case PATHING_TO_VANGUARD_AFTER_FAIL:
                if (Pathfinder.getInstance().isRunning())
                    break;

                if (Pathfinder.getInstance().succeeded()) {
                    swapState(HandleShaftStateState.ROTATING_TO_VANGUARD, 0);
                    log("Succeded pathfinding");
                    vangRetry = 0;
                    break;
                }

                if (Pathfinder.getInstance().failed()) {
                    log("failed, returning to base");
                    swapState(HandleShaftStateState.RETURNING_TO_BASE, 5000);
                    break;
                }
        }
        return this;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting handle shaft state");
        pathing = false;
        routeNavigator.stop();
        RotationHandler.getInstance().stop();
        KeyBindUtil.releaseAllExcept();
    }

    private void swapState(HandleShaftStateState newState, int delay) {
        this.handleShaftState = newState;
        timer.schedule(delay);
        log("Switched to state: " + newState);
    }

    @Override
    public void onMotionUpdate(ShaftMacro macro) {
        if (handleShaftState == HandleShaftStateState.PATHING_TO_VANGUARD2) {
            Block below = Minecraft.getMinecraft().theWorld.getBlockState(PlayerUtil.getBlockStandingOnFloor()).getBlock();
            if (below == Blocks.ladder) { //|| below == Blocks.air) {
                overLadder = true;
            }
        }
        if (handleShaftState == HandleShaftStateState.PATHING_TO_VANGUARD2) {
            Block below = Minecraft.getMinecraft().theWorld.getBlockState(PlayerUtil.getBlockStandingOnFloor()).getBlock();
            if (below == Blocks.air && overLadder) {
                overAir = true;
            }
        }
        if (vanguard != null) {
            if (Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(vanguard) < 100) {
                overLadder = true;
                overAir = true;
            }
        }
    }

    @Override
    public void onChat(ShaftMacro macro, String message) {
        if (message.contains("VANGUARD CORPSE LOOT!")) {
            vanguardFound = true;
        }
    }
}
