package com.jelly.mightyminerv2.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.SystemNotificationUtil;
import lombok.Getter;

@Command("test")
public class TestCommand {
    @Getter
    private static final TestCommand instance = new TestCommand();

    @Main
    public void main() {
        Logger.sendMessage("Test command:");
        Logger.sendMessage("  /test notification <title> <message>");
    }

    @SubCommand
    public void notification(String title, String message) {
        SystemNotificationUtil.systemNotification(title, message);
    }
}
