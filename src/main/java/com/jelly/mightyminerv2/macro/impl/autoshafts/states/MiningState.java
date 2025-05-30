package com.jelly.mightyminerv2.macro.impl.autoshafts.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.macro.impl.autoshafts.ShaftMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;

public class MiningState implements AutoShaftState {
    private final BlockMiner miner = BlockMiner.getInstance();
    private final MineableBlock[] blocksToMine = {MineableBlock.GRAY_MITHRIL, MineableBlock.GREEN_MITHRIL, MineableBlock.BLUE_MITHRIL,
            MineableBlock.TITANIUM};
    private final int[] titaniumPriority = {3, 2, 1, 20};

    private int noFuelCounter = 0;

    @Override
    public void onStart(ShaftMacro macro) {
        log("Entering mining state");
        noFuelCounter = 0; // Reset fuel counter when starting mining state
        miner.start(
                blocksToMine,
                macro.getMiningSpeed(),
                titaniumPriority,
                MightyMinerConfig.miningTool
        );
    }

    @Override
    public AutoShaftState onTick(ShaftMacro macro) {
        String miningTool = MightyMinerConfig.miningTool;
        if (miningTool.toLowerCase().contains("drill") || InventoryUtil.getFullName(miningTool).contains("Drill")) {
            // log("Fuel detected: " + InventoryUtil.getDrillRemainingFuel(miningTool));
            if (InventoryUtil.getDrillRemainingFuel(miningTool) <= 500) {
                noFuelCounter++;
                //log("Less than 100 fuel left in drill. Starting to refuel");
                if(MightyMinerConfig.drillRefuel)
                    if (noFuelCounter >= 300) {
                        return new RefuelState();
                    }
                else {
                    if (noFuelCounter >= 300) {
                        log("No fuel in drill for 300 ticks, restarting macro");
                        return new StartingState();
                    } else {
                        log("No fuel in drill, but refuel is disabled. Continuing mining");
                    }
                }
            }
        }

        if (miner.isRunning()) {
            return this;
        }

        switch(miner.getError()) {
            case NONE:
                break;
            case NO_POINTS_FOUND:
                log ("Restarting because the block chosen cannot be mined");
                return new MiningState();
            case NOT_ENOUGH_BLOCKS:
                log ("Not enough blocks nearby! Restarting macro");
                return new EtherWarpToNextVeinState();
            case NO_PICKAXE_ABILITY:
                log("Not enough pickaxes nearby! Restarting macro");
                KeyBindUtil.releaseAllExcept();
                return new StartingState();
            default:
                logError("Block miner error: " + miner.getError().name());
                KeyBindUtil.releaseAllExcept();
                macro.disable("Block miner failed unexpectedly! Please send the logs to the developer");
                break;
        }
        return null;
    }

    @Override
    public void onEnd(ShaftMacro macro) {
        miner.stop();
        log("Exiting mining state");
    }
}
