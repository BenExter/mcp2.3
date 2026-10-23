package dev.truchosafio4.entity;
import dev.truchosafio4.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
public class WhiteGhostEntity extends AbstractGhostEntity {
    public WhiteGhostEntity(EntityType<? extends HostileEntity> type,World world){super(type,world);}
    public static DefaultAttributeContainer.Builder attributes(){return ghostAttributes(28);}
    @Override protected net.minecraft.util.math.Vec3d pursuitPoint(LivingEntity target) {
        if(age % 100 < 35 || squaredDistanceTo(target) < 9) return super.pursuitPoint(target);
        double angle = (getUuid().hashCode() & 65535) * Math.PI * 2 / 65536.0 + age * .012;
        return target.getPos().add(Math.cos(angle)*4, 1.5, Math.sin(angle)*4);
    }
    @Override public void tick() {
        if(!getWorld().isClient && age % 20 == 0 && getTarget() != null) {
            for(WhiteGhostEntity ally : getWorld().getEntitiesByClass(WhiteGhostEntity.class,
                getBoundingBox().expand(20), e -> e != this && e.isAlive())) {
                if(ally.getTarget() == null) ally.setTarget(getTarget());
            }
        }
        super.tick();
    }
    @Override protected void ghostAttack(LivingEntity target){target.damage(getDamageSources().mobAttack(this),5f);}
    @Override protected void dropLoot(net.minecraft.entity.damage.DamageSource source,boolean byPlayer){super.dropLoot(source,byPlayer);dropStack(new net.minecraft.item.ItemStack(ModItems.ESSENCE_WHITE));}
}
