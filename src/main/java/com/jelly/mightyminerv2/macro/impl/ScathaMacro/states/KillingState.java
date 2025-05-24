package com.jelly.mightyminerv2.macro.impl.ScathaMacro.states;

import com.google.common.collect.ImmutableSet;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.macro.impl.ScathaMacro.ScathaMacro;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.*;

public class KillingState implements ScathaMacroState {
    protected final Clock timer = new Clock();
    protected final Clock shutdownTimer = new Clock();

    Random random = new Random();

    private Optional<EntityLivingBase> targetMob = Optional.empty();
    private EntityLivingBase targetEntity = null;
    private final Set<EntityLivingBase> mobQueue = new HashSet<>();
    Set<String> mobName = ImmutableSet.of("Scatha", "Worm");

    @Override
    public void onStart(ScathaMacro macro) {
        log("Entering Killing State");

        changeState(KILLER_STATE.STARTING, 0);
    }

    @Override
    public ScathaMacroState onTick(ScathaMacro macro) {
        switch (state) {
            case STARTING:
                changeState(KILLER_STATE.QUEUEING, 500);
                Logger.sendLog("Killing State: Starting, changing to QUEUEING");
                InventoryUtil.holdItem(MightyMinerConfig.slayerWeapon);
                macro.disable();
                break;
            case QUEUEING:
                if (!timer.passed() && timer.isScheduled()) {
                    break;
                }

                List<EntityLivingBase> targetMobs = EntityUtil.getEntitiesSimple(mobName, mobQueue);

                if (targetMobs.isEmpty()) {
                    if (!this.shutdownTimer.isScheduled()) {
                        log("Cannot find mobs. Starting a 1 second timer");
                        this.shutdownTimer.schedule(1_000);
                    }
                    return this;
                } else if (this.shutdownTimer.isScheduled()) {
                    this.shutdownTimer.reset();
                }

                EntityLivingBase best = null;
                for (EntityLivingBase mob : targetMobs) {
                    BlockPos mobPos = EntityUtil.getBlockBelow(mob);
                    if (BlockUtil.canStandOn(mobPos)) {
                        best = mob;
                        break;
                    }
                }
                if (best == null) {
                    log("Didnt find a mob that has a valid position. ");
                    this.changeState(KILLER_STATE.STARTING, 500);
                    return this;
                }
                if (!targetMob.isPresent() || !targetMob.get().equals(best)) {
                    this.targetMob = Optional.of(best);
                    this.targetEntity = best;
                    changeState(KILLER_STATE.INITIAL_ROTATION, 1000);
                }
                break;
            case INITIAL_ROTATION:
                if (!timer.passed() && timer.isScheduled()) {
                    break;
                }

                if (targetMob.isPresent()) {
                    EntityLivingBase mob = targetMob.get();
                    BlockPos mobPos = EntityUtil.getBlockBelow(mob);
                    Vec3 lookVec = new Vec3(mobPos.getX() + 0.5, mobPos.getY() + 1.7, mobPos.getZ() + 0.5);
                    Angle angle = AngleUtil.getRotation(lookVec);
                    RotationHandler.getInstance().easeTo(
                            new RotationConfiguration(
                                    angle,
                                    400,
                                    null
                            )
                    );
                    changeState(KILLER_STATE.PATHING, 0);
                }
                break;
            case PATHING:
                if (!timer.passed() && timer.isScheduled()) {
                    break;
                }

                if (targetMob.isPresent()) {
                    EntityLivingBase mob = targetMob.get();
                    BlockPos mobPos = EntityUtil.getBlockBelow(mob);

                    double distance = Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(mob);

                    if (distance > 9 && distance < 144) {
                        KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward, true);
                    } else if (Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(mob) < 4) {
                        KeyBindUtil.releaseAllExcept();
                        changeState(KILLER_STATE.KILLING, 0);
                    } else if (distance > 144) {
                        changeState(KILLER_STATE.INITIAL_ROTATION, 0);
                    }
                }
                break;
            case KILLING:
                if (!timer.passed() && timer.isScheduled()) {
                    break;
                }

                if (targetMob.isPresent()) {
                    EntityLivingBase mob = targetMob.get();
                    // BlockPos mobPos = EntityUtil.getBlockBelow(mob);
                    // Vec3 lookVec = new Vec3(mobPos.add(0 , 1.5, 0));
                    // RotationHandler.getInstance().easeTo(
                    //         new RotationConfiguration(
                    //                 new Target(lookVec),
                    //                 100,
                    //                 null
                    //         )
                    // );

                    KeyBindUtil.leftClick();
                    timer.schedule(100 + random.nextDouble() * 100);

                    if (Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(mob) > 9) {
                        changeState(KILLER_STATE.INITIAL_ROTATION, 0);
                    } else {
                        if (!EntityUtil.getEntitiesSimple(mobName, mobQueue).contains(mob)) {
                            Logger.sendMessage("Killed " + mob.getCustomNameTag() + "returning to Mining.");

                            this.targetMob = Optional.empty();
                            this.mobQueue.remove(mob);
                            this.changeState(KILLER_STATE.STARTING, 0);
                            return new MiningState();
                        } else {
                            log("Mob is not dead. Rechecking");
                            this.changeState(KILLER_STATE.PATHING, 0);
                        }
                    }
                }
                break;
        }

        return this;
    }

    @Override
    public void onEnd(ScathaMacro macro) {
        AutoMobKiller.getInstance().stop();
        log("Exiting Killing State");
    }

    @Getter
    @Setter
    private KILLER_STATE state;
    public enum KILLER_STATE {
        STARTING,
        QUEUEING,
        PATHING,
        INITIAL_ROTATION,
        KILLING
    }

    private void changeState(KILLER_STATE state, int time) {
        this.state = state;
        timer.schedule(time);
    }
}
