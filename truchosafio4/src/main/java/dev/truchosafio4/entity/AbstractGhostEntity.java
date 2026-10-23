package dev.truchosafio4.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Flying custom-logic family. All damage and target changes happen on server. */
public abstract class AbstractGhostEntity extends AnimatedHostileEntity {
    protected int specialCooldown;
    protected int attackCooldown;
    protected AbstractGhostEntity(EntityType<? extends HostileEntity> type, World world) { super(type,world); setNoGravity(true); }
    protected static DefaultAttributeContainer.Builder ghostAttributes(double hp) {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH,hp)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,.25).add(EntityAttributes.GENERIC_FOLLOW_RANGE,32);
    }
    @Override protected void initGoals() { targetSelector.add(1,new ActiveTargetGoal<>(this, PlayerEntity.class,true)); }
    protected abstract void ghostAttack(LivingEntity target);
    protected Vec3d pursuitPoint(LivingEntity target) { return target.getPos().add(0, 1, 0); }
    protected boolean scriptedMovement() { return false; }
    @Override public boolean handleFallDamage(float distance, float multiplier, net.minecraft.entity.damage.DamageSource source) { return false; }
    protected double flightSpeed() { return .14; }
    @Override public void tick() {
        super.tick(); setNoGravity(true);
        if (getWorld().isClient) return;
        if(specialCooldown>0) specialCooldown--;
        if(attackCooldown>0) attackCooldown--;
        if (scriptedMovement()) return;
        LivingEntity target=getTarget();
        if(target==null || !target.isAlive()) { setVelocity(getVelocity().multiply(.72)); return; }
        if(target instanceof PlayerEntity p && (p.isCreative() || p.isSpectator())) { setTarget(null); return; }
        Vec3d to=pursuitPoint(target).subtract(getPos());
        getLookControl().lookAt(target, 30, 30);
        double distance=to.length();
        if(distance>1.6 && distance<36) {
            Vec3d steer=to.normalize().multiply(flightSpeed());
            setVelocity(getVelocity().multiply(.65).add(steer.multiply(.35)));
        } else setVelocity(getVelocity().multiply(.76));
        if(distance<2.0 && attackCooldown==0) {
            attackCooldown=30; ghostAttack(target); animateAttack();
        }
    }
    protected boolean serverWorld() { return getWorld() instanceof ServerWorld; }
}
