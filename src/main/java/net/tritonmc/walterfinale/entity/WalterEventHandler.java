package net.tritonmc.walterfinale.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.tritonmc.walterfinale.WalterFinale;
import net.tritonmc.walterfinale.mixin.MobGoalsAccessor;

import java.util.Optional;
import java.util.function.BiConsumer;

public class WalterEventHandler implements ServerEntityEvents.Load {


    public WalterEventHandler()
    {
        ServerEntityEvents.ENTITY_LOAD.register(this);
    }

    @Override
    public void onLoad(Entity entity, ServerLevel level)
    {
        if (!entity.entityTags().contains(WalterFinale.MOD_ID) || !(entity instanceof Mob mob))
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
