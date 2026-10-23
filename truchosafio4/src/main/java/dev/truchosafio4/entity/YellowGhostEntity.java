package dev.truchosafio4.entity;
import dev.truchosafio4.system.PossessionManager;
import dev.truchosafio4.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
public class YellowGhostEntity extends AbstractGhostEntity {
    public YellowGhostEntity(EntityType<? extends HostileEntity> type,World world){super(type,world);}
    public static DefaultAttributeContainer.Builder attributes(){return ghostAttributes(45);}
    @Override protected boolean scriptedMovement(){return PossessionManager.isPossessing(getUuid());}
    @Override public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt){
        super.readCustomDataFromNbt(nbt);setInvisible(false);setInvulnerable(false);
    }
    @Override protected void ghostAttack(LivingEntity target){
        if(!target.damage(getDamageSources().mobAttack(this),3f))return;
        if(target instanceof ServerPlayerEntity sp && specialCooldown==0 && PossessionManager.possess(this,sp)){
            animateSpecial();specialCooldown=600;
        }
    }
    @Override protected void dropLoot(net.minecraft.entity.damage.DamageSource source,boolean byPlayer){super.dropLoot(source,byPlayer);dropStack(new net.minecraft.item.ItemStack(ModItems.ESSENCE_YELLOW));}
}
