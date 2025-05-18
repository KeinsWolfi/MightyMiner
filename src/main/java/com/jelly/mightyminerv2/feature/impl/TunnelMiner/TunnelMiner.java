package com.jelly.mightyminerv2.feature.impl.TunnelMiner;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.states.ApplyAbilityState;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.states.StartingState;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.states.TunnelMinerState;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.HashMap;
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

    @Override
    public String getName() {
        return "TunnelMiner";
    }

    public void start(MineableBlock[] blocksToMine, final int[] priority, String miningTool) {
        if (!miningTool.isEmpty() && !InventoryUtil.holdItem(miningTool)) {
            logError(miningTool + " not found in inventory!");
            error = TunnelMinerError.NO_TOOLS_AVAILABLE;
            this.stop();
            return;
        }

        if (blocksToMine == null || Arrays.stream(priority).allMatch(i -> i == 0)) {
            logError("Target blocks not set!");
            error = TunnelMinerError.NO_TARGET_BLOCKS;
            return;
        }

        for (int i = 0; i < blocksToMine.length; i++) {
            for (int j : blocksToMine[i].stateIds) {
                blockPriority.put(j, priority[i]);
            }
        }

        this.enabled = true;
        this.error = TunnelMinerError.NONE;
        this.pickaxeAbilityState = BlockMiner.PickaxeAbilityState.AVAILABLE;
        this.retryActivatePickaxeAbility = 0;

        // Initialize with starting state
        this.currentState = new StartingState();
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
        if (!this.enabled || mc.currentScreen != null || event.phase == TickEvent.Phase.END) {
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
    }
}
