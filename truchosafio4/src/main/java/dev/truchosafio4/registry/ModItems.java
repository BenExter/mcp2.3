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
    public static final Item RED_EGG = egg("rojo_spawn_egg", ModEntities.RED, 0x440007, 0xf23f4b);
    public static final Item BLACK_EGG = egg("negro_spawn_egg", ModEntities.BLACK, 0x0c0a13, 0xb48fff);
    public static final Item YELLOW_EGG = egg("amarillo_spawn_egg", ModEntities.YELLOW, 0x6b4b00, 0xffe04b);
    public static final Item WHITE_EGG = egg("blanco_spawn_egg", ModEntities.WHITE, 0x7f8e99, 0xf6ffff);
    public static final Item QUMARA_EGG = egg("qumara_spawn_egg", ModEntities.QUMARA, 0x333e2b, 0x6de0cd);
    public static final Item ESSENCE_RED = register("esencia_roja", new Item(new Item.Settings()));
    public static final Item ESSENCE_BLACK = register("esencia_negra", new Item(new Item.Settings()));
    public static final Item ESSENCE_YELLOW = register("esencia_amarilla", new Item(new Item.Settings()));
    public static final Item ESSENCE_WHITE = register("esencia_blanca", new Item(new Item.Settings()));
    private static Item register(String id, Item item) { return Registry.register(Registries.ITEM, Truchosafio4.id(id), item); }
    private static Item egg(String id, net.minecraft.entity.EntityType<? extends net.minecraft.entity.mob.MobEntity> e, int a, int b) { return register(id,new SpawnEggItem(e,a,b,new Item.Settings())); }
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(FLASHBANG_EGG); entries.add(RED_EGG); entries.add(BLACK_EGG); entries.add(YELLOW_EGG); entries.add(WHITE_EGG); entries.add(QUMARA_EGG);
        });
    }
}
