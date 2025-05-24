package com.jelly.mightyminerv2.feature.impl.TunnelMiner;

import com.jelly.mightyminerv2.event.WormSpawnEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.states.*;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TunnelMiner extends AbstractFeature {
    private static TunnelMiner instance;

    public static TunnelMiner getInstance() {
        if (instance == null) {
            instance = new TunnelMiner();
        }
        return instance;
    }

    private TunnelMinerState currentState;

    @Getter
    @Setter
    private BlockMiner.PickaxeAbilityState pickaxeAbilityState;

    @Getter
    @Setter
    private TunnelMinerError error = TunnelMinerError.NONE;
    public enum TunnelMinerError {
        NONE,
        NO_TOOLS_AVAILABLE, // Required mining tool not found in inventory
        NO_POINTS_FOUND,    // Cannot find valid points to target on block
        NO_TARGET_BLOCKS,
        NO_PICKAXE_ABILITY,
        SHOULD_KILL_SCATHA,
    }

    @Getter
    @Setter
    private BlockPos targetBlockPos; // BlockPos of current block being mined

    @Getter
    @Setter
    private Block targetBlockType;

    private int retryActivatePickaxeAbility;

    @Getter
    private Map<Integer, Integer> blockPriority = new HashMap<>();

    @Getter
    @Setter
    private TunnelMinerStateEnum tunnelMinerState = TunnelMinerStateEnum.FORWARD;
    public enum TunnelMinerStateEnum {
        FORWARD,
        BACKWARD
    }

    @Getter
    @Setter
    private DIRECTION direction;
    public enum DIRECTION {
        NORTH, EAST, SOUTH, WEST
    }

    @Getter
    @Setter
    private List<BlockPos> blocksBlocking;

    @Override
    public String getName() {
        return "TunnelMiner";
    }

    public void start(String miningTool) {
        if (!miningTool.isEmpty() && !InventoryUtil.holdItem(miningTool)) {
            logError(miningTool + " not found in inventory!");
            error = TunnelMinerError.NO_TOOLS_AVAILABLE;
            this.stop();
            return;
        }

        this.enabled = true;
        this.error = TunnelMinerError.NONE;
        this.pickaxeAbilityState = BlockMiner.PickaxeAbilityState.AVAILABLE;
        this.retryActivatePickaxeAbility = 0;

        log("Starting Tunnel Miner");
        currentState = new StartingState();
        // Initialize with starting state
        this.start();
    }

    @Override
    public void stop() {
        if(currentState != null)
            currentState.onEnd(this);
        super.stop();
        KeyBindUtil.releaseAllExcept();  // Release all keybinds
    }

    @SubscribeEvent
    protected void onTick(TickEvent.ClientTickEvent event) {
        // Skip if not enabled, GUI is open, or not in the correct phase
        if (!this.enabled || event.phase == TickEvent.Phase.END) return;

        if (mc.currentScreen != null && !(currentState instanceof DisablePerksState)) {
            return;
        }

        if (currentState == null)
            return;

        TunnelMinerState nextState = currentState.onTick(this);
        transitionTo(nextState);

        if (retryActivatePickaxeAbility >= 4) {
            setError(TunnelMinerError.NO_PICKAXE_ABILITY);
            stop();
        }
    }

    private void transitionTo(TunnelMinerState nextState) {
        if (currentState == nextState)
            return;

        if ((currentState instanceof StartingState && nextState instanceof ApplyAbilityState)
                || (currentState instanceof ApplyAbilityState && nextState instanceof StartingState)) {
            retryActivatePickaxeAbility ++;
        }
        else {
            retryActivatePickaxeAbility = 0;
        }

        currentState.onEnd(this);
        currentState = nextState;

        if (currentState == null) {
            log("null state, returning");
            return;
        }

        if (currentState instanceof ShouldKillScathaState) {
            log("Should kill");
            setError(TunnelMinerError.SHOULD_KILL_SCATHA);
            return;
        }

        currentState.onStart(this);
    }

    @SubscribeEvent
    protected void onChat(ClientChatReceivedEvent event) {
        if (event.type != 0) {
            return;
        }
        String message = event.message.getUnformattedText();

        if (message.contains("is now available!")) {
            pickaxeAbilityState = BlockMiner.PickaxeAbilityState.AVAILABLE;
        }
        if (message.contains("You used your") || message.contains("Your pickaxe ability is on cooldown for")) {
            pickaxeAbilityState = BlockMiner.PickaxeAbilityState.UNAVAILABLE;
        }

        currentState.onChatMessage(event);
    }

    @SubscribeEvent
    protected void onWormSpawn(WormSpawnEvent event) {
        currentState.onWormSpawn(event);
    }
}
