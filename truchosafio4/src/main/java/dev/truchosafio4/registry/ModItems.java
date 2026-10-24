package dev.truchosafio4.registry;
import dev.truchosafio4.Truchosafio4;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

public final class ModItems {
    public static final Item FLASHBANG_EGG = egg("flashbang_spawn_egg", ModEntities.FLASHBANG, 0x080808, 0xd4d3be);
    private static Item register(String id, Item item) { return Registry.register(Registries.ITEM, Truchosafio4.id(id), item); }
    private static Item egg(String id, net.minecraft.entity.EntityType<? extends net.minecraft.entity.mob.MobEntity> e, int a, int b) { return register(id,new SpawnEggItem(e,a,b,new Item.Settings())); }
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(FLASHBANG_EGG);
        });
    }
}
