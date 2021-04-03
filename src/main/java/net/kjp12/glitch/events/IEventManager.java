/*
 * Copyright (c) 2021 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * */
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

    boolean mayRejoin(ServerPlayerEntity player);

    void tick();

    void onDeath(ServerPlayerEntity player);

    void onRemove(ServerPlayerEntity player);
}
