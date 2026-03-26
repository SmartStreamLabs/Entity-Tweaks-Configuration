package com.smartstreamlabs.entity_tweaks.spawn;

import com.smartstreamlabs.entity_tweaks.EntityTweaksMod;
import com.smartstreamlabs.entity_tweaks.config.EntitySpawnConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = EntityTweaksMod.MOD_ID)
public final class NaturalSpawnHandler {
    private NaturalSpawnHandler() {
    }

    @SubscribeEvent
    public static void onPotentialSpawns(LevelEvent.PotentialSpawns event) {
        List<Weighted<MobSpawnSettings.SpawnerData>> disallowedSpawns = new ArrayList<>();

        for (Weighted<MobSpawnSettings.SpawnerData> weightedSpawnerData : event.getSpawnerDataList()) {
            Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(weightedSpawnerData.value().type());
            if (entityId != null && !EntitySpawnConfigManager.getInstance().isNaturalSpawnEnabled(entityId)) {
                disallowedSpawns.add(weightedSpawnerData);
            }
        }

        disallowedSpawns.forEach(event::removeSpawnerData);
    }
}
