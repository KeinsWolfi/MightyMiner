package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathingToVeinState implements AutoShaftState {
    private final RouteNavigator routeNavigator = RouteNavigator.getInstance();
    private final String GRAPH_NAME = (MightyMinerConfig.etherwarpPath) ? "EtherWarpPath1" : "Glacial Macro";
    private int attempts = 0;

    private final Random random = new Random();

    @Getter
    private static final List<RouteWaypoint> VEINS = new ArrayList<>();

    RouteWaypoint vein1 = new RouteWaypoint(57, 141, 268, TransportMethod.WALK);
    RouteWaypoint vein2 = new RouteWaypoint(104, 124, 333, TransportMethod.WALK);

    private boolean failed = false;

    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering pathing to vein state");
        VEINS.add(vein1);
        VEINS.add(vein2);

        List<RouteWaypoint> nodes = GraphHandler.instance.findPathFrom(GRAPH_NAME, PlayerUtil.getBlockStandingOn(), VEINS.get(MightyMinerConfig.veinIndex));

        if (nodes.isEmpty()) {
            logError("Starting block: " + PlayerUtil.getBlockStandingOn() + ", Ending block: " + VEINS.get(MightyMinerConfig.veinIndex));
            Logger.sendError("Could not find a path to the target block! Please send the logs to the developer.");
            failed = true;
            return;
        }
        routeNavigator.start(new Route(nodes));
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if (failed) return new WarpingState();

        if (routeNavigator.isRunning()) return this;

        if (routeNavigator.succeeded()) {
            macro.setNextVeinIndex(1);
            return new MiningState();
        }

        switch (routeNavigator.getNavError()) {
            case NONE:
                macro.disable("Could not find a path to the target block!");
                break;
            case TIME_FAIL: case PATHFIND_FAILED:
                attempts++;
                if(attempts >= 3) {
                    logError("Failed to pathfind. Warping and restarting");
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp base");
                    return new WarpingState();
                } else {
                    logError("Failed to pathfind. Retrying to pathfind");
                    onStart(macro);
                    return this;
                }
        }
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        routeNavigator.stop();
        log("Exiting pathing to vein state");
    }
}
