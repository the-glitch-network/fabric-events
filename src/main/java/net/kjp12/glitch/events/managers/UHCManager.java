package net.kjp12.glitch.events.managers;// Created 2021-03-17T23:21:51

import net.kjp12.glitch.events.IEventManager;
import net.kjp12.glitch.events.Main;
import net.kjp12.glitch.events.mixin.MixinHungerManager;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.kjp12.glitch.events.Main.SCOREBOARD_KILLS;
import static net.kjp12.glitch.events.Main.service;

/**
 * @author KJP12
 * @since 0.0.0
 */
public class UHCManager implements IEventManager {
    private final MinecraftServer server;
    private final long duration;
    private final int start, end;

    private final Function<Set<ServerPlayerEntity>, Map<String, Set<ServerPlayerEntity>>> teams;
    private Set<ServerPlayerEntity> players;

    private int ticks = 1200;

    public UHCManager(MinecraftServer server, long duration, Function<Set<ServerPlayerEntity>, Map<String, Set<ServerPlayerEntity>>> teams, int start, int end) {
        this.server = server;
        this.duration = duration;
        this.teams = teams;
        this.start = start;
        this.end = end;
    }

    @Override
    public void onActivate() {
        var playerManager = server.getPlayerManager();
        playerManager.sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, new LiteralText("seconds until UHC starts."), 0, 20, 5));
        playerManager.sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, new LiteralText("60")));
        server.getOverworld().setWeather(0, 1500, true, true);
    }

    @Override
    public void onEnd() {
        var worldBorder = server.getOverworld().getWorldBorder();
        worldBorder.setSize(worldBorder.getSize());
        server.sendSystemMessage(new LiteralText("UHC has ended."), Util.NIL_UUID);
        service.schedule(() -> server.execute(this::removeScoreboard), 5, TimeUnit.MINUTES);
    }

    private void removeScoreboard() {
        var scoreboard = server.getScoreboard();
        var ktmp = scoreboard.getNullableObjective(SCOREBOARD_KILLS);
        if (ktmp != null) {
            scoreboard.removeObjective(ktmp);
        }
    }

    @Override
    public boolean hasStarted() {
        return ticks < 0;
    }

    @Override
    public void tick() {
        ticks--;
        if(ticks < 0) return;
        if(ticks == 0) {
            server.getOverworld().getWorldBorder().interpolateSize(start, end, duration);
            server.setPvpEnabled(true);
            for (var p : Main.spectators) Main.setSpectator(p);
            var playerManager = server.getPlayerManager();
            players = new HashSet<>(playerManager.getPlayerList());
            players.removeAll(Main.spectators);
            server.getGameRules().get(GameRules.NATURAL_REGENERATION).set(true, server);
            server.getGameRules().get(GameRules.DO_FIRE_TICK).set(true, server);
            final var scoreboard = server.getScoreboard();
            removeScoreboard();
            final var kills = scoreboard.addObjective(SCOREBOARD_KILLS, ScoreboardCriterion.PLAYER_KILL_COUNT, new LiteralText("Kills"), ScoreboardCriterion.RenderType.INTEGER);
            scoreboard.setObjectiveSlot(0, kills);
            if (teams != null) {
                var teams = this.teams.apply(players);
                var spectators = teams.remove(null);
                if (spectators != null) {
                    Main.spectators.addAll(spectators);
                    players.removeAll(spectators);
                    for (var s : spectators) Main.setSpectator(s);
                }
                Main.spreadTeamsSlow(teams, 12500, (int) (start * 0.45), server);
            } else {
                Main.spreadPlayersSlow(players, 12500, (int) (start * 0.45), server);
            }
            Main.service.schedule(() -> server.execute(this::finalise), 15000, TimeUnit.MILLISECONDS);
        } else if(ticks <= 200) {
            if(ticks % 20 == 0) {
                server.getPlayerManager().sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, new LiteralText(Integer.toString(ticks / 20)).formatted(Formatting.RED)));
            }
        } else if(ticks % 200 == 0) {
            server.getPlayerManager().sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, new LiteralText(Integer.toString(ticks / 20)).formatted(Formatting.YELLOW)));
        }
    }

    void finalise() {
        for(var p : players) {
            p.setAbsorptionAmount(0F);
            p.setAir(300);
            p.setHealth(20F);
            p.extinguish();
            var hunger = (HungerManager & MixinHungerManager) p.getHungerManager();
            hunger.setFoodLevel(20);
            hunger.setFoodSaturationLevel(5.0F);
            hunger.setFoodStarvationTimer(0);
            hunger.setExhaustion(0F);
            p.abilities.invulnerable = false;
            p.abilities.flying = false;
            p.abilities.allowFlying = false;
            p.setInvulnerable(false);
            p.setGameMode(GameMode.SURVIVAL);
        }
    }

    @Override
    public void onDeath(ServerPlayerEntity player) {
        if (players == null) return;
        player.setGameMode(GameMode.SPECTATOR);
        players.remove(player);

        System.out.printf("%s has died. %d remain. %s\n", player, players.size(), players);

        if (players.size() == 1) {
            Main.currentEventManager = null;
            this.onEnd();
            var playerManager = server.getPlayerManager();
            playerManager.sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, new LiteralText("Winner of the UHC"), 0, 150, 10));
            playerManager.sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, players.iterator().next().getName()));
        }
    }
}
