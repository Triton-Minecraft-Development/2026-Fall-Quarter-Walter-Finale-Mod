package net.tritonmc.walterfinale.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tritonmc.walterfinale.WalterFinale;
import net.tritonmc.walterfinale.entity.GoalRegistry;
import net.tritonmc.walterfinale.entity.WaveMarkerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique
    private boolean isValidRemovableMarker(Marker marker)
    {
        Entity entity = (Entity) (Object) this;
        return WaveMarkerManager.hasTag(marker) && !entity.getUUID().equals(marker.getUUID());
    }

    @Inject(method = "addTag", at = @At("RETURN"))
    private void addTag(String tag, CallbackInfoReturnable<Boolean> cir)
    {
        Entity entity = (Entity) (Object) this;
        Level level = entity.level();
        if (entity.level().isClientSide())
        {
            return;
        }
        if (!cir.getReturnValue())
        {
            return;
        }
        if (tag.equals(WalterFinale.MOD_ID))
        {
            if (!(entity instanceof Mob mob))
            {
                return;
            }
            Optional<BiConsumer<? extends Mob, MobGoalsAccessor>> op = GoalRegistry.get(mob.getType());
            if (op.isEmpty())
            {
                return;
            }
            GoalRegistry.acceptMob(mob, op.get());
        }
        else if (tag.equals(WaveMarkerManager.MARKER_ID) && level instanceof ServerLevel serverLevel)
        {
            // Armor stands marked with the "wave_marker" tag will mark the
            // position for wave mobs to converge in on.

            // All previous armor stands used for marking
            // wave positions will be overridden and removed

            serverLevel.getEntities(EntityTypes.MARKER, this::isValidRemovableMarker).forEach(marker ->
            {
                marker.remove(Entity.RemovalReason.DISCARDED);
            });

        }

    }

}
