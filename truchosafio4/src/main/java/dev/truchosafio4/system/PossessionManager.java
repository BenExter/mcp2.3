package dev.truchosafio4.system;

import dev.truchosafio4.entity.YellowGhostEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import java.util.*;

/** Server-owned possession; no fake player attacks or interference with resurrection/PvP rules. */
public final class PossessionManager {
    private record Host(UUID player, UUID ghost, int jumps, long started, long expiration) {}
    private static final Map<UUID,Host> hosts = new HashMap<>();
    private static final Map<UUID,Long> immunity = new HashMap<>();
    public static boolean isPossessing(UUID ghost) { return hosts.containsKey(ghost); }
    private static boolean occupied(UUID player) { return hosts.values().stream().anyMatch(h -> h.player().equals(player)); }
    public static void clear() { hosts.clear(); immunity.clear(); }
    public static void stop(MinecraftServer server) {
        for(Host h : hosts.values()) { YellowGhostEntity ghost = find(server,h.ghost()); if(ghost!=null) release(ghost); }
        clear();
    }
    private static YellowGhostEntity find(MinecraftServer server, UUID id) {
        for(ServerWorld world : server.getWorlds()) if(world.getEntity(id) instanceof YellowGhostEntity g) return g;
        return null;
    }
    private static void release(YellowGhostEntity ghost) {
        ghost.setInvisible(false); ghost.setInvulnerable(false); ghost.noClip=false;
        ghost.setVelocity(Vec3d.ZERO); ghost.setTarget(null);
    }
    public static boolean possess(YellowGhostEntity ghost, ServerPlayerEntity player) {
        MinecraftServer server=player.getServer();
        if(server==null || hosts.containsKey(ghost.getUuid()) || occupied(player.getUuid()) ||
            immunity.getOrDefault(player.getUuid(),-1L)>server.getTicks() || player.isSpectator() || player.isCreative())return false;
        long now=server.getTicks();
        hosts.put(ghost.getUuid(),new Host(player.getUuid(),ghost.getUuid(),0,now,now+160));
        ghost.setInvisible(true); ghost.setInvulnerable(true); ghost.noClip=true; ghost.setVelocity(Vec3d.ZERO);
        mark(player); return true;
    }
    private static void mark(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,80,0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING,30,0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,30,1));
        player.getServerWorld().spawnParticles(ParticleTypes.WITCH,player.getX(),player.getY()+1,player.getZ(),12,.4,.7,.4,.03);
    }
    public static void tick(MinecraftServer server) {
        long now=server.getTicks(); immunity.entrySet().removeIf(e -> e.getValue()<=now);
        List<Host> queued=new ArrayList<>();
        Iterator<Map.Entry<UUID,Host>> iterator=hosts.entrySet().iterator();
        while(iterator.hasNext()) {
            Host h=iterator.next().getValue();
            ServerPlayerEntity host=server.getPlayerManager().getPlayer(h.player());
            YellowGhostEntity ghost=find(server,h.ghost());
            if(ghost==null) { iterator.remove(); immunity.put(h.player(),now+600); continue; }
            if(!ghost.isAlive() || host==null || !host.isAlive() || host.isCreative() || host.isSpectator() ||
                host.getWorld()!=ghost.getWorld() || now>=h.expiration()) {
                release(ghost); iterator.remove(); immunity.put(h.player(),now+600); continue;
            }
            ghost.setVelocity(Vec3d.ZERO);
            ghost.refreshPositionAndAngles(host.getX(),host.getY()+.8,host.getZ(),ghost.getYaw(),ghost.getPitch());
            if(now%20==0)mark(host);
            if(h.jumps()>=2 || now-h.started()<50)continue;
            ServerPlayerEntity next=host.getServerWorld().getPlayers().stream()
                .filter(other -> other!=host && other.isAlive() && !other.isSpectator() && !other.isCreative() &&
                    other.squaredDistanceTo(host)<=256 && !occupied(other.getUuid()) &&
                    queued.stream().noneMatch(q -> q.player().equals(other.getUuid())) &&
                    immunity.getOrDefault(other.getUuid(),-1L)<=now && host.canSee(other))
                .min(Comparator.comparingDouble(other -> other.squaredDistanceTo(host))).orElse(null);
            if(next==null)continue;
            Vec3d line=next.getEyePos().subtract(host.getEyePos());
            for(int i=0;i<=16;i++) {
                Vec3d point=host.getEyePos().add(line.multiply(i/16.0));
                host.getServerWorld().spawnParticles(ParticleTypes.WITCH,point.x,point.y,point.z,1,0,0,0,0);
            }
            immunity.put(host.getUuid(),now+600); mark(next);
            iterator.remove(); queued.add(new Host(next.getUuid(),h.ghost(),h.jumps()+1,now,now+120));
        }
        for(Host h:queued)hosts.put(h.ghost(),h);
    }
}
