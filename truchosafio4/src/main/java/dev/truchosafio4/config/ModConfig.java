package dev.truchosafio4.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mutable hot-reload config. No world or dimension is added by this class. */
public final class ModConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static ModConfig ACTIVE;
    public int schema_version = 1;
    public boolean natural_spawning_enabled = false;
    public int spawn_check_interval_ticks = 120;
    public int min_distance_from_players = 26;
    public int max_distance_from_players = 65;
    public int max_per_dimension = 26;
    public int max_per_player = 6;
    public int protect_world_spawn_radius = 100;
    public Map<String, MobRule> mobs = new LinkedHashMap<>();
    public BossRules qumara = new BossRules();
    public static final String[] KINDS = {"flashbang", "rojo", "negro", "amarillo", "blanco"};
    public static final class MobRule {
        public boolean enabled = true;
        public boolean natural_spawn_enabled = false;
        public List<String> dimension_whitelist = List.of("minecraft:overworld");
        public Map<String, Boolean> dimension_enabled = new LinkedHashMap<>();
        public List<String> biomes = List.of();
        public int spawn_weight = 8;
        public double spawn_chance = 0.18;
        public int min_group_size = 1;
        public int max_group_size = 1;
        public int max_alive_per_dimension = 5;
        public boolean require_darkness = true;
        public int min_y = -32;
        public int max_y = 240;
        public boolean allows(String dimension, String biome) {
            return enabled && natural_spawn_enabled && dimension_whitelist != null
                && dimension_whitelist.contains(dimension)
                && dimension_enabled != null && Boolean.TRUE.equals(dimension_enabled.get(dimension))
                && (biomes == null || biomes.isEmpty() || biomes.contains(biome));
        }
    }
    public static final class BossRules {
        public boolean natural_spawn_enabled = false;
        public int max_health = 1000;
        public boolean allow_admin_control = true;
        public boolean poison_aura_enabled = true;
        public int mechanisms = 14;
        public boolean summon_raptors = true;
    }
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("truchosafio4.json"); }
    public static void load() {
        try {
            Path path = file();
            if (Files.notExists(path)) {
                // Fill the initial defaults with safe, disabled natural spawning.
                ACTIVE = defaults(); save(); return;
            }
            ModConfig parsed = GSON.fromJson(Files.readString(path), ModConfig.class);
            if (parsed == null || parsed.mobs == null || parsed.qumara == null)
                throw new IllegalArgumentException("La configuracion esta incompleta");
            for (String key : KINDS) parsed.mobs.putIfAbsent(key, defaults().mobs.get(key));
            parsed.validate();
            ACTIVE = parsed;
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Error leyendo config/truchosafio4.json: " + e.getMessage(), e);
        }
    }
    public void validate() {
        if(spawn_check_interval_ticks<20 || spawn_check_interval_ticks>72000 || min_distance_from_players<12 ||
            max_distance_from_players<=min_distance_from_players || max_distance_from_players>128 ||
            max_per_dimension<1 || max_per_dimension>256 || max_per_player<1 || max_per_player>64 ||
            protect_world_spawn_radius<0 || protect_world_spawn_radius>100000)
            throw new IllegalArgumentException("Limites de spawn invalidos");
        if(qumara==null || qumara.max_health<1 || qumara.max_health>1024 || qumara.mechanisms!=14)
            throw new IllegalArgumentException("Qumara requiere vida positiva y exactamente 14 mecanismos");
        for(String kind : KINDS) {
            MobRule r=mobs.get(kind);
            if(r==null || r.dimension_whitelist==null || r.dimension_enabled==null ||
                r.dimension_enabled.containsValue(null) || r.min_group_size<1 || r.max_group_size<r.min_group_size ||
                r.max_group_size>32 || r.max_alive_per_dimension<1 || r.max_alive_per_dimension>256 ||
                r.spawn_weight<0 || r.spawn_weight>10000 || !Double.isFinite(r.spawn_chance) || r.spawn_chance<0 ||
                r.spawn_chance>1 || r.min_y>r.max_y)
                throw new IllegalArgumentException("Regla de criatura invalida: "+kind);
        }
    }
    public static ModConfig defaults() {
        ModConfig c = new ModConfig();
        for (String name : KINDS) {
            MobRule rule = new MobRule();
            rule.dimension_enabled.put("minecraft:overworld", false);
            rule.dimension_enabled.put("minecraft:the_nether", false);
            rule.spawn_weight = switch (name) {case "negro" -> 2; case "blanco" -> 23; default -> 8;};
            rule.max_group_size = "blanco".equals(name) ? 5 : 1;
            rule.max_alive_per_dimension = "blanco".equals(name) ? 12 : 4;
            c.mobs.put(name, rule);
        }
        return c;
    }
    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(ACTIVE));
        } catch (IOException e) { throw new IllegalStateException("No se pudo guardar config", e); }
    }
}
