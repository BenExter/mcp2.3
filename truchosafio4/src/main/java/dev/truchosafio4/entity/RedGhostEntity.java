package dev.truchosafio4.entity;
import dev.truchosafio4.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
public class RedGhostEntity extends AbstractGhostEntity {
    public RedGhostEntity(EntityType<? extends HostileEntity> type,World world){super(type,world);}
    public static DefaultAttributeContainer.Builder attributes(){return ghostAttributes(60);}
    @Override protected double flightSpeed(){return .2;}
    @Override protected void ghostAttack(LivingEntity target){target.damage(getDamageSources().mobAttack(this),7f);}
    private int dashTicks;
    private Vec3d dashDirection = Vec3d.ZERO;
    @Override protected boolean scriptedMovement() { return dashTicks > 0; }
    @Override public void tick(){
        super.tick(); if(getWorld().isClient)return;
        LivingEntity t=getTarget();
        if(dashTicks>0){
            dashTicks--;
            if(horizontalCollision || t==null || !t.isAlive()){dashTicks=0;return;}
            setVelocity(dashDirection.multiply(1.15));
            if(getBoundingBox().stretch(getVelocity()).expand(.5).intersects(t.getBoundingBox())){
                t.damage(getDamageSources().mobAttack(this),12f);
                attackCooldown=30;dashTicks=0;setVelocity(getVelocity().multiply(.2));
            }
            return;
        }
        if(t==null || specialCooldown>0)return;
        double d=squaredDistanceTo(t);
        if(d>16 && d<256 && getVisibilityCache().canSee(t)){
            specialCooldown=100;dashTicks=12;
            dashDirection=t.getEyePos().subtract(getEyePos()).normalize();
            setVelocity(dashDirection.multiply(1.15));animateSpecial();
        }
    }
    @Override protected void dropLoot(net.minecraft.entity.damage.DamageSource source,boolean causedByPlayer){super.dropLoot(source,causedByPlayer);dropStack(new net.minecraft.item.ItemStack(ModItems.ESSENCE_RED));}
}
