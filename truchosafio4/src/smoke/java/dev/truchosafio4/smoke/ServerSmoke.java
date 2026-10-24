package dev.truchosafio4.smoke;

import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Collectors;

/** Explicitly enabled tests on a dedicated Fabric server; absent from the distributable mod. */
public final class ServerSmoke implements ModInitializer {
    private static int assertions;
    private static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    @Override public void onInitialize(){
        if(!Boolean.getBoolean("truchosafio4.smoke"))return;
        ServerLifecycleEvents.SERVER_STARTED.register(server->{
            try{run(server);Files.writeString(Path.of("SMOKE_PASS.txt"),"PASS: "+assertions+" Flashbang dedicated-server assertions\n");}
            catch(Throwable failure){failure.printStackTrace();try{Files.writeString(Path.of("SMOKE_FAIL.txt"),failure.toString());}catch(Exception ignored){}}
            finally{server.stop(false);}
        });
    }
    private static void run(MinecraftServer server)throws Exception {
        var world=server.getOverworld();
        var source=server.getCommandSource().withWorld(world).withPosition(Vec3d.ofCenter(world.getSpawnPos()).add(0,5,0));
        var entities=Registries.ENTITY_TYPE.getIds().stream().filter(id->id.getNamespace().equals("truchosafio4")).map(Object::toString).collect(Collectors.toSet());
        check(entities.equals(Set.of("truchosafio4:flashbang")),"only Flashbang entity registered");
        var items=Registries.ITEM.getIds().stream().filter(id->id.getNamespace().equals("truchosafio4")).map(Object::toString).collect(Collectors.toSet());
        check(items.equals(Set.of("truchosafio4:flashbang_spawn_egg")),"only Flashbang egg registered");
        var dispatcher=server.getCommandManager().getDispatcher();
        check(dispatcher.getRoot().getChild("qumara")==null,"no legacy boss command");
        var root=dispatcher.getRoot().getChild("truchosafio");
        check(root.getChild("hearts")==null && root.getChild("qumara")==null,"no removed subsystem commands");
        check(root.getChild("spawn").getChildren().size()==1,"only one summon subcommand");
        check(!ModConfig.ACTIVE.natural_spawning_enabled,"natural spawning initially off");
        check(ModConfig.ACTIVE.mobs.keySet().equals(Set.of("flashbang")),"only Flashbang config");
        check(!ModConfig.ACTIVE.mobs.get("flashbang").natural_spawn_enabled,"Flashbang natural spawning off");
        check(dispatcher.execute("truchosafio spawn flashbang",source)==1,"manual summon with defaults off");
        check(dispatcher.execute("truchosafio spawn flashbang minecraft:the_nether 0 80 0",source)==1,"manual summon in Nether");
        var flashbang=ModEntities.FLASHBANG.create(world);
        check(flashbang!=null,"create real Flashbang entity");
        check(flashbang.getMaxHealth()==20,"preserved health");
        NbtCompound saved=new NbtCompound();flashbang.writeNbt(saved);
        var restored=ModEntities.FLASHBANG.create(world);restored.readNbt(saved);
        check(restored.getType()==ModEntities.FLASHBANG,"NBT reload");
        check(dispatcher.execute("truchosafio natural flashbang nether on",source)==1,"enable Nether spawn");
        check(ModConfig.ACTIVE.mobs.get("flashbang").allows("minecraft:the_nether","minecraft:nether_wastes"),"Nether enabled");
        check(!ModConfig.ACTIVE.mobs.get("flashbang").allows("minecraft:overworld","minecraft:plains"),"Overworld still disabled");
        check(dispatcher.execute("truchosafio natural flashbang nether off",source)==1,"disable Nether spawn");
        check(!ModConfig.ACTIVE.mobs.get("flashbang").natural_spawn_enabled,"all dimensions off again");
        check(dispatcher.execute("truchosafio reload",source)==1,"reload JSON");
        ModConfig.ACTIVE.natural_spawning_enabled=false;ModConfig.save();
        var config=Files.readString(Path.of("config/truchosafio4.json"));
        check(!config.contains("qumara") && !config.contains("rojo"),"saved JSON contains only Flashbang");
        System.out.println("PASS: "+assertions+" Flashbang dedicated-server assertions");
    }
}
