package com.jelly.mightyminerv2.util.helper.location;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

// DO NOT REARRANGE THE LAST THREE LOCATIONS - ORDINAL USED IN GAMSTATEHANDLER#ISPLAYERINSKYBLOCK TO REDUCE CHECKS
@Getter
public enum Location {
    PRIVATE_ISLAND("Private Island"),
    HUB("Hub"),
    THE_PARK("The Park"),
    THE_FARMING_ISLANDS("The Farming Islands"),
    SPIDER_DEN("Spider's Den"),
    THE_END("The End"),
    CRIMSON_ISLE("Crimson Isle"),
    GOLD_MINE("Gold Mine"),
    DEEP_CAVERNS("Deep Caverns"),
    DWARVEN_MINES("Dwarven Mines"),
    MINESHAFT("Mineshaft"),
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    JERRY_WORKSHOP("Jerry's Workshop"),
    DUNGEON_HUB("Dungeon Hub"),
    GARDEN("Garden"),
    DUNGEON("Dungeon"),
    LIMBO("UNKNOWN"),
    LOBBY("PROTOTYPE"),
    // Knowhere - Avengers: Infinity War
    KNOWHERE("Knowhere");

    private static final Map<String, Location> nameToLocationMap = new HashMap<>();

    static {
        for (Location location : Location.values()) {
            nameToLocationMap.put(location.getName(), location);
        }
    }

    private final String name;

    Location(String name) {
        this.name = name;
    }

    public static Location fromName(String name) {
        final Location loc = nameToLocationMap.get(name);
        if (loc == null) return Location.KNOWHERE;
        return loc;
    }
}
