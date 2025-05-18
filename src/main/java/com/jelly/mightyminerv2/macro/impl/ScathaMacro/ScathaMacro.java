package com.jelly.mightyminerv2.macro.impl.ScathaMacro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class ScathaMacro extends AbstractMacro {

    @Getter
    private static ScathaMacro instance = new ScathaMacro();
    private final TunnelMiner miner = TunnelMiner.getInstance();

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
        log("Scatha macro enabled");
        miner.start(MightyMinerConfig.miningTool);
    }
}
