package dev.truchosafio4.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
public class RaptorEntity extends AnimatedHostileEntity {
    public RaptorEntity(EntityType<? extends HostileEntity> type,World world){super(type,world);}
    public static DefaultAttributeContainer.Builder attributes(){return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH,28).add(EntityAttributes.GENERIC_MOVEMENT_SPEED,.38).add(EntityAttributes.GENERIC_ATTACK_DAMAGE,5);}
    @Override public boolean tryAttack(net.minecraft.entity.Entity target){
        boolean hit=super.tryAttack(target);if(hit)animateAttack();return hit;
    }
    @Override protected void initGoals(){goalSelector.add(1,new MeleeAttackGoal(this,1.2,true));goalSelector.add(2,new WanderAroundFarGoal(this,1));targetSelector.add(1,new ActiveTargetGoal<>(this,PlayerEntity.class,true));}
}
