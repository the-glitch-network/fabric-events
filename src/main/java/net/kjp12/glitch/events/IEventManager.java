package net.kjp12.glitch.events;// Created 2021-03-17T21:33:54

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author KJP12
 * @since 0.0.0
 */
public interface IEventManager {
    void onActivate();

    void onEnd();

    boolean hasStarted();

    void tick();

    void onDeath(ServerPlayerEntity player);
}
