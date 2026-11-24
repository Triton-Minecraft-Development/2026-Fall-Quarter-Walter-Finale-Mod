package net.tritonmc.walterfinale.entity.pathfind;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tritonmc.walterfinale.entity.WaveMarkerManager;

import java.util.Optional;

public class MarkerPathfinder extends Goal {

    public static final double DEFAULT_TARGET_DISTANCE_THRESHOLD = 20;
    public static final double DEFAULT_MAX_DISTANCE_FROM_MARKER = 100;

    private final Mob mob;
    private final double speedModifier;

    /**
     * The distance this mob's target must be within for the mob to
     * ignore pathfinding to the marker
     */
    private final double targetDistanceThreshold;

    /**
     * The max distance the mob can be from the marker before giving up on chasing a target
     */
    private final double maxDistanceFromMarker;
    private Path path;
    private int ticksUntilNextPathRecalculation;

    public MarkerPathfinder(Mob mob, double speedModifier)
    {
        this(mob, speedModifier, DEFAULT_TARGET_DISTANCE_THRESHOLD, DEFAULT_MAX_DISTANCE_FROM_MARKER);
    }

    public MarkerPathfinder(Mob mob, double speedModifier, double targetDistanceThreshold, double maxDistanceFromMarker)
    {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.targetDistanceThreshold = targetDistanceThreshold;
        this.maxDistanceFromMarker = maxDistanceFromMarker;
    }

    @Override
    public boolean canUse()
    {
        Level level = this.mob.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
        {
            return false;
        }
        Optional<Vec3> vec3Opt = WaveMarkerManager.getWavePosition(serverLevel);
        Optional<BlockPos> blockPosOpt = WaveMarkerManager.getWavePositionAsBlockPos(serverLevel);
        if (vec3Opt.isEmpty() || blockPosOpt.isEmpty())
        {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(blockPosOpt.get(), 0);
        Entity target = this.mob.getTarget();
        if (target == null)
        {
            return this.path != null; // No target. Follow path
        }
        if (this.shouldPrioritizeTarget())
        {
            return false; // Mob should prioritize current target over pathfinding to marker
        }
        if (this.mob.distanceTo(target) <= this.targetDistanceThreshold) // Close enough to target to ignore path
        {
            // Check if mob is too far away whilst chasing target
            // Do not allow players to guide mobs away
            if (this.maxDistanceFromMarker > -1)
            {
                return this.mob.position().distanceTo(vec3Opt.get()) >= this.maxDistanceFromMarker;
            }
        }
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse()
    {
        return this.mob.isAlive() && this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public void start()
    {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.ticksUntilNextPathRecalculation = 0;
    }

    @Override
    public void stop()
    {
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick()
    {
        Level level = this.mob.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
        {
            return;
        }
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        Optional<Vec3> targetPosOpt = WaveMarkerManager.getWavePosition(serverLevel);
        if (this.ticksUntilNextPathRecalculation == 0 && targetPosOpt.isPresent())
        {
            Vec3 targetPos = targetPosOpt.get();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            double distSqr = this.mob.distanceToSqr(targetPos);
            if (distSqr > 1024)
            {
                this.ticksUntilNextPathRecalculation += 10;
            } else if (distSqr > 256)
            {
                this.ticksUntilNextPathRecalculation += 5;
            }
            if (!this.mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, this.speedModifier))
            {
                this.ticksUntilNextPathRecalculation += 15;
            }
            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
        }
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
    }

    private boolean shouldPrioritizeTarget()
    {
        return this.targetDistanceThreshold <= -1 &&
                this.maxDistanceFromMarker <= -1 &&
                this.mob.getTarget() != null;
    }
}
