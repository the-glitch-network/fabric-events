package net.kjp12.glitch.events.mixin;// Created 2021-03-17T19:27:30

import net.kjp12.glitch.events.commands.EventCommand;
import net.kjp12.glitch.events.commands.SpectateCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KJP12
 * @since 0.0.0
 */
@Mixin(CommandManager.class)
public abstract class MixinCommandManager extends CommandRegistry {

    @Inject(method = "<init>", at = @At("TAIL"))
    public void uhc$registerCommands(CallbackInfo cbi) {
        registerCommand(new EventCommand());
        registerCommand(new SpectateCommand());
    }
}
