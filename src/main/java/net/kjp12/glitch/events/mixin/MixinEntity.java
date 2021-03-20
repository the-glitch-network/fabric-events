package net.kjp12.glitch.events.mixin;// Created 2021-03-18T08:05:30

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author KJP12
 * @since 0.0.0
 */
@Mixin(Entity.class)
public interface MixinEntity {
    @Accessor
    void setInvulnerable(boolean invulnerable);
}
