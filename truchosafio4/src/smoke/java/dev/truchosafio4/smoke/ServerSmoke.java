package dev.truchosafio4.smoke;

import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.entity.*;
import dev.truchosafio4.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import java.nio.file.*;

/** Runs on a real dedicated Fabric server. Not packaged into the released mod. */
public final class ServerSmoke implements ModInitializer {
    private static int assertions;
    private static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    @Override public void onInitialize(){
        if(!Boolean.getBoolean("truchosafio4.smoke"))return;
        ServerLifecycleEvents.SERVER_STARTED.register(server->{
            try{run(server);Files.writeString(Path.of("SMOKE_PASS.txt"),"PASS: "+assertions+" dedicated-server assertions\n");}
            catch(Throwable failure){failure.printStackTrace();try{Files.writeString(Path.of("SMOKE_FAIL.txt"),failure.toString());}catch(Exception ignored){}}
            finally{server.stop(false);}
        });
    }
    private static void run(MinecraftServer server)throws Exception {
        ServerWorld world=server.getOverworld();
        var source=server.getCommandSource().withWorld(world).withPosition(new Vec3d(0,100,0));
        check(!ModConfig.ACTIVE.natural_spawning_enabled,"natural spawn default disabled");
        for(String kind:ModConfig.KINDS)check(!ModConfig.ACTIVE.mobs.get(kind).natural_spawn_enabled,kind+" natural spawn disabled");
        for(String kind:new String[]{"flashbang","rojo","negro","amarillo","blanco","qumara","raptor"}){
            check(server.getCommandManager().getDispatcher().execute("truchosafio spawn "+kind,source)==1,"spawn command "+kind);
        }
        for(String kind:new String[]{"flashbang","rojo","negro","amarillo","blanco","qumara","raptor","mecanismo"}){
            var entity=ModEntities.of(kind).create(world);check(entity!=null,"instantiate "+kind);
            NbtCompound saved=new NbtCompound();entity.writeNbt(saved);
            var copy=ModEntities.of(kind).create(world);copy.readNbt(saved);
            check(copy.getType()==entity.getType(),"NBT reload "+kind);
        }
        QumaraEntity boss=ModEntities.QUMARA.create(world);boss.refreshPositionAndAngles(30,100,30,0,0);world.spawnEntity(boss);
        check(boss.getMaxHealth()==1000,"configured boss health");check(boss.remainingMechanisms()==0,"initial shield");
        check(boss.cast(3),"activate shield");check(boss.remainingMechanisms()==14,"exactly 14 mechanisms");
        float health=boss.getHealth();check(!boss.damage(world.getDamageSources().generic(),10),"shield blocks damage");check(boss.getHealth()==health,"shield preserves health");
        NbtCompound nbt=new NbtCompound();boss.writeNbt(nbt);var reloaded=ModEntities.QUMARA.create(world);reloaded.readNbt(nbt);
        check(reloaded.remainingMechanisms()==14,"mechanisms survive NBT serialization");
        int destroyed=0;
        for(var e:world.iterateEntities())if(e instanceof QumaraMechanismEntity mechanism && boss.getUuid().equals(mechanism.owner())){
            mechanism.damage(world.getDamageSources().genericKill(),10000);destroyed++;
        }
        check(destroyed==14,"14 actual mechanisms destroyed");check(boss.remainingMechanisms()==0,"all deaths reported to owner");
        check(boss.damage(world.getDamageSources().generic(),10),"boss vulnerable after shield breaks");
        check(boss.getHealth()<health,"damage changes health after shield");
        check(!boss.cast(3),"broken shield cannot be reactivated");
        boss.setAura(false);NbtCompound aura=new NbtCompound();boss.writeNbt(aura);reloaded.readNbt(aura);
        check(!reloaded.isAuraOn(),"aura persists");check(reloaded.remainingMechanisms()==0,"broken phase persists");
        check(boss.cast(4),"raptor ability accepted");check(!boss.cast(4),"raptor cooldown enforced");
        for(int i=0;i<36;i++)boss.tick();
        int raptors=0;for(var e:world.iterateEntities())if(e instanceof RaptorEntity)raptors++;
        check(raptors>=4,"raptors actually summoned into world (plus command spawn)");
        check(server.getCommandManager().getDispatcher().execute("truchosafio natural flashbang nether on",source)==1,"enable Nether spawning");
        check(ModConfig.ACTIVE.mobs.get("flashbang").allows("minecraft:the_nether","minecraft:nether_wastes"),"Nether rule active");
        check(!ModConfig.ACTIVE.mobs.get("flashbang").allows("minecraft:overworld","minecraft:plains"),"Overworld remains disabled");
        check(server.getCommandManager().getDispatcher().execute("truchosafio natural flashbang nether off",source)==1,"disable Nether spawning");
        check(!ModConfig.ACTIVE.mobs.get("flashbang").natural_spawn_enabled,"all species dimensions disabled again");
        check(server.getCommandManager().getDispatcher().execute("truchosafio reload",source)==1,"JSON reload command");
        ModConfig.ACTIVE.natural_spawning_enabled=false;ModConfig.save();
        System.out.println("PASS: "+assertions+" dedicated Fabric server assertions (real entities, commands, shield, NBT, raptors, config)");
    }
}
