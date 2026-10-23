package dev.truchosafio4.entity;
import dev.truchosafio4.system.HeartDrainManager;
import dev.truchosafio4.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
public class BlackGhostEntity extends AbstractGhostEntity {
    public BlackGhostEntity(EntityType<? extends HostileEntity> type,World world){super(type,world);}
    public static DefaultAttributeContainer.Builder attributes(){return ghostAttributes(80);}
    @Override protected double flightSpeed(){return .09;}
    @Override protected void ghostAttack(LivingEntity target){
        if(!target.damage(getDamageSources().mobAttack(this),4f))return;
        if(target instanceof ServerPlayerEntity sp && specialCooldown==0){
            if(HeartDrainManager.drain(sp)){animateSpecial();specialCooldown=700;}
        }
    }
    @Override protected void dropLoot(net.minecraft.entity.damage.DamageSource source,boolean byPlayer){super.dropLoot(source,byPlayer);dropStack(new net.minecraft.item.ItemStack(ModItems.ESSENCE_BLACK));}
}
