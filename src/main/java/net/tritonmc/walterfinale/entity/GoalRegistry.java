package net.tritonmc.walterfinale.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.tritonmc.walterfinale.mixin.MobGoalsAccessor;
import org.apache.commons.lang3.function.Predicates;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiConsumer;

public class GoalRegistry {

    private static final HashMap<EntityType<?>, BiConsumer<? extends Mob, MobGoalsAccessor>> REGISTRY = new HashMap<>();

    private static <T extends Mob> void register(EntityType<T> entityType, BiConsumer<T, MobGoalsAccessor> consumer)
    {
        REGISTRY.put(entityType, consumer);
    }

    public static <T extends Mob> void acceptMob(Mob mob, BiConsumer<T, MobGoalsAccessor> consumer)
    {
        @SuppressWarnings("unchecked")
        T typedMob = (T) mob;
        consumer.accept(typedMob, (MobGoalsAccessor) mob);
    }

    public static Optional<BiConsumer<? extends Mob, MobGoalsAccessor>> get(EntityType<?> entityType)
    {
        if (entityType == null)
        {
            return Optional.empty();
        }
        if (!REGISTRY.containsKey(entityType) || REGISTRY.get(entityType) == null)
        {
            return Optional.empty();
        }

        return Optional.of(REGISTRY.get(entityType));
    }

    public static void init()
    {
        register(EntityTypes.SNOW_GOLEM, (golem, mobGoalsAccessor) ->
        {
            GoalSelector goals = mobGoalsAccessor.goalSelector();
            GoalSelector targetGoals = mobGoalsAccessor.targetSelector();

            goals.removeAllGoals(Predicates.truePredicate());
            targetGoals.removeAllGoals(Predicates.truePredicate());

            goals.addGoal(1, new RangedAttackGoal(golem, 1.25, 10, 15f));
            goals.addGoal(2, new WaterAvoidingRandomStrollGoal(golem, 1.0, 1.0000001E-5f));
            goals.addGoal(3, new LookAtPlayerGoal(golem, ServerPlayer.class, 6.0f));
            goals.addGoal(4, new RandomLookAroundGoal(golem));

            targetGoals.addGoal(1, new HurtByTargetGoal(golem, SnowGolem.class).setAlertOthers(SnowGolem.class));
            targetGoals.addGoal(2, new NearestAttackableTargetGoal<>(golem, ServerPlayer.class, false));
        });

        register(EntityTypes.POLAR_BEAR, (polarBear, mobGoalsAccessor) ->
        {
            GoalSelector goalSelector = mobGoalsAccessor.goalSelector();
            GoalSelector targetGoals = mobGoalsAccessor.targetSelector();

            goalSelector.removeAllGoals(Predicates.truePredicate());
            targetGoals.removeAllGoals(Predicates.truePredicate());

            goalSelector.addGoal(0, new FloatGoal(polarBear));
            goalSelector.addGoal(1, polarBear.new PolarBearMeleeAttackGoal());
            goalSelector.addGoal(2, new LookAtPlayerGoal(polarBear, ServerPlayer.class, 6.0f));
            goalSelector.addGoal(3, new RandomLookAroundGoal(polarBear));

            targetGoals.addGoal(1, polarBear.new PolarBearHurtByTargetGoal());
            targetGoals.addGoal(2, new NearestAttackableTargetGoal<>(polarBear, ServerPlayer.class, false));
        });
    }
}
