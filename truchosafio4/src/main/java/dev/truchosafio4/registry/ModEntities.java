package dev.truchosafio4.registry;

import dev.truchosafio4.Truchosafio4;
import dev.truchosafio4.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntities {
    public static final EntityType<FlashbangEntity> FLASHBANG = register("flashbang", FlashbangEntity::new, 0.6f, 3.45f);
    public static final EntityType<RedGhostEntity> RED = register("rojo", RedGhostEntity::new, 0.8f, 1.8f);
    public static final EntityType<BlackGhostEntity> BLACK = register("negro", BlackGhostEntity::new, 0.9f, 2.1f);
    public static final EntityType<YellowGhostEntity> YELLOW = register("amarillo", YellowGhostEntity::new, 0.65f, 1.5f);
    public static final EntityType<WhiteGhostEntity> WHITE = register("blanco", WhiteGhostEntity::new, 0.65f, 1.3f);
    public static final EntityType<QumaraEntity> QUMARA = register("qumara", QumaraEntity::new, 4.8f, 7.0f);
    public static final EntityType<RaptorEntity> RAPTOR = register("raptor", RaptorEntity::new, 0.9f, 1.25f);
    public static final EntityType<QumaraMechanismEntity> MECHANISM = register("mecanismo", QumaraMechanismEntity::new, 0.7f, 0.7f);

    private static <T extends net.minecraft.entity.mob.HostileEntity> EntityType<T> register(String name, EntityType.EntityFactory<T> factory, float width, float height) {
        return Registry.register(Registries.ENTITY_TYPE, Truchosafio4.id(name),
            EntityType.Builder.create(factory, SpawnGroup.MONSTER).dimensions(width, height).maxTrackingRange(12).build(name));
    }
    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(FLASHBANG, FlashbangEntity.attributes());
        FabricDefaultAttributeRegistry.register(RED, RedGhostEntity.attributes());
        FabricDefaultAttributeRegistry.register(BLACK, BlackGhostEntity.attributes());
        FabricDefaultAttributeRegistry.register(YELLOW, YellowGhostEntity.attributes());
        FabricDefaultAttributeRegistry.register(WHITE, WhiteGhostEntity.attributes());
        FabricDefaultAttributeRegistry.register(QUMARA, QumaraEntity.attributes());
        FabricDefaultAttributeRegistry.register(RAPTOR, RaptorEntity.attributes());
        FabricDefaultAttributeRegistry.register(MECHANISM, QumaraMechanismEntity.attributes());
    }
    public static EntityType<?> of(String kind) {
        return switch(kind) {case "flashbang" -> FLASHBANG; case "rojo" -> RED; case "negro" -> BLACK;
            case "amarillo" -> YELLOW; case "blanco" -> WHITE; case "qumara" -> QUMARA; case "raptor" -> RAPTOR; case "mecanismo" -> MECHANISM; default -> null;};
    }
}
