package com.smartstreamlabs.entity_tweaks.config;

import com.smartstreamlabs.entity_tweaks.EntityTweaksMod;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class EntitySpawnConfigManager {
    private static final EntitySpawnConfigManager INSTANCE = new EntitySpawnConfigManager();
    private static final String CONFIG_FILE_NAME = "entity_spawns.cfg";

    private final Path configPath = FMLPaths.CONFIGDIR.get().resolve(EntityTweaksMod.MOD_ID).resolve(CONFIG_FILE_NAME);
    private volatile Map<Identifier, Boolean> cachedRules = Map.of();

    private EntitySpawnConfigManager() {
    }

    public static EntitySpawnConfigManager getInstance() {
        return INSTANCE;
    }

    public synchronized void synchronize(Collection<Identifier> entityIds) {
        try {
            Files.createDirectories(this.configPath.getParent());

            ParsedConfig parsedConfig = parseExistingConfig();
            TreeMap<String, Boolean> mergedEntries = new TreeMap<>();

            parsedConfig.values().forEach((key, value) -> mergedEntries.put(key.toString(), value));
            entityIds.stream()
                    .map(Identifier::toString)
                    .sorted()
                    .forEach(id -> mergedEntries.putIfAbsent(id, Boolean.TRUE));

            writeConfig(mergedEntries);
            this.cachedRules = Collections.unmodifiableMap(mergedEntries.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> Identifier.parse(entry.getKey()),
                            Map.Entry::getValue,
                            (left, right) -> right,
                            LinkedHashMap::new
                    )));
        } catch (IOException exception) {
            EntityTweaksMod.LOGGER.error("Failed to synchronize entity spawn config at {}", this.configPath, exception);
        }
    }

    public boolean isNaturalSpawnEnabled(Identifier entityId) {
        return this.cachedRules.getOrDefault(entityId, Boolean.TRUE);
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    private ParsedConfig parseExistingConfig() throws IOException {
        if (Files.notExists(this.configPath)) {
            return new ParsedConfig(Map.of());
        }

        Map<Identifier, Boolean> values = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(this.configPath, StandardCharsets.UTF_8);

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String rawLine = lines.get(lineNumber);
            String trimmedLine = rawLine.trim();

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }

            int separatorIndex = trimmedLine.indexOf('=');
            if (separatorIndex < 0) {
                EntityTweaksMod.LOGGER.warn("Ignoring malformed entity config line {} in {}", lineNumber + 1, this.configPath);
                continue;
            }

            String rawKey = trimmedLine.substring(0, separatorIndex).trim();
            String rawValue = trimmedLine.substring(separatorIndex + 1).trim();
            Identifier resourceLocation = Identifier.tryParse(rawKey);

            if (resourceLocation == null) {
                EntityTweaksMod.LOGGER.warn("Ignoring invalid entity id '{}' in {}", rawKey, this.configPath);
                continue;
            }

            if (!isBooleanValue(rawValue)) {
                EntityTweaksMod.LOGGER.warn("Ignoring invalid boolean '{}' for entity '{}' in {}", rawValue, rawKey, this.configPath);
                continue;
            }

            values.put(resourceLocation, Boolean.parseBoolean(rawValue.toLowerCase(Locale.ROOT)));
        }

        return new ParsedConfig(values);
    }

    private void writeConfig(Map<String, Boolean> entries) throws IOException {
        List<String> output = new ArrayList<>();
        output.add("# Entity Tweaks natural spawn configuration");
        output.add("# Set an entry to false to disable natural world spawning for that entity.");
        output.add("# Spawn eggs, commands, and other manual spawn methods are not blocked by this file.");
        output.add("# New natural-spawn entity ids from vanilla or mods are appended automatically with a default of true.");
        output.add("");

        entries.forEach((entityId, enabled) -> output.add(entityId + " = " + enabled.toString().toLowerCase(Locale.ROOT)));
        Files.write(this.configPath, output, StandardCharsets.UTF_8);
    }

    private static boolean isBooleanValue(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private record ParsedConfig(Map<Identifier, Boolean> values) {
    }
}
