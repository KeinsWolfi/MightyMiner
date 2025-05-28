package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;

import java.util.List;

public class PathingToVeinState implements AutoShaftState {
    private final RouteNavigator routeNavigator = RouteNavigator.getInstance();
    private final String GRAPH_NAME = "Glacial Macro";
    private int attempts = 0;

    RouteWaypoint end = new RouteWaypoint(57, 141, 268, TransportMethod.WALK);

    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering pathing to vein state");

        List<RouteWaypoint> nodes = GraphHandler.instance.findPathFrom(GRAPH_NAME, PlayerUtil.getBlockStandingOn(), end);

        if (nodes.isEmpty()) {
            logError("Starting block: " + PlayerUtil.getBlockStandingOn() + ", Ending block: " + end);
            macro.disable("Could not find a path to the target block! Please send the logs to the developer.");
            return;
        }
        routeNavigator.start(new Route(nodes));
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if (routeNavigator.isRunning()) return this;

        if (routeNavigator.succeeded()) return new MiningState();

        switch (routeNavigator.getNavError()) {
            case NONE:
                macro.disable("Could not find a path to the target block!");
                break;
            case TIME_FAIL: case PATHFIND_FAILED:
                attempts++;
                if(attempts >= 3) {
                    logError("Failed to pathfind. Warping and restarting");
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
