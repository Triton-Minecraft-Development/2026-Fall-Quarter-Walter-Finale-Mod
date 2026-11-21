package net.tritonmc.walterfinale.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;
import net.tritonmc.walterfinale.entity.WaveMarkerManager;

import java.text.DecimalFormat;
import java.util.Optional;

public class WaveMarkerCommand implements CommandRegistrationCallback {

    private static final String NAME = "walter_finale_wave_pos";
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private static String posString(Vec3 pos)
    {
        return "x: " + FORMAT.format(pos.x) + " y: " + FORMAT.format(pos.y) + " z: " + FORMAT.format(pos.z);
    }

    public WaveMarkerCommand()
    {
        CommandRegistrationCallback.EVENT.register(this);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection)
    {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal(NAME)
                        .requires(sourceStack -> sourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(
                                LiteralArgumentBuilder.<CommandSourceStack>literal("get")
                                        .executes(context ->
                                        {
                                            CommandSourceStack sourceStack = context.getSource();
                                            ServerLevel level = sourceStack.getLevel();
                                            Optional<Vec3> posOpt = WaveMarkerManager.getWavePosition(level);
                                            if (posOpt.isEmpty())
                                            {
                                                sourceStack.sendSystemMessage(Component.literal(ChatFormatting.RED + "No wave position has been set in dimension '" + level.dimension().identifier() + "'"));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            sourceStack.sendSystemMessage(Component.literal(ChatFormatting.WHITE + "Wave position is at " + ChatFormatting.AQUA + posString(posOpt.get())));
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
                        .then(
                                LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                                        .executes(context ->
                                        {
                                            CommandSourceStack sourceStack = context.getSource();
                                            ServerLevel level = sourceStack.getLevel();
                                            if (!sourceStack.isPlayer() || !(sourceStack.getEntity() instanceof ServerPlayer player))
                                            {
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            Vec3 newPos = player.position();
                                            WaveMarkerManager.setWavePosition(newPos, level);
                                            sourceStack.sendSystemMessage(Component.literal(ChatFormatting.WHITE + "Set wave position to " + ChatFormatting.AQUA + posString(newPos)));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(
                                                Commands.argument("position", Vec3Argument.vec3())
                                                        .executes(context ->
                                                        {
                                                            Vec3 pos = Vec3Argument.getVec3(context, "position");
                                                            WaveMarkerManager.setWavePosition(pos, context.getSource().getLevel());
                                                            context.getSource().sendSystemMessage(Component.literal(ChatFormatting.WHITE + "Set wave position to " + ChatFormatting.AQUA + posString(pos)));
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                        )
                        )
                        .then(
                                LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                                        .executes(context ->
                                        {
                                            CommandSourceStack sourceStack = context.getSource();
                                            ServerLevel level = sourceStack.getLevel();
                                            if (!sourceStack.isPlayer() || !(sourceStack.getEntity() instanceof ServerPlayer player))
                                            {
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            Optional<Vec3> posOpt = WaveMarkerManager.getWavePosition(level);
                                            if (posOpt.isEmpty())
                                            {
                                                sourceStack.sendSystemMessage(Component.literal(ChatFormatting.RED + "No wave position has been set in dimension '" + level.dimension().identifier()));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            Vec3 pos = posOpt.get();
                                            player.teleportTo(pos.x, pos.y, pos.z);
                                            sourceStack.sendSystemMessage(Component.literal(ChatFormatting.WHITE + "Teleported to wave position at " + ChatFormatting.AQUA + posString(pos)));
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
                        .then(
                                LiteralArgumentBuilder.<CommandSourceStack>literal("clear")
                                        .executes(context ->
                                        {
                                            CommandSourceStack sourceStack = context.getSource();
                                            sourceStack.getLevel().getEntities(EntityTypes.MARKER, WaveMarkerManager::hasTag).forEach(marker ->
                                            {
                                                marker.remove(Entity.RemovalReason.DISCARDED);
                                            });
                                            sourceStack.sendSystemMessage(Component.literal(ChatFormatting.WHITE + "Wave marker position cleared for dimension '" + sourceStack.getLevel().dimension().identifier() + "'"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
        );
    }
}
