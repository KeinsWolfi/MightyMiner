package com.jelly.mightyminerv2.handler;

import com.jelly.mightyminerv2.event.WormSpawnEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventDispatcher {
    
    private long lastPreAlertTime = 0;

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        long now = System.currentTimeMillis();
        if
        (now - lastPreAlertTime > 10000 && event.name.equals("mob.spider.step")
                &&
                (event.sound.getPitch() == 2.0952382f)
        ) {
            MinecraftForge.EVENT_BUS.post(new WormSpawnEvent());

            lastPreAlertTime = now;
        }

    }
}
