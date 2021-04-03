/*
 * Copyright (c) 2021 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * */
package net.kjp12.glitch.events.commands;// Created 2021-03-17T19:30:08

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kjp12.glitch.events.Main;
import net.kjp12.glitch.events.TeamMode;
import net.kjp12.glitch.events.managers.UHCManager;
import net.kjp12.hachimitsu.utilities.StringUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author KJP12
 * @since 0.0.0
 */
public class EventCommand {
    public static Predicate<ServerCommandSource> REQUIRE_EVENT_PERMISSION = Permissions.require("the-glitch.event", 2);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var event = literal("event").requires(REQUIRE_EVENT_PERMISSION);
        var end0 = literal("end").executes(EventCommand::executeEnd);
        var start0 = literal("start");
        var uhc0 = literal("uhc").executes(EventCommand::executeUhcWithNoArgs);
        var uhc0s = argument("time", string()).then(argument("mode", integer(0, 3)).then(argument("teams", integer(1)).then(argument("start", integer(64, 30000000)).then(argument("end", integer(8, 30000000))).executes(EventCommand::executeUhcWithAllArgs))));
        dispatcher.register(event.then(start0.then(uhc0.then(uhc0s))).then(end0).then(literal("spectate").requires(src -> src.getEntity() instanceof ServerPlayerEntity).executes(EventCommand::executeSpectate)));
    }

    public static int executeUhcWithNoArgs(CommandContext<ServerCommandSource> ctx) {
        return executeUhcStart(ctx.getSource(), TimeUnit.HOURS.toMillis(1), null, 0, 2048, 16);
    }

    public static int executeUhcWithAllArgs(CommandContext<ServerCommandSource> ctx) {
        return executeUhcStart(ctx.getSource(), StringUtils.parseDuration(getString(ctx, "time")), TeamMode.values()[getInteger(ctx, "mode")], getInteger(ctx, "teams"), getInteger(ctx, "start"), getInteger(ctx, "end"));
    }

    public static int executeUhcStart(ServerCommandSource source, long duration, TeamMode mode, int teams, int start, int end) {
        if (Main.currentEventManager != null) {
            Main.currentEventManager.onEnd();
        }
        Main.currentEventManager = new UHCManager(source.getMinecraftServer(), duration, mode != null && teams > 0 ? mode.mkFunction(teams) : null, start, end);
        Main.currentEventManager.onActivate();
        return Command.SINGLE_SUCCESS;
    }

    public static int executeEnd(CommandContext<ServerCommandSource> source) {
        if (Main.currentEventManager != null) {
            Main.currentEventManager.onEnd();
            Main.currentEventManager = null;
            return Command.SINGLE_SUCCESS;
        } else return 0;
    }

    public static int executeSpectate(CommandContext<ServerCommandSource> ctx) {
        final var source = ctx.getSource();
        if (Main.currentEventManager != null && Main.currentEventManager.hasStarted()) {
            source.sendError(new LiteralText("There is an on-going event. You shall not escape."));
            return 0;
        } else {
            var entity = source.getEntity();
            assert entity instanceof ServerPlayerEntity : "You may not run this command from the console or as entities.";
            var player = (ServerPlayerEntity) entity;
            if (Main.spectators.add(player)) {
                source.sendFeedback(new LiteralText("You have opted in to being a spectator for the next event."), false);
            } else {
                Main.spectators.remove(player);
                source.sendFeedback(new LiteralText("You have opted out of being a spectator for the next event."), false);
            }
            return Command.SINGLE_SUCCESS;
        }
    }
}
