package com.jelly.mightyminerv2.util;

import java.awt.*;

public abstract class SystemNotificationUtil {
    public static void systemNotification(String title, String message) {
        try {
            if (!SystemTray.isSupported()) {
                System.out.println("System tray not supported!");
                return;
            }

            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

            TrayIcon trayIcon = new TrayIcon(image, "Test");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("My Java App");
            tray.add(trayIcon);

            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            e.printStackTrace();
        }
    }
}
