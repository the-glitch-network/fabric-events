package net.kjp12.glitch.events.commands;// Created 2021-03-17T19:30:08

import net.kjp12.glitch.events.Main;
import net.kjp12.glitch.events.managers.UHCManager;
import net.kjp12.hachimitsu.utilities.IStringSpliterator;
import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.TimeUnit;

/**
 * @author KJP12
 * @since 0.0.0
 */
public class EventCommand extends AbstractCommand {
    @Override
    public String getCommandName() {
        return "event";
    }

    @Override
    public int getPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsageTranslationKey(CommandSource source) {
        return "???";
    }

    @Override
    public void execute(CommandSource source, String[] args) throws CommandException {
        var itr = IStringSpliterator.of(args);
        if(!itr.hasNext()) throw new IncorrectUsageException("event <uhc>");
        switch (itr.nextString()) {
            case "uhc" -> {
                var server = MinecraftServer.getServer();
                var overworld = server.worlds[0];
                var worldBorder = overworld.getWorldBorder();
                if(!itr.hasNext()) throw new IncorrectUsageException("event uhc start <time=1h> <teams=2> <start=1000> <end=start>\nevent uhc stop");
                switch(itr.nextString()) {
                    case "start" -> {
                        var duration = itr.hasNext() ? itr.nextDuration() : TimeUnit.HOURS.toMillis(1);
                        int teams, tmode;
                        if(itr.hasNext()) {
                            itr.next();
                            teams = (int) itr.tryParseLong(2, 10);
                            tmode = switch (itr.charAt(0)) {
                                default  -> 0; // split by n
                                case '^' -> 1; // split by n with remainder spectating
                                case '/' -> 2; // split into n teams
                                case '*' -> 3; // split into n teams with remainder spectating
                            };
                        } else {
                            teams = 1;
                            tmode = 0;
                        }
                        var start = itr.hasNext() ? itr.nextInt() : 1024;
                        var end = itr.hasNext() ? itr.nextInt() : 8;
                        Main.currentEventManager = new UHCManager(server, duration, Main.mkTeamSplitter(teams, tmode), start, end);
                        Main.currentEventManager.onActivate();
                    }
                    case "stop" -> {
                        worldBorder.setSize(worldBorder.getOldSize());
                    }
                    default -> throw new IncorrectUsageException("event uhc start <time=1h> <teams=2> <start=1000> <end=start>\nevent uhc stop");
                }
            }
            default -> throw new IncorrectUsageException("event <uhc>");
        }
    }
}
