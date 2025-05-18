package com.jelly.mightyminerv2.macro.impl.ScathaMacro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.impl.ScathaMacro.states.KillingState;
import com.jelly.mightyminerv2.macro.impl.ScathaMacro.states.ScathaMacroState;
import com.jelly.mightyminerv2.macro.impl.ScathaMacro.states.StartingState;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collections;
import java.util.List;

public class ScathaMacro extends AbstractMacro {

    @Getter
    private static ScathaMacro instance = new ScathaMacro();

    private ScathaMacroState currentState;

    public final TunnelMiner tunnelMiner = TunnelMiner.getInstance();

    @Override
    public String getName() {
        return "Scatha Macro";
    }

    @Override
    public List<String> getNecessaryItems() {
        return Collections.emptyList();
    }

    @Override
    public void onPause() {
        FeatureManager.getInstance().pauseAll();
        log("Scatha macro paused");
    }

    @Override
    public void onResume() {
        FeatureManager.getInstance().resumeAll();
        log("Scatha macro resumed");
    }

    @Override
    public void onEnable() {
        currentState = new StartingState();
        log("Scatha macro enabled");
    }

    @Override
    public void onDisable() {
        if (currentState != null) currentState.onEnd(this);

        tunnelMiner.setError(TunnelMiner.TunnelMinerError.NONE);
        tunnelMiner.setDirection(null);

        log("Scatha macro disabled");
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.isEnabled()) return;

        if (currentState == null) return;

        ScathaMacroState state = currentState.onTick(this);
        transitionTo(state);

        if (tunnelMiner.getError() == TunnelMiner.TunnelMinerError.SHOULD_KILL_SCATHA && !(currentState instanceof KillingState)) {
            transitionTo(new KillingState());
        }
    }

    private void transitionTo(ScathaMacroState state) {
        if (currentState == state) return;

        currentState.onEnd(this);
        currentState = state;

        if (currentState == null) {
            log("null state, returning");
            return;
        }

        currentState.onStart(this);
    }
}
