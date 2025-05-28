package com.jelly.mightyminerv2.macro.impl.autoshafts;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.impl.autoshafts.states.AutoShaftState;
import com.jelly.mightyminerv2.macro.impl.autoshafts.states.StartingState;
import lombok.Getter;
import lombok.Setter;

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
}
