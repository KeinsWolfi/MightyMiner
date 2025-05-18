package com.jelly.mightyminerv2.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.google.common.collect.ImmutableSet;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.states.BreakingState;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.SystemNotificationUtil;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.Set;

@Command("test")
public class TestCommand {
    @Getter
    private static final TestCommand instance = new TestCommand();

    @Main
    public void main() {
        Logger.sendMessage("Test command:");
        Logger.sendMessage("  /test notification <title> <message>");
        Logger.sendMessage("  /test checkBorderPos");
        Logger.sendMessage("  /test checkFacingBorder");
        Logger.sendMessage("  /test checkAllBorder");
    }

    @SubCommand
    public void notification(String title, String message) {
        SystemNotificationUtil.systemNotification(title, message);
    }

    @SubCommand
    public void checkBorderPos() {
        Logger.sendWarning(String.valueOf(BreakingState.atBorder()) + " pos: " + Minecraft.getMinecraft().thePlayer.getPosition());
    }

    @SubCommand
    public void checkFacingBorder() {
        Logger.sendWarning(String.valueOf(BreakingState.facingBorder()) + " yaw: " + Minecraft.getMinecraft().thePlayer.rotationYaw + " pos: " + Minecraft.getMinecraft().thePlayer.getPosition());
    }

    @SubCommand
    public void checkAllBorder() {
        Logger.sendWarning(String.valueOf(BreakingState.atBorder() && BreakingState.facingBorder()) + " pos: " + Minecraft.getMinecraft().thePlayer.getPosition() + " yaw: " + Minecraft.getMinecraft().thePlayer.rotationYaw);
    }

    @SubCommand
    public void startKiller(String namesString) {
        String[] names = namesString.split(",");
        if (names.length == 0) {
            Logger.sendError("Please provide a name");
            return;
        }
        Set<String> mobName = ImmutableSet.of("Knifethrower ", "[Lv25] Goblin ");
        AutoMobKiller.getInstance().start(
                mobName,
                MightyMinerConfig.slayerWeapon
        );
        Logger.sendMessage("Added " + names.length + " names to the killer. (" + mobName + ")");
    }

    @SubCommand
    public void stopKiller() {
        AutoMobKiller.getInstance().stop();
        Logger.sendMessage("Stopped the killer");
    }
}
