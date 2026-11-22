package net.tritonmc.walterfinale.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.tritonmc.walterfinale.entity.WaveMarkerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Unique
    private static boolean isValidRemovableMarker(Marker target, Marker toRemove)
    {
        return WaveMarkerManager.hasTag(target) && WaveMarkerManager.hasTag(toRemove) && !target.getUUID().equals(toRemove.getUUID());
    }

    @Inject(method = "addFreshEntity", at = @At("RETURN"))
    private void addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (!cir.getReturnValue())
        {
            return;
        }
        if (!(entity instanceof Marker marker))
        {
            return;
        }
        if (!entity.entityTags().contains(WaveMarkerManager.MARKER_ID))
        {
            return;
        }
        ServerLevel level = (ServerLevel) (Object) this;
        level.getEntities(EntityTypes.MARKER, marker1 -> isValidRemovableMarker(marker, marker1)).forEach(m ->
        {
            m.remove(Entity.RemovalReason.DISCARDED);
        });
    }
}
