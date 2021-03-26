package net.kjp12.glitch.events.mixin;// Created 2021-03-18T04:31:47

import net.kjp12.glitch.events.Main;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * @author KJP12
 * @since 0.0.0
 */
@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/GameJoinS2CPacket"))
    private GameJoinS2CPacket events$onPlayerConnect(int playerEntityId, GameMode gameMode, GameMode previousGameMode,
                                                     long sha256Seed, boolean hardcore, Set<RegistryKey<World>> dimensionIds,
                                                     DynamicRegistryManager.Impl registryManager, DimensionType dimensionType,
                                                     RegistryKey<World> dimensionId, int maxPlayers, int chunkLoadDistance,
                                                     boolean reducedDebugInfo, boolean showDeathScreen, boolean debugWorld,
                                                     boolean flatWorld) {
        if (Main.currentEventManager != null) {
            return new GameJoinS2CPacket(playerEntityId, GameMode.SPECTATOR, gameMode, sha256Seed, hardcore, dimensionIds,
                    registryManager, dimensionType, dimensionId, maxPlayers, chunkLoadDistance, reducedDebugInfo, showDeathScreen,
                    debugWorld, flatWorld);
        } else {
            return new GameJoinS2CPacket(playerEntityId, gameMode, previousGameMode, sha256Seed, hardcore, dimensionIds,
                    registryManager, dimensionType, dimensionId, maxPlayers, chunkLoadDistance, reducedDebugInfo, showDeathScreen,
                    debugWorld, flatWorld);
        }
    }

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void events$onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo cbi) {
        if(Main.currentEventManager != null) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(new LiteralText("You have joined mid-game. You will be a spectator for the remainder of the game."), true);
        }
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void events$removePlayer(ServerPlayerEntity player, CallbackInfo cbi) {
        Main.spectators.remove(player);
    }
}
