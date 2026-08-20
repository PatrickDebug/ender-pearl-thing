package com.annie.carryonenderpearl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;

import java.util.List;

public final class CarryOnEnderPearlAddon implements ModInitializer {
    /*
     * Carry On itself performs the final reach, empty-hand, blacklist,
     * carry-state and other safety checks. This value is only the server-side
     * search distance used to find the pearl the player is aiming at.
     */
    private static final double SEARCH_DISTANCE = 5.0D;

    /*
     * Ender Pearls are tiny and move quickly, so the aiming cylinder is a bit
     * wider than their physical hitbox. Smaller values require more precise aim.
     */
    private static final double AIM_RADIUS = 0.65D;
    private static final double AIM_RADIUS_SQR = AIM_RADIUS * AIM_RADIUS;

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tryPickupAimedPearl(player);
            }
        });
    }

    private static void tryPickupAimedPearl(ServerPlayer player) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);

        // This is Carry On's own key state, already synced by Carry On's client.
        // It lets this addon remain completely server-side.
        if (!carry.isKeyPressed() || carry.isCarrying()) {
            return;
        }

        // Match Carry On's normal requirement before doing the search.
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 end = eye.add(look.scale(SEARCH_DISTANCE));

        AABB searchBox = new AABB(eye, end).inflate(1.0D);
        List<ThrownEnderpearl> pearls = level.getEntitiesOfClass(
                ThrownEnderpearl.class,
                searchBox,
                pearl -> !pearl.isRemoved()
        );

        ThrownEnderpearl best = null;
        double bestAlongRay = Double.MAX_VALUE;

        for (ThrownEnderpearl pearl : pearls) {
            Vec3 toPearl = pearl.position().subtract(eye);

            // Distance along the player's look ray.
            double alongRay = toPearl.dot(look);
            if (alongRay < 0.0D || alongRay > SEARCH_DISTANCE) {
                continue;
            }

            Vec3 closestPoint = eye.add(look.scale(alongRay));
            double perpendicularDistanceSqr = pearl.position().distanceToSqr(closestPoint);

            if (perpendicularDistanceSqr <= AIM_RADIUS_SQR && alongRay < bestAlongRay) {
                best = pearl;
                bestAlongRay = alongRay;
            }
        }

        if (best != null) {
            // Use Carry On's own pickup pipeline so its normal carry data,
            // rendering sync, placement, sounds, slowness, etc. are retained.
            PickupHandler.tryPickupEntity(player, best, null);
        }
    }
}
