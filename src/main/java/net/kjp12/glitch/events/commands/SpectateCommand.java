package net.kjp12.glitch.events.commands;// Created 2021-03-17T19:30:08

import net.kjp12.glitch.events.Main;
import net.kjp12.glitch.events.managers.UHCManager;
import net.kjp12.hachimitsu.utilities.IStringSpliterator;
import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.concurrent.TimeUnit;

/**
 * @author KJP12
 * @since 0.0.0
 */
public class SpectateCommand extends AbstractCommand {
    @Override
    public String getCommandName() {
        return "spectate";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public String getUsageTranslationKey(CommandSource source) {
        return "???";
    }

    @Override
    public void execute(CommandSource source, String[] args) throws CommandException {
        if(Main.currentEventManager != null && Main.currentEventManager.hasStarted())
            throw new IncorrectUsageException("There is an on-going event. You shall not escape.");
        var entity = source.getEntity();
        if(!(entity instanceof ServerPlayerEntity))
            throw new IncorrectUsageException("You may not run this command from the console or as entities.");
        var player = (ServerPlayerEntity) entity;
        if(Main.spectators.add(player)) {
            source.sendMessage(new LiteralText("You have opted in to being a spectator for the next event."));
        } else {
            Main.spectators.remove(player);
            source.sendMessage(new LiteralText("You have opted out of being a spectator for the next event."));
        }
    }
}
