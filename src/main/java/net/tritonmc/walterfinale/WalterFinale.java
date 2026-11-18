package net.tritonmc.walterfinale;

import net.fabricmc.api.ModInitializer;
import net.tritonmc.walterfinale.entity.GoalRegistry;
import net.tritonmc.walterfinale.entity.WalterEventHandler;

public class WalterFinale implements ModInitializer {

    public static final String MOD_ID = "walter_finale";

    @Override
    public void onInitialize()
    {
        GoalRegistry.init();
        new WalterEventHandler();
    }
}
