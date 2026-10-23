package dev.truchosafio4.system;
import dev.truchosafio4.Truchosafio4;
import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import java.util.*;

/** Fully dynamic dimension/biome-based spawn. NOT registered as vanilla biome spawn to avoid duplicates. */
public final class SpawnManager {
    public static void tick(MinecraftServer server){
        ModConfig config=ModConfig.ACTIVE;
        if(config==null||!config.natural_spawning_enabled)return;
        int interval=Math.max(20,config.spawn_check_interval_ticks);
        if(server.getTicks()%interval!=0)return;
        for(ServerWorld world:server.getWorlds()){
            if(world.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL ||
                !world.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_MOB_SPAWNING))continue;
            String dimension=world.getRegistryKey().getValue().toString();
            for(ServerPlayerEntity player:world.getPlayers()){
                if(player.isCreative()||player.isSpectator())continue;
                attempt(world,player,dimension,config);
            }
        }
    }
    private static void attempt(ServerWorld world,ServerPlayerEntity player,String dim,ModConfig c){
        Random random=new Random(world.getRandom().nextLong());
        int min=Math.max(12,c.min_distance_from_players);
        int max=Math.max(min+3,c.max_distance_from_players);
        double angle=random.nextDouble()*Math.PI*2;
        double distance=min+random.nextDouble()*(max-min);
        BlockPos pos=BlockPos.ofFloored(player.getX()+Math.cos(angle)*distance,
            player.getY()+random.nextInt(17)-8,player.getZ()+Math.sin(angle)*distance);
        if(!world.isChunkLoaded(pos) || !world.getWorldBorder().contains(pos))return;
        if(world.getPlayers().stream().anyMatch(p -> !p.isSpectator() && p.squaredDistanceTo(Vec3d.ofCenter(pos)) < min*(double)min))return;
        if(!world.isAir(pos)||!world.isAir(pos.up())||!world.isAir(pos.up(2)))return;
        if("minecraft:overworld".equals(dim) && pos.getSquaredDistance(world.getSpawnPos())<c.protect_world_spawn_radius*c.protect_world_spawn_radius)return;
        String biome=world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.BIOME)
            .getId(world.getBiome(pos).value()).toString();
        List<String> types=new ArrayList<>();int total=0;
        for(String type:ModConfig.KINDS){
            ModConfig.MobRule rule=c.mobs.get(type);
            if(rule!=null && rule.allows(dim,biome) && pos.getY()>=rule.min_y && pos.getY()<=rule.max_y
                && (!rule.require_darkness || world.getLightLevel(pos)<=7)){
                types.add(type);total+=Math.max(0,rule.spawn_weight);
            }
        }
        if(total<1)return;
        int roll=random.nextInt(total),acc=0;String chosen=null;
        for(String type:types){acc+=Math.max(0,c.mobs.get(type).spawn_weight);if(roll<acc){chosen=type;break;}}
        if(chosen==null)return;
        ModConfig.MobRule rule=c.mobs.get(chosen);
        if(random.nextDouble()>Math.max(0,Math.min(1,rule.spawn_chance)))return;
        final EntityType<?> expected=ModEntities.of(chosen);
        int species=0, all=0, local=0;
        for(net.minecraft.entity.Entity entity : world.iterateEntities()) {
            if(!entity.isAlive())continue;
            boolean ours=entity.getType()==ModEntities.FLASHBANG || entity instanceof dev.truchosafio4.entity.AbstractGhostEntity;
            if(entity.getType()==expected)species++;
            if(ours){all++;if(entity.squaredDistanceTo(player)<=80*80)local++;}
        }
        int budget=Math.min(rule.max_alive_per_dimension-species,
            Math.min(c.max_per_dimension-all,c.max_per_player-local));
        if(budget<=0)return;
        int group=rule.min_group_size+random.nextInt(rule.max_group_size-rule.min_group_size+1);
        for(int i=0;i<Math.min(group,budget);i++){
            if(!(expected.create(world) instanceof HostileEntity mob))continue;
            BlockPos at=pos.add(i*2,0,0);
            if(expected==ModEntities.FLASHBANG){
                // Ground creature: search down without loading chunks or placing it in solid blocks.
                for(int down=0;down<12 && world.isAir(at.down()) && at.getY()>world.getBottomY()+1;down++)at=at.down();
                if(!world.getBlockState(at.down()).isSolidBlock(world,at.down()))continue;
            }
            if(!world.isChunkLoaded(at) || !world.getWorldBorder().contains(at))continue;
            final Vec3d center=Vec3d.ofCenter(at);
            if(world.getPlayers().stream().anyMatch(p -> !p.isSpectator() && p.squaredDistanceTo(center)<min*(double)min))continue;
            mob.refreshPositionAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,random.nextFloat()*360,0);
            if(!world.isSpaceEmpty(mob))continue;
            world.spawnEntity(mob);
        }
    }
}
