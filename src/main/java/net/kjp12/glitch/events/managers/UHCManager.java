package net.kjp12.glitch.events.managers;// Created 2021-03-17T23:21:51

import net.kjp12.glitch.events.IEventManager;
import net.kjp12.glitch.events.Main;
import net.kjp12.glitch.events.mixin.MixinEntity;
import net.kjp12.glitch.events.mixin.MixinHungerManager;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.LevelInfo;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author KJP12
 * @since 0.0.0
 */
public class UHCManager implements IEventManager {
    private final MinecraftServer server;
    private final long duration;
    private final int start, end;

    private Function<Set<ServerPlayerEntity>, Map<String, Set<ServerPlayerEntity>>> teams;
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
        var properties = server.worlds[0].getLevelProperties();
        properties.setRainTime(1500);
        properties.setRaining(true);
        properties.setThunderTime(1500);
        properties.setThundering(true);
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
            server.worlds[0].getWorldBorder().setSize(start, end, duration);
            server.setPvpEnabled(true);
            for(var p : Main.spectators) Main.setSpectator(p);
            var playerManager = server.getPlayerManager();
            players = new HashSet<>(playerManager.getPlayers());
            players.removeAll(Main.spectators);
            for(var w : server.worlds) {
                var gameRules = w.getGameRules();
                gameRules.setGameRule("naturalRegeneration", "false");
                gameRules.setGameRule("doFireTick", "false");
            }
            final var scoreboard = server.worlds[0].getScoreboard();
            final var kills = scoreboard.method_2131("Kills", ScoreboardCriterion.PLAYERS_KILLED);
            scoreboard.setObjectiveSlot(0, kills);
            if(teams != null) {
                var teams = this.teams.apply(players);
                var spectators = teams.remove(null);
                if(spectators != null) {
                    Main.spectators.addAll(spectators);
                    for (var s : spectators) Main.setSpectator(s);
                }
            } else {
                final int l = players.size();

            }
        } else if(ticks <= 200) {
            if(ticks % 20 == 0) {
                server.getPlayerManager().sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, new LiteralText(Integer.toString(ticks / 20)).setStyle(new Style().setColor(Formatting.RED))));
            }
        } else if(ticks % 200 == 0) {
            server.getPlayerManager().sendToAll(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, new LiteralText(Integer.toString(ticks / 20)).setStyle(new Style().setColor(Formatting.YELLOW))));
        }
    }

    void finalise() {
        for(var p : players) {
            p.setAbsorption(0F);
            p.setAir(300);
            p.setHealth(20F);
            p.extinguish();
            var hunger = (HungerManager & MixinHungerManager) p.getHungerManager();
            hunger.setFoodLevel(20);
            hunger.setFoodSaturationLevel(5.0F);
            hunger.setFoodStarvationTimer(0);
            hunger.setExhaustion(0F);
            var world = p.getServerWorld();
            var properties = world.getLevelProperties();
            p.abilities.invulnerable = false;
            ((MixinEntity) p).setInvulnerable(false);
            p.interactionManager.setGameMode(LevelInfo.GameMode.SURVIVAL);
            p.networkHandler.sendPacket(new GameJoinS2CPacket(p.getEntityId(), LevelInfo.GameMode.SURVIVAL, true, world.dimension.getType(), world.getGlobalDifficulty(), server.getPlayerManager().getMaxPlayerCount(), properties.getGeneratorType(), true));
        }
        for(var w : server.worlds) {
            w.getGameRules().setGameRule("doFireTick", "true");
        }
    }

    @Override
    public void onDeath(ServerPlayerEntity player) {
        player.setGameMode(LevelInfo.GameMode.SPECTATOR);
        players.remove(player);
    }
}
