package com.jelly.mightyminerv2.feature.impl.TunnelMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.TunnelMiner.TunnelMiner;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;

public class DisablePerksState implements TunnelMinerState {
    protected final Clock timer = new Clock();
    protected final Clock perksTimer = new Clock();

    private final Minecraft mc = Minecraft.getMinecraft();

    enum STATE {
        NONE,
        TOGGLE_PERKS,
    }

    enum SUBSTATE {
        OPENING_HOTM,
        TOGGLING_PERKS,
        CLOSING_HOTM
    }

    enum PERK {
        MOLE,
        EFFICIENT_MINER
    }

    private STATE state = STATE.NONE;
    private SUBSTATE substate = SUBSTATE.OPENING_HOTM;
    private PERK perk = PERK.MOLE;

    @Override
    public void onStart(TunnelMiner miner) {
        Logger.sendMessage("Entering DisablePerksState");
        state = STATE.TOGGLE_PERKS;
        substate = SUBSTATE.OPENING_HOTM;
    }

    @Override
    public TunnelMinerState onTick(TunnelMiner miner) {
        if (state == STATE.TOGGLE_PERKS) {
            switch (substate) {
                case OPENING_HOTM:
                    mc.thePlayer.sendChatMessage("/hotm");
                    swapSubState(SUBSTATE.TOGGLING_PERKS, 1000);
                    break;
                case TOGGLING_PERKS:
                    if (!timer.passed() && timer.isScheduled()) {
                        break;
                    }

                    if (
                            !(mc.currentScreen instanceof GuiChest)
                                    || !InventoryUtil.getInventoryName().equals("Heart of the Mountain")
                                    || !InventoryUtil.isInventoryLoaded()
                    ) {
                        break;
                    }

                    int moleSlot = InventoryUtil.getSlotIdOfItemInContainer("Mole");
                    int efficientMinerSlot = InventoryUtil.getSlotIdOfItemInContainer("Efficient Miner");

                    switch (perk) {
                        case MOLE:
                            if (moleSlot == -1) {
                                logError("No Mole perk found! In theory this should NEVER happen!!! Please contact the developer");
                                miner.stop();
                                miner.setError(TunnelMiner.TunnelMinerError.NO_POINTS_FOUND);
                                return null;
                            }
                            InventoryUtil.clickContainerSlot(
                                    moleSlot,
                                    1,
                                    InventoryUtil.ClickMode.QUICK_MOVE
                            );
                            perk = PERK.EFFICIENT_MINER;
                            perksTimer.schedule(MightyMinerConfig.getRandomGuiWaitDelay());
                            break;
                        case EFFICIENT_MINER:
                            if (perksTimer.isScheduled() && !perksTimer.passed()) break;

                            if (efficientMinerSlot == -1) {
                                logError("No Efficient Miner perk found! In theory this should NEVER happen!!! Please contact the developer");
                                miner.stop();
                                miner.setError(TunnelMiner.TunnelMinerError.NO_POINTS_FOUND);
                                return null;
                            }
                            InventoryUtil.clickContainerSlot(
                                    efficientMinerSlot,
                                    1,
                                    InventoryUtil.ClickMode.QUICK_MOVE
                            );
                            swapSubState(SUBSTATE.CLOSING_HOTM, 1000);
                            break;
                    }
                    break;
                case CLOSING_HOTM:
                    if (timer.isScheduled() && !timer.passed()) break;

                    InventoryUtil.closeScreen();
                    state = STATE.NONE;
                    return new RotatingState();
            }
        }

        return this;
    }

    private void swapSubState(final SUBSTATE newSubState, final int time) {
        substate = newSubState;
        timer.schedule(time);
        Logger.sendLog("Swapping to substate: " + newSubState);
    }

    @Override
    public void onEnd(TunnelMiner miner) {
        Logger.sendLog("Exiting DisablePerksState");
        state = STATE.NONE;
    }
}
