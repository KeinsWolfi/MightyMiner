package com.jelly.mightyminerv2.macro.impl.autoshafts;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.impl.autoshafts.states.AutoShaftState;
import com.jelly.mightyminerv2.macro.impl.autoshafts.states.EnterShaftState;
import com.jelly.mightyminerv2.macro.impl.autoshafts.states.StartingState;
import com.jelly.mightyminerv2.util.Logger;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class ShaftMacro extends AbstractMacro {
    @Getter
    private static final ShaftMacro instance = new ShaftMacro();

    private AutoShaftState currentState;

    @Getter
    @Setter
    private int miningSpeed = 0;

    @Override
    public String getName() {
        return "Auto Shafts";
    }

    @Override
    public List<String> getNecessaryItems() {
        List<String> items = new ArrayList<>();
        items.add(MightyMinerConfig.miningTool);
        items.add(MightyMinerConfig.slayerWeapon);

        if (MightyMinerConfig.drillSwap) {
            items.add(MightyMinerConfig.altMiningTool);
        }

        return items;
    }

    @Override
    public void onEnable() {
        log("autoShafts::onEnable");
        if (MightyMinerConfig.setMiningSpeed) {
            this.miningSpeed = MightyMinerConfig.miningSpeed;
        }
        currentState = new StartingState();
    }

    @Override
    public void onDisable() {
        if (currentState != null) {
            currentState.onEnd(this);
        }
        this.miningSpeed = 0;
        log("autoShafts::onDisable");
    }

    @Override
    public void onPause() {
        FeatureManager.getInstance().pauseAll();
        log("autoShafts::onPause");
    }

    @Override
    public void onResume() {
        FeatureManager.getInstance().resumeAll();
        log("autoShafts::onResume");
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (this.isTimerRunning()) {
            return;
        }

        if (currentState == null)
            return;

        AutoShaftState nextState = currentState.onTick(this);
        transitionTo(nextState);
    }

    private void transitionTo(AutoShaftState nextState){
        // Skip if no state change
        if (currentState == nextState)
            return;

        currentState.onEnd(this);
        currentState = nextState;

        if (currentState == null) {
            log("null state, returning");
            return;
        }

        currentState.onStart(this);
    }

    @Override
    public void onChat(String message) {
        if (!this.isEnabled()) return;

        if (message.contains("You found a") && message.contains("Glacite Mineshaft")) {
            Logger.sendLog("Detected Glacite Mineshaft entrance in chat, transitioning to EnterShaftState.");
            transitionTo(new EnterShaftState());
        }
    }
}
