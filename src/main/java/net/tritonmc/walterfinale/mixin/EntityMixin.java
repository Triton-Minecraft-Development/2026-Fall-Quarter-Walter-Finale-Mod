package net.tritonmc.walterfinale.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.tritonmc.walterfinale.WalterFinale;
import net.tritonmc.walterfinale.entity.GoalRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.BiConsumer;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "addTag", at = @At("RETURN"))
    private void addTag(String tag, CallbackInfoReturnable<Boolean> cir)
    {
        Entity entity = (Entity) (Object) this;
        if (!entity.level().isClientSide())
        {
            return;
        }
        if (!cir.getReturnValue() || !tag.equals(WalterFinale.MOD_ID))
        {
            return;
        }

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
}
