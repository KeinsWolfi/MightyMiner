package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.location.Location;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;

import java.util.List;

public class StartingState implements AutoShaftState {
    @Override
    public void onStart(ShaftMacro macro) {
        log("Entrring starting state");
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        if (!InventoryUtil.areItemsInHotbar(macro.getNecessaryItems())) {
            logError("Not all necessary items are in the hotbar. Please put the following items in your hotbar: " + macro.getNecessaryItems());
        }

        if (GameStateHandler.getInstance().getCurrentSubLocation() == SubLocation.DWARVEN_BASE_CAMP || GameStateHandler.getInstance().getCurrentSubLocation() == SubLocation.GLACITE_TUNNELS) {
            if (InventoryUtil.isInventoryFull()) {
                return new SellingInventoryState();
            }

            if (InventoryUtil.getAmountOfItemInInventory("Skeleton Key") < 1) {
                return new BuyingSkeletonKeyState();
            }

            return macro.getMiningSpeed() == 0 ? new GettingStatsState() : new PathingToVeinState();
        } else {
            return new WarpingState();
        }
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        log("Exiting starting state");
    }
}
