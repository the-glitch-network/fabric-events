package net.kjp12.glitch.events.mixin;// Created 2021-03-18T07:34:23

import net.kjp12.glitch.events.Main;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KJP12
 * @since 0.0.0
 */
@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
    @Inject(method = "onKilled", at = @At("TAIL"))
    private void events$onKilled(CallbackInfo cbi) {
        if(Main.currentEventManager != null) {
            Main.currentEventManager.onDeath((ServerPlayerEntity)(Object)this);
        }
    }
}
