/*
 * Copyright (c) 2021 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * */
package net.kjp12.glitch.events;// Created 2021-03-17T19:09:53

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kjp12.glitch.events.commands.EventCommand;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author KJP12
 * @since 0.0.0
 */
public class Main implements ModInitializer {
    public static final long TIMEOUT_DURATION = TimeUnit.MINUTES.toMillis(5);
    public static final String SCOREBOARD_KILLS = "events:kills";
    public static final double PI2 = Math.PI * 2D;
    public static IEventManager currentEventManager;
    public static final HashSet<ServerPlayerEntity> spectators = new HashSet<>();
    public static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    public static final Random random = new Random();

    // c* - circumference
    // r* - radius
    // t* - team

    public static void spreadTeamsSlow(Map<String, Set<ServerPlayerEntity>> teams, int duration, int radius, MinecraftServer server) {
        final ServerWorld world = server.getOverworld();
        final Scoreboard scoreboard = world.getScoreboard();
        final double co = Main.random.nextDouble() * PI2, d = PI2 / teams.size(), r = radius * 0.95, tr = r * 0.05;
        int i = 0;
        for (final var e : teams.entrySet()) {
            final var t0 = scoreboard.getTeam(e.getKey());
            final var t = t0 != null ? t0 : scoreboard.addTeam(e.getKey());
            final var vs = e.getValue();
            final double bi = Math.fma(i++, d, co), bx = Math.sin(bi) * r, bz = Math.cos(bi) * r, to = Main.random.nextDouble() * PI2, td = PI2 / vs.size();
            int p = 0;
            for (final var v : vs) {
                final double ti = Math.fma(p++, td, to);
                queueTeleport(server, v, Math.fma(Math.sin(ti), tr, bx), Math.fma(Math.cos(ti), tr, bz), (float) (Math.toDegrees(ti) - 90D), duration, () -> scoreboard.addPlayerToTeam(v.getEntityName(), t));
            }
        }
    }

    public static void spreadPlayersSlow(Set<ServerPlayerEntity> players, int duration, int radius, MinecraftServer server) {
        final double co = Main.random.nextDouble() * PI2, d = PI2 / players.size(), r = radius * 0.95;
        int i = 0;
        for (final var v : players) {
            final double bi = Math.fma(i++, d, co);
            queueTeleport(server, v, Math.sin(bi) * r, Math.cos(bi) * r, (float) (Math.toDegrees(bi) - 90D), duration, null);
        }
    }

    public static void queueTeleport(MinecraftServer server, ServerPlayerEntity player, double ox, double oz, float yaw, int duration, Runnable postTask) {
        final int fx = fastFloor(ox), fz = fastFloor(oz), rd = Main.random.nextInt(duration);
        final double oy = player.world.getChunk(fx >> 4, fz >> 4).getHeightmap(Heightmap.Type.WORLD_SURFACE).get(fx & 15, fz & 15);
        Main.service.schedule(() -> server.execute(() -> {
            var world = (ServerWorld) player.world;
            summonLightning(world, ox, oy, oz);
            summonLightning(world, player.getX(), player.getY(), player.getZ());
            player.setVelocity(0, 0, 0);
            player.updatePositionAndAngles(ox, oy, oz, yaw, 0F);
            player.refreshPositionAfterTeleport(ox, oy, oz);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (duration - rd) / 50, 5));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (duration - rd) / 50, 5));
            player.world.setBlockState(new BlockPos(ox, oy - 1, oz), Blocks.STONE.getDefaultState());
            if (postTask != null) postTask.run();
        }), rd, TimeUnit.MILLISECONDS);
    }

    public static void summonLightning(ServerWorld world, double x, double y, double z) {
        var lightning = EntityType.LIGHTNING_BOLT.create(world);
        assert lightning != null;
        lightning.refreshPositionAfterTeleport(x, y, z);
        world.spawnEntity(lightning);
    }

    public static int fastFloor(double d) {
        return (int) (d + 1024.0D) - 1024;
    }

    public static void setSpectator(ServerPlayerEntity player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.RESET, null));
        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, new LiteralText("Spectator."), 0, 20, 5));
    }

    public static <V> Map<String, Set<V>> mkMapWithRemainders(Set<V> pl, int il, int jl, int r) {
        var m = new HashMap<String, Set<V>>((int) (il * 0.75F) + 1, 0.75F);
        var itr = pl.iterator();
        for (int i = 0, k = 0; i < il && itr.hasNext(); i++) {
            var h = new HashSet<V>((int) (jl * 0.75F) + 1, 0.75F);
            for (int j = 0; j < jl; j++) h.add(itr.next());
            if (k++ < r) h.add(itr.next());
            m.put(String.valueOf(i), h);
        }
        m.values().removeIf(Set::isEmpty);
        return m;
    }

    public static <V> Map<String, Set<V>> mkMapSplitRemainders(Set<V> pl, int il, int jl, int r) {
        var m = new HashMap<String, Set<V>>((int) (il * 0.75F) + 1, 0.75F);
        var itr = pl.iterator();
        for (int i = 0; i < il && itr.hasNext(); i++) {
            var h = new HashSet<V>((int) (jl * 0.75F) + 1, 0.75F);
            for (int j = 0; j < jl; j++) h.add(itr.next());
            m.put(String.valueOf(i), h);
        }
        if (r != 0) {
            var h = new HashSet<V>((int) (r * 0.75F) + 1, 0.75F);
            while (itr.hasNext()) h.add(itr.next());
            m.put(null, h);
        }
        m.values().removeIf(Set::isEmpty);
        return m;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> EventCommand.register(dispatcher));
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (currentEventManager != null) {
                currentEventManager.tick();
            }
        });
    }
}
