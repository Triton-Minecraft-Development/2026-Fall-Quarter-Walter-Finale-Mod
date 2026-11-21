package net.tritonmc.walterfinale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Manager for marking where mobs will pathfind towards. Mobs will pathfind towards the position.
 * <p>
 *     The position can be set with the following command in-game:
 * </p>
 * <code>/summon armor_stand ~ ~ ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,Marker:1b,Invisible:1b,Tags:["wave_marker"]}</code>
 * <p>
 *     An in-game command is used for this rather than an in-mod one because a datapack is being used to spawn waves.
 *     Exposing a unique armor marker tagged specifically for in-game command access while having an accessible internal
 *     position is the best way to go about this without completely overwriting the datapack.
 * </p>
 * <p>
 *     NOTE: Killing, removing, or discarding of the marked armor marker in any way will remove the marked position for mobs to
 *     pathfind towards until a new one is placed
 * </p>
 */
public class WaveMarkerManager {

    public static final String MARKER_ID = "wave_marker";

    public static boolean hasTag(Marker marker)
    {
        return marker.entityTags().contains(MARKER_ID);
    }

    public static void setWavePosition(Vec3 position, ServerLevel level)
    {
        Marker marker = EntityTypes.MARKER.create(level, EntitySpawnReason.COMMAND);
        if (marker == null)
        {
            return;
        }
        marker.setPos(position);
        level.addFreshEntity(marker);
        marker.addTag(MARKER_ID);
    }

    public static Optional<Vec3> getWavePosition(ServerLevel level)
    {
        List<? extends Marker> markers = level.getEntities(EntityTypes.MARKER, WaveMarkerManager::hasTag);
        markers.sort(Comparator.comparingInt(Entity::getId).reversed());
        if (markers.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(markers.getFirst().position());
    }

    public static Optional<BlockPos> getWavePositionAsBlockPos(ServerLevel level)
    {
        Optional<Vec3> posOpt = getWavePosition(level);
        if (posOpt.isEmpty())
        {
            return Optional.empty();
        }
        Vec3 pos = posOpt.get();
        return Optional.of(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
    }
}
