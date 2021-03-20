package net.kjp12.glitch.events.mixin;// Created 2021-03-18T04:31:47

import net.kjp12.glitch.events.Main;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KJP12
 * @since 0.0.0
 */
@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/GameJoinS2CPacket"))
    private GameJoinS2CPacket events$onPlayerConnect(int id, LevelInfo.GameMode gameMode, boolean hardCore, int viewDistance, Difficulty difficulty, int maxPlayers, LevelGeneratorType levelGeneratorType, boolean reducedDebugInfo){
        if(Main.currentEventManager != null) {
            return new GameJoinS2CPacket(id, LevelInfo.GameMode.SPECTATOR, false, viewDistance, difficulty, maxPlayers, levelGeneratorType, true);
        } else {
            return new GameJoinS2CPacket(id, gameMode, hardCore, viewDistance, difficulty, maxPlayers, levelGeneratorType, reducedDebugInfo);
        }
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/text/Text;)V"))
    private void events$onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo cbi) {
        if(Main.currentEventManager != null) {
            player.sendMessage(new LiteralText("You have joined mid-game. You will be a spectator for the remainder of the game."));
        }
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void events$removePlayer(ServerPlayerEntity player, CallbackInfo cbi) {
        Main.spectators.remove(player);
    }
}
