package com.smartstreamlabs.entity_tweaks.spawn;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.util.random.Weighted;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

public final class EntityConfigSyncHelper {
    private static final Set<MobCategory> NATURAL_SPAWN_CATEGORIES = EnumSet.of(
            MobCategory.MONSTER,
            MobCategory.CREATURE,
            MobCategory.AMBIENT,
            MobCategory.AXOLOTLS,
            MobCategory.UNDERGROUND_WATER_CREATURE,
            MobCategory.WATER_AMBIENT,
            MobCategory.WATER_CREATURE
    );

    private EntityConfigSyncHelper() {
    }

    public static Set<Identifier> collectNaturalSpawnEntityIds(MinecraftServer server) {
        TreeSet<Identifier> entityIds = new TreeSet<>(Comparator.comparing(Identifier::toString));

        server.registryAccess().lookupOrThrow(Registries.BIOME).listElements().forEach(biomeHolder -> addBiomeSpawnEntries(entityIds, biomeHolder));
        server.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements().forEach(structureHolder -> addStructureSpawnEntries(entityIds, structureHolder));

        return entityIds;
    }

    private static void addBiomeSpawnEntries(Set<Identifier> entityIds, Holder.Reference<Biome> biomeHolder) {
        MobSpawnSettings mobSpawnSettings = biomeHolder.value().getMobSettings();

        NATURAL_SPAWN_CATEGORIES.forEach(category -> mobSpawnSettings.getMobs(category).unwrap().forEach(weightedSpawnerData -> {
            Identifier entityId = net.minecraft.world.entity.EntityType.getKey(weightedSpawnerData.value().type());
            if (entityId != null) {
                entityIds.add(entityId);
            }
        }));
    }

    private static void addStructureSpawnEntries(Set<Identifier> entityIds, Holder.Reference<Structure> structureHolder) {
        structureHolder.value().spawnOverrides().forEach((category, spawnOverride) -> {
            if (!NATURAL_SPAWN_CATEGORIES.contains(category)) {
                return;
            }

            addSpawnerEntries(entityIds, spawnOverride);
        });
    }

    private static void addSpawnerEntries(Set<Identifier> entityIds, StructureSpawnOverride spawnOverride) {
        spawnOverride.spawns().unwrap().forEach(weightedSpawnerData -> {
            Identifier entityId = net.minecraft.world.entity.EntityType.getKey(weightedSpawnerData.value().type());
            if (entityId != null) {
                entityIds.add(entityId);
            }
        });
    }
}
