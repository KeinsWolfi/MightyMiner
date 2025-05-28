package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;

public class PathingToVeinState implements AutoShaftState {
    private final RouteNavigator routeNavigator = RouteNavigator.getInstance();
    private final String GRAPH_NAME = "Glacial Macro";
    private int attempts = 0;


    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering pathing to vein state");
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting pathing to vein state");
    }
}
