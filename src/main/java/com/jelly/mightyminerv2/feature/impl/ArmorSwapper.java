package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.Logger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArmorSwapper extends AbstractFeature {
    private static ArmorSwapper instance;

    public static ArmorSwapper getInstance() {
        if (instance == null) {
            instance = new ArmorSwapper();
        }
        return instance;
    }

    @Getter
    @Setter
    private ARMOR_SWAPPER_STATE state = ARMOR_SWAPPER_STATE.NONE;
    public enum ARMOR_SWAPPER_STATE {
        STARTING,
        OPENING_WARDROBE,
        SWAPPING_PAGE,
        EQUIPPING_ARMOR,
        CLOSING_WARDROBE,
        NONE
    }

    short slot = 0;

    short page = 0;

    @Override
    public String getName() {
        return "ArmorSwapper";
    }

    public void start(short page, short slot) {
        this.page = page;
        this.slot = slot;
        this.state = ARMOR_SWAPPER_STATE.STARTING;
        this.enabled = true;
        this.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @SubscribeEvent
    protected void onTick(TickEvent.ClientTickEvent event) {
        if(!this.isEnabled()) {
            return;
        }

        switch (state) {
            case STARTING:
                this.swapState(ARMOR_SWAPPER_STATE.OPENING_WARDROBE, 100);
                break;
            case OPENING_WARDROBE:
                mc.thePlayer.sendChatMessage("/wd " + page);
                this.swapState(ARMOR_SWAPPER_STATE.SWAPPING_PAGE, 100);
                break;
            case SWAPPING_PAGE:
                if (!this.timer.passed() && this.timer.isScheduled()) {
                    break;
                }

                if (!(mc.currentScreen instanceof GuiChest)) break;

                if (!InventoryUtil.getInventoryName().startsWith("Wardrobe (")) break;

                if (!InventoryUtil.isInventoryLoaded()) break;

                this.swapState(ARMOR_SWAPPER_STATE.EQUIPPING_ARMOR, 100);

                break;
            case EQUIPPING_ARMOR:
                if (!this.timer.passed() && this.timer.isScheduled()) {
                    break;
                }

                if (!(mc.currentScreen instanceof GuiChest)
                        || !InventoryUtil.getInventoryName().startsWith("Wardrobe (")
                        || !InventoryUtil.isInventoryLoaded()
                ) {
                    break;
                }

                int slotId = InventoryUtil.getSlotIdOfItemInContainer("Slot " + slot + ":");
                Logger.sendLog("Slot ID: " + slotId);

                if (slotId == -1) {
                    Logger.sendLog("Slot ID not found, closing wardrobe");
                    this.swapState(ARMOR_SWAPPER_STATE.CLOSING_WARDROBE, 100);
                } else {
                    InventoryUtil.clickContainerSlot(
                            slotId,
                            InventoryUtil.ClickType.LEFT,
                            InventoryUtil.ClickMode.PICKUP
                    );
                    this.swapState(ARMOR_SWAPPER_STATE.CLOSING_WARDROBE, 100);
                }

                break;
            case CLOSING_WARDROBE:
                if (!this.timer.passed() && this.timer.isScheduled()) {
                    break;
                }

                if (!(mc.currentScreen instanceof GuiChest)
                        || !InventoryUtil.getInventoryName().startsWith("Wardrobe (")
                        || !InventoryUtil.isInventoryLoaded()
                ) {
                    break;
                }

                InventoryUtil.closeScreen();
                this.stop();
                break;
            case NONE:
                // Do nothing
                break;
        }
    }

    private void swapState(final ARMOR_SWAPPER_STATE newState, final int time) {
        state = newState;
        this.timer.schedule(time);
        Logger.sendLog("Swapping to substate: " + newState);
    }
}
