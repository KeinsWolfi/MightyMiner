package com.jelly.mightyminerv2.macro.impl.ScathaMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.macro.impl.ScathaMacro.ScathaMacro;
import com.jelly.mightyminerv2.util.Logger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

public class MiningState implements ScathaMacroState {

    @Getter
    @Setter
    private boolean shouldScathaKill = false;

    @Override
    public void onStart(ScathaMacro macro) {
        log("Entering Mining State");
        if (Minecraft.getMinecraft().thePlayer.rotationPitch < 0) {
            macro.tunnelMiner.setTunnelMinerState(TunnelMiner.TunnelMinerStateEnum.BACKWARD);
        }
        macro.tunnelMiner.start(MightyMinerConfig.miningTool);
    }

    @Override
    public ScathaMacroState onTick(ScathaMacro macro) {
        if (shouldScathaKill) {
            Logger.sendLog("Starting Scatha Kill");
            shouldScathaKill = false;
            return new KillingState();
        }
        return this;
    }

    @Override
    public void onEnd(ScathaMacro macro) {
        log("Exiting Mining State");
        macro.tunnelMiner.stop();
    }
}
