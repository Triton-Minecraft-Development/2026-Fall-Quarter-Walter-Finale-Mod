package net.tritonmc.walterfinale;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.tritonmc.walterfinale.command.WaveMarkerCommand;
import net.tritonmc.walterfinale.entity.GoalRegistry;
import net.tritonmc.walterfinale.entity.WalterEventHandler;

public class WalterFinale implements ModInitializer {

    public static final String MOD_ID = "walter_finale";

    public static Identifier of(String path)
    {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize()
    {
        GoalRegistry.init();
        new WalterEventHandler();
        new WaveMarkerCommand();
    }
}
