package net.tritonmc.walterfinale.entity.pathfind;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tritonmc.walterfinale.entity.WaveMarkerManager;

import java.util.Optional;

public class MarkerPathfinder extends Goal {

    private final Mob mob;
    private final double speedModifier;
    private Path path;
    private int ticksUntilNextPathRecalculation;

    public MarkerPathfinder(Mob mob, double speedModifier)
    {
        this.mob = mob;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse()
    {
        Level level = this.mob.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
        {
            return false;
        }
        Optional<BlockPos> blockPosOpt = WaveMarkerManager.getWavePositionAsBlockPos(serverLevel);
        if (blockPosOpt.isEmpty())
        {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(blockPosOpt.get(), 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse()
    {
        return this.mob.isAlive();
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
}
