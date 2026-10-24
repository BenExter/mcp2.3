package dev.truchosafio4.registry;
import dev.truchosafio4.Truchosafio4;
import dev.truchosafio4.entity.FlashbangEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
public final class ModEntities {
    public static final EntityType<FlashbangEntity> FLASHBANG = Registry.register(Registries.ENTITY_TYPE,
        Truchosafio4.id("flashbang"),EntityType.Builder.create(FlashbangEntity::new,SpawnGroup.MONSTER)
            .dimensions(.6f,3.45f).maxTrackingRange(12).build("flashbang"));
    public static void registerAttributes(){FabricDefaultAttributeRegistry.register(FLASHBANG,FlashbangEntity.attributes());}
    public static EntityType<?> of(String kind){return "flashbang".equals(kind)?FLASHBANG:null;}
}
