package com.jelly.mightyminerv2.util;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor;
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EntityUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isNpc(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (!(entity instanceof EntityOtherPlayerMP)) {
            return false;
        }
        return !TablistUtil.getTabListPlayersSkyblock().contains(entity.getName());
    }

    public static BlockPos getBlockBelow(Entity entity) {
        for (int i = 1; i < 20; i++) {
            BlockPos pos = new BlockPos(entity.posX, 30, entity.posZ);
            if (mc.theWorld.getBlockState(pos).getBlock().isFullCube()) {
                return pos;
            }
        }
        return new BlockPos(entity.posX, entity.posY - 1, entity.posZ);
    }

    public static BlockPos getBlockStandingOn(Entity entity) {
        return new BlockPos(entity.posX, Math.ceil(entity.posY - 0.25) - 1, entity.posZ);
    }

    public static Optional<Entity> getEntityLookingAt() {
        return Optional.ofNullable(mc.objectMouseOver.entityHit);
    }

    public static boolean isStandDead(String name) {
        return getHealthFromStandName(name, true) == 0;
    }

    public static int getHealthFromStandName(String name, boolean noMaxHealth) {
        int health = 0;
        try {
            String cleanedName = StringUtils.stripControlCodes(name).replace(",", "");

            if (noMaxHealth) {
                // Use regex to find the number before ❤
                Pattern pattern = Pattern.compile("(\\d+)(?=❤)");
                Matcher matcher = pattern.matcher(cleanedName);
                if (matcher.find()) {
                    health = Integer.parseInt(matcher.group(1));
                }
            } else {
                // Get last part and extract the number before "/"
                String[] arr = cleanedName.split(" ");
                health = Integer.parseInt(arr[arr.length - 1].split("/")[0].replace(",", ""));
            }
        } catch (Exception ignored) {
            // You can log or handle exceptions if needed
        }

        return health;
    }

    public static Entity getEntityCuttingOtherEntity(Entity e, Class<?> entityType) {
        List<Entity> possible = mc.theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
            boolean flag1 = (!a.isDead && !a.equals(mc.thePlayer));
            boolean flag2 = !(a instanceof EntityArmorStand);
            boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
            boolean flag4 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
            boolean flag5 = (entityType == null || entityType.isInstance(a));
            return flag1 && flag2 && flag3 && flag4 && flag5;
        });
        if (!possible.isEmpty()) return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        return null;
    }

    public static List<EntityLivingBase> getEntitiesSimple(Set<String> entityNames, Set<EntityLivingBase> entitiesToIgnore) {
        List<EntityLivingBase> entities = new ArrayList<>();
        mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityArmorStand)
                .filter((v) ->
                        entityNames.stream().anyMatch((a) -> (v.hasCustomName() ? StringUtils.stripControlCodes(v.getCustomNameTag()) : "").contains(a)))
                .collect(Collectors.toList()).forEach((entity) -> {
                    if (!entitiesToIgnore.contains((EntityLivingBase) entity) && !entity.equals(mc.thePlayer)) {
                        entities.add((EntityLivingBase) entity);
                        Logger.sendLog("Found entity: " + entity.getName() + " with health: " + ((EntityLivingBase) entity).getHealth() + " and name: " + (entity.hasCustomName() ? entity.getCustomNameTag() : entity.getName()));
                    }

                    Entity livingBase = getEntityCuttingOtherEntity(entity, null);
                    if (livingBase instanceof EntityLivingBase) {
                        if (!entitiesToIgnore.contains((EntityLivingBase) livingBase) && !livingBase.equals(mc.thePlayer)) {
                            entities.add((EntityLivingBase) livingBase);
                            Logger.sendLog("Found entity: " + livingBase.getName() + " with health: " + ((EntityLivingBase) livingBase).getHealth() + " and name: " + (livingBase.hasCustomName() ? livingBase.getCustomNameTag() : livingBase.getName()));
                        }
                    }
                });

        Vec3 playerPos = mc.thePlayer.getPositionVector();
        float normalizedYaw = AngleUtil.normalizeAngle(mc.thePlayer.rotationYaw);
        return entities.stream()
                .sorted(Comparator.comparingDouble(ent -> {
                            Vec3 entPos = ent.getPositionVector();
                            double distanceCost = playerPos.distanceTo(entPos);
                            double angleCost = Math.abs(AngleUtil.getNeededYawChange(normalizedYaw, AngleUtil.getRotationYaw(entPos)));
                            return distanceCost * ((float) MightyMinerConfig.devMKillDist / 100f) + angleCost * ((float) MightyMinerConfig.devMKillRot / 100f);
                        }
                )).collect(Collectors.toList());
    }

    public static List<EntityLivingBase> getEntities(Set<String> entityNames, Set<EntityLivingBase> entitiesToIgnore) {
        List<EntityLivingBase> entities = new ArrayList<>();
        mc.theWorld.loadedEntityList.stream()
            .filter(entity -> entity instanceof EntityArmorStand)
            .filter((v) ->
                    !v.getName().contains(mc.thePlayer.getName()) && !v.isDead &&
                    entityNames.stream().anyMatch((a) -> v.getCustomNameTag().contains(a)) &&
                    ((EntityLivingBase) v).getHealth() > 0)
            .collect(Collectors.toList()).forEach((entity) -> {
                Entity livingBase = getEntityCuttingOtherEntity(entity, null);
                if (livingBase instanceof EntityLivingBase) {
                    if (!entitiesToIgnore.contains((EntityLivingBase) livingBase) && !livingBase.equals(mc.thePlayer)) {
                        entities.add((EntityLivingBase) livingBase);
                    }
                }
            });

        Vec3 playerPos = mc.thePlayer.getPositionVector();
        float normalizedYaw = AngleUtil.normalizeAngle(mc.thePlayer.rotationYaw);
        return entities.stream()
                .filter(EntityLivingBase::isEntityAlive)
                .sorted(Comparator.comparingDouble(ent -> {
                            Vec3 entPos = ent.getPositionVector();
                            double distanceCost = playerPos.distanceTo(entPos);
                            double angleCost = Math.abs(AngleUtil.getNeededYawChange(normalizedYaw, AngleUtil.getRotationYaw(entPos)));
                            return distanceCost * ((float) MightyMinerConfig.devMKillDist / 100f) + angleCost * ((float) MightyMinerConfig.devMKillRot / 100f);
                        }
                )).collect(Collectors.toList());
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }


    public static BlockPos nearbyBlock(EntityLivingBase entityLivingBase) {
        BlockPos closestBlock = null;
        double closestDistance = Double.MAX_VALUE;
        BlockStateAccessor bsa = new BlockStateAccessor(mc.theWorld);

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos currentPos = entityLivingBase.getPosition().add(x, y, z);

                    if (MovementHelper.INSTANCE.canStandOn(
                            bsa,
                            currentPos.getX(),
                            currentPos.getY(),
                            currentPos.getZ(),
                            bsa.get(currentPos.getX(), currentPos.getY(), currentPos.getZ())
                    ) && RaytracingUtil.canSeePoint(new Vec3(currentPos), entityLivingBase.getPositionEyes(1.0F))) {
                        double distance = currentPos.distanceSq(PlayerUtil.getBlockStandingOn());

                        if (distance < closestDistance) {
                            closestBlock = currentPos;
                            closestDistance = distance;
                        }
                    }
                }
            }
        }

        if (closestBlock == null) {
            return getBlockStandingOn(entityLivingBase);
        }

        return closestBlock;
    }

}
