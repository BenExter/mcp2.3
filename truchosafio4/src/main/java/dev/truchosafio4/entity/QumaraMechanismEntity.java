package dev.truchosafio4.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import java.util.UUID;
public class QumaraMechanismEntity extends AnimatedHostileEntity {
    private UUID owner;
    private int slot;
    public QumaraMechanismEntity(EntityType<? extends HostileEntity> type,World world){super(type,world);setNoGravity(true);setPersistent();}
    public static DefaultAttributeContainer.Builder attributes(){return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH,40).add(EntityAttributes.GENERIC_MOVEMENT_SPEED,0);}
    @Override protected void initGoals() {}
    public UUID owner(){return owner;}
    public void assign(UUID owner,int slot){this.owner=owner;this.slot=slot;}
    @Override public void tick(){
        super.tick();setNoGravity(true);setVelocity(0,0,0);
        if(getWorld() instanceof net.minecraft.server.world.ServerWorld sw && owner!=null && sw.getEntity(owner) instanceof QumaraEntity boss){
            if(!boss.isAlive()){discard();return;}
            double angle=slot*Math.PI*2/14 + getWorld().getTime()*.005;
            refreshPositionAndAngles(boss.getX()+Math.cos(angle)*12,boss.getY()+2+Math.sin(angle*2)*.8,
                boss.getZ()+Math.sin(angle)*12,getYaw()+1,0);
        }
    }
    @Override public void onDeath(net.minecraft.entity.damage.DamageSource source){
        if(getWorld() instanceof net.minecraft.server.world.ServerWorld sw && owner!=null && sw.getEntity(owner) instanceof QumaraEntity boss)
            boss.mechanismDestroyed(slot);
        super.onDeath(source);
    }
    @Override public void writeCustomDataToNbt(NbtCompound nbt){super.writeCustomDataToNbt(nbt);if(owner!=null)nbt.putUuid("QumaraOwner",owner);nbt.putInt("Slot",slot);}
    @Override public void readCustomDataFromNbt(NbtCompound nbt){super.readCustomDataFromNbt(nbt);if(nbt.containsUuid("QumaraOwner"))owner=nbt.getUuid("QumaraOwner");slot=nbt.getInt("Slot");}
}
