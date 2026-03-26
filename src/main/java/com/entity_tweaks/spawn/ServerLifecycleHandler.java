package com.smartstreamlabs.entity_tweaks.spawn;

import com.smartstreamlabs.entity_tweaks.EntityTweaksMod;
import com.smartstreamlabs.entity_tweaks.config.EntitySpawnConfigManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@EventBusSubscriber(modid = EntityTweaksMod.MOD_ID)
public final class ServerLifecycleHandler {
    private ServerLifecycleHandler() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        EntitySpawnConfigManager.getInstance().synchronize(EntityConfigSyncHelper.collectNaturalSpawnEntityIds(event.getServer()));
        EntityTweaksMod.LOGGER.info("Synchronized natural spawn entity config at {}", EntitySpawnConfigManager.getInstance().getConfigPath());
    }
}
