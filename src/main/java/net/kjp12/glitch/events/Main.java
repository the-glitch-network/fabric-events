package net.kjp12.glitch.events;// Created 2021-03-17T19:09:53

import net.minecraft.entity.LightningBoltEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.world.level.LevelInfo;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author KJP12
 * @since 0.0.0
 */
public class Main {
    public static final double PI2 = Math.PI * 2D;
    public static IEventManager currentEventManager;
    public static final HashSet<ServerPlayerEntity> spectators = new HashSet<>();
    public static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    public static final Random random = new Random();

    // c* - circumference
    // r* - radius
    // t* - team

    public static void spreadTeamsSlow(Map<String, Set<ServerPlayerEntity>> teams, int duration, int radius, MinecraftServer server) {
        final ServerWorld world = server.worlds[0];
        final Scoreboard scoreboard = world.getScoreboard();
        final int l = teams.size();
        final double co = Main.random.nextDouble() * PI2, d = PI2 / l, r = radius * 0.95, tr = r * 0.05;
        int i = 0;
        for(final var e : teams.entrySet()) {
            final var k = e.getKey();
            scoreboard.addTeam(k);
            final var vs = e.getValue();
            final int it = i++, pl = vs.size();
            final double bi = Math.fma(it,d,co), bx = Math.sin(bi) * r, bz = Math.cos(bi) * r, to = Main.random.nextDouble() * PI2, td = PI2 / pl;
            int p = 0;
            for(final var v : vs) {
                final double ti = Math.fma(p++,td,to), ox = Math.fma(Math.sin(ti),tr,bx), oz = Math.fma(Math.cos(ti),tr,bz);
                final int ix = fastFloor(ox) >> 4, iz = fastFloor(oz) >> 4;
                final double oy = world.getChunk(ix, iz).method_1369(ix, iz);
                Main.service.schedule(() -> server.execute(() -> {
                    world.spawnEntity(new LightningBoltEntity(world, ox, oy, oz));
                    final double vx = v.x, vy = v.y, vz = v.z, yaw = Math.toDegrees(ti) - 180D;
                    v.updatePositionAndAngles(ox, oy, oz, (float) yaw, 0F);
                    v.refreshPositionAfterTeleport(ox, oy, oz);
                    world.spawnEntity(new LightningBoltEntity(world, vx, vy, vz));
                    scoreboard.addPlayerToTeam(v.getTranslationKey(), k);

                }), Main.random.nextInt(duration), TimeUnit.MILLISECONDS);
            }
        }
    }

    public static void spreadPlayersSlow(Set<ServerPlayerEntity> players, int duration, int radius) {
        final double co = Main.random.nextDouble() * PI2;
    }

    public static int fastFloor(double d) {
        return (int)(d + 1024.0D) - 1024;
    }

    public static void setSpectator(ServerPlayerEntity player) {
        player.setGameMode(LevelInfo.GameMode.SPECTATOR);
        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.RESET, null));
        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, new LiteralText("Spectator."), 0, 20, 5));
    }

    public static Map<String, Set<ServerPlayerEntity>> mkMapWithRemainders(Set<ServerPlayerEntity> pl, int il, int jl, int r) {
        var m = new HashMap<String, Set<ServerPlayerEntity>>((int) (il * 0.75F) + 1, 0.75F);
        var itr = pl.iterator();
        for(int i = 0, k = 0; i < il && itr.hasNext(); i++) {
            var h = new HashSet<ServerPlayerEntity>((int)(jl * 0.75F) + 1, 0.75F);
            for(int j = 0; j < jl; j++) h.add(itr.next());
            if(k++ < r) h.add(itr.next());
            m.put(String.valueOf(i), h);
        }
        m.values().removeIf(Set::isEmpty);
        return m;
    }

    public static Map<String, Set<ServerPlayerEntity>> mkMapSplitRemainders(Set<ServerPlayerEntity> pl, int il, int jl, int r) {
        var m = new HashMap<String, Set<ServerPlayerEntity>>((int) (il * 0.75F) + 1, 0.75F);
        var itr = pl.iterator();
        for(int i = 0, k = 0; i < il && itr.hasNext(); i++) {
            var h = new HashSet<ServerPlayerEntity>((int)(jl * 0.75F) + 1, 0.75F);
            for(int j = 0; j < jl; j++) h.add(itr.next());
            m.put(String.valueOf(i), h);
        }
        if(r != 0) {
            var h = new HashSet<ServerPlayerEntity>((int) (r * 0.75F) + 1, 0.75F);
            while(itr.hasNext()) h.add(itr.next());
            m.put(null, h);
        }
        m.values().removeIf(Set::isEmpty);
        return m;
    }

    public static Function<Set<ServerPlayerEntity>, Map<String, Set<ServerPlayerEntity>>> mkTeamSplitter(int teams, int mode) {
        return switch (mode) {
            case 0 -> pl -> {
                int t = pl.size();
                return mkMapWithRemainders(pl, t / teams, teams, t % teams);
            };
            case 1 -> pl -> {
                int t = pl.size();
                return mkMapSplitRemainders(pl, t / teams, teams, t % teams);
            };
            case 2 -> pl -> {
                //var m = new HashMap<String, Set<ServerPlayerEntity>>((int) (teams * 0.75F) + 1, 0.75F);
                int t = pl.size(); // total size
                return mkMapWithRemainders(pl, teams, t / teams, t % teams);
                //int l = t / teams; // size of list
                //int r = t % teams; // remaining to split
                //var itr = pl.iterator();
                //for(int i = 0, k = 0; i < teams && itr.hasNext(); i++) {
                //    var h = new HashSet<ServerPlayerEntity>((int)(l * 0.75F) + 1, 0.75F);
                //    for(int j = 0; j < l; j++) {
                //        h.add(itr.next());
                //    }
                //    if(k++ < r) {
                //        h.add(itr.next());
                //    }
                //    m.put(String.valueOf(i), h);
                //}
                //m.values().removeIf(Set::isEmpty);
                //return m;
            };
            case 3 -> pl -> {
                //var m = new HashMap<String, Set<ServerPlayerEntity>>((int) (teams * 0.75F) + 1, 0.75F);
                int t = pl.size(); // total size
                return mkMapSplitRemainders(pl, teams, t / teams, t % teams);
                //int l = t / teams; // size of list
                //int r = t % teams; // remaining to split
                //var itr = pl.iterator();
                //for(int i = 0, k = 0; i < teams && itr.hasNext(); i++) {
                //    var h = new HashSet<ServerPlayerEntity>((int)(l * 0.75F) + 1, 0.75F);
                //    for(int j = 0; j < l; j++) {
                //        h.add(itr.next());
                //    }
                //    m.put(String.valueOf(i), h);
                //}
                //if(r != 0) {
                //    var h = new HashSet<ServerPlayerEntity>((int) (r * 0.75F) + 1, 0.75F);
                //    while (itr.hasNext()) {
                //        h.add(itr.next());
                //    }
                //    // null == spectator
                //    m.put(null, h);
                //}
                //m.values().removeIf(Set::isEmpty);
                //return m;
            };
            default -> null;
        };
    }
}
