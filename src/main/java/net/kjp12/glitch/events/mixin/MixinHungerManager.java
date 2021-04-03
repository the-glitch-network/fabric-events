/*
 * Copyright (c) 2021 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * */
package net.kjp12.glitch.events.mixin;// Created 2021-03-18T06:38:58

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author KJP12
 * @since 0.0.0
 */
@Mixin(HungerManager.class)
public interface MixinHungerManager {
    @Accessor
    void setFoodSaturationLevel(float saturationLevel);

    @Accessor
    void setFoodStarvationTimer(int timer);

    @Accessor
    void setExhaustion(float exhaustion);
}
