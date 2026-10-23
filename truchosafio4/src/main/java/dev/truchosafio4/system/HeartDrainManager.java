package dev.truchosafio4.system;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.truchosafio4.Truchosafio4;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.Identifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Stores penalty per UUID in the world save, reapplies after connect/respawn. */
public final class HeartDrainManager {
    private static final Identifier CURSE = Truchosafio4.id("corazon_robado");
    private static final Gson GSON = new Gson();
    private static Map<String,Integer> counts = new HashMap<>();
    private static final Map<UUID,Long> cooldowns = new HashMap<>();
    private static MinecraftServer server;
    private static Path path() {return server.getSavePath(WorldSavePath.ROOT).resolve("truchosafio4_hearts.json");}
    public static void init(MinecraftServer s) {
        server=s;counts=new HashMap<>();cooldowns.clear();
        try { if(Files.exists(path())) {
            Map<String,Integer> data=GSON.fromJson(Files.readString(path()),new TypeToken<Map<String,Integer>>(){}.getType());
            if(data!=null) data.forEach((uuid,value)->{
                UUID.fromString(uuid);
                if(value==null || value<0 || value>7)throw new IllegalArgumentException("Penalizacion invalida");
                counts.put(uuid,value);
            });
        }} catch(Exception e){throw new IllegalStateException("No se pudo leer la penalizacion de corazones; se conserva el archivo sin sobrescribir",e);}
    }
    private static void save(){
        if(server==null)throw new IllegalStateException("Servidor no inicializado");
        try{
            Path temporary=path().resolveSibling("truchosafio4_hearts.json.tmp");
            Files.writeString(temporary,GSON.toJson(counts));
            try{Files.move(temporary,path(),java.nio.file.StandardCopyOption.ATOMIC_MOVE,java.nio.file.StandardCopyOption.REPLACE_EXISTING);}
            catch(java.nio.file.AtomicMoveNotSupportedException e){Files.move(temporary,path(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);}
        }catch(Exception e){throw new IllegalStateException("No se pudo guardar los corazones",e);}
    }
    public static void apply(ServerPlayerEntity p) {
        EntityAttributeInstance hp=p.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);if(hp==null)return;
        int stolen=Math.min(7,Math.max(0,counts.getOrDefault(p.getUuidAsString(),0)));
        hp.removeModifier(CURSE);
        if(stolen>0)hp.addPersistentModifier(new EntityAttributeModifier(CURSE, -2.0*stolen, EntityAttributeModifier.Operation.ADD_VALUE));
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
    }
    public static boolean drain(ServerPlayerEntity p){
        if(p.isCreative()||p.isSpectator()||server==null)return false;
        long now=server.getTicks(); if(now < cooldowns.getOrDefault(p.getUuid(),-1L))return false;
        int count=counts.getOrDefault(p.getUuidAsString(),0);if(count>=7)return false;
        counts.put(p.getUuidAsString(),count+1);cooldowns.put(p.getUuid(),now+700);
        apply(p);save();return true;
    }
    public static int get(ServerPlayerEntity p){return counts.getOrDefault(p.getUuidAsString(),0);}
    public static void restore(ServerPlayerEntity p,int hearts){counts.put(p.getUuidAsString(),Math.max(0,get(p)-Math.max(0,hearts)));apply(p);save();}
}
