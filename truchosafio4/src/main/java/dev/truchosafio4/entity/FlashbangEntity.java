package dev.truchosafio4.entity;
import dev.truchosafio4.Truchosafio4;
import dev.truchosafio4.network.FlashPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.EnumSet;

/** Flat, true front/back/profile rendering is implemented by the GeckoLib geometry atlas. */
public final class FlashbangEntity extends AnimatedHostileEntity {
    public FlashbangEntity(EntityType<? extends HostileEntity> type, World world) { super(type, world); }
    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, .53).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40);
    }
    private int cooldown;
    @Override protected void initGoals() {
        this.goalSelector.add(0,new SwimGoal(this));
        this.goalSelector.add(1,new Chase(this));
        this.targetSelector.add(1,new ActiveTargetGoal<>(this, PlayerEntity.class,true));
    }
    @Override public void tick() { super.tick(); if(cooldown>0) cooldown--; }
    private void flash() {
        if (!(getWorld() instanceof ServerWorld sw) || cooldown>0) return;
        cooldown=100;
        animateSpecial();
        sw.playSound(null, getX(),getY(),getZ(), Truchosafio4.FLASH_SOUND, SoundCategory.HOSTILE, 2f, 1f);
        sw.spawnParticles(ParticleTypes.FLASH,getX(),getY()+1.5,getZ(),2,0,0,0,0);
        for(ServerPlayerEntity player:sw.getPlayers()) {
            if(player.isSpectator() || player.squaredDistanceTo(this)>24*24 || !player.canSee(this)) continue;
            Vec3d look=player.getRotationVec(1f);
            Vec3d line=getEyePos().subtract(player.getEyePos()).normalize();
            int time=look.dotProduct(line)>.3?100:40;
            ServerPlayNetworking.send(player,new FlashPayload(time));
            if(player.squaredDistanceTo(this)<=36) player.damage(player.getDamageSources().mobAttack(this),52f);
        }
        discard();
    }
    private static final class Chase extends Goal {
        private final FlashbangEntity mob;
        Chase(FlashbangEntity mob) { this.mob=mob;setControls(EnumSet.of(Control.MOVE,Control.LOOK)); }
        @Override public boolean canStart() { return mob.getTarget()!=null && mob.getTarget().isAlive(); }
        @Override public boolean shouldContinue() { return canStart(); }
        @Override public void tick() {
            LivingEntity t=mob.getTarget(); if(t==null)return;
            mob.getLookControl().lookAt(t,30,30);
            mob.getNavigation().startMovingTo(t,1.0);
            if(mob.squaredDistanceTo(t)<=36 && mob.getVisibilityCache().canSee(t)) mob.flash();
        }
    }
}
