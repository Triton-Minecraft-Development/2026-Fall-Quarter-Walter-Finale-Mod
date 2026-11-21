package net.tritonmc.walterfinale.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for increasing thrown snowball damage by snow golems
 */
@Mixin(Snowball.class)
public abstract class SnowballMixin {

    @Unique
    private static final float DAMAGE = 3f;

    @Unique
    private static final int FREEZE_TICK_DURATION = 200;

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onHitEntity(EntityHitResult hitResult, CallbackInfo ci)
    {
        Snowball snowball = (Snowball) (Object) this;
        if (!(snowball.level() instanceof ServerLevel level))
        {
            return;
        }
        if (!(snowball.getOwner() instanceof SnowGolem))
        {
            return;
        }
        ci.cancel();
        Entity hitEntity = hitResult.getEntity();
        if (hitEntity instanceof Blaze)
        {
            hitEntity.hurt(snowball.damageSources().thrown(snowball, snowball.getOwner()), 3f);
        } else if (!(hitEntity instanceof SnowGolem))
        {
            hitEntity.hurtServer(level, snowball.damageSources().thrown(snowball, snowball.getOwner()), DAMAGE);
            hitEntity.setTicksFrozen(FREEZE_TICK_DURATION);
        }
    }
}
