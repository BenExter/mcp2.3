package dev.truchosafio4.entity;
import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.*;

/** Admin ride = native AI completely disabled. All abilities server-authoritative. */
public final class QumaraEntity extends AnimatedHostileEntity {
    private final net.minecraft.entity.boss.ServerBossBar bossBar = new net.minecraft.entity.boss.ServerBossBar(
        net.minecraft.text.Text.literal("Qumara"),net.minecraft.entity.boss.BossBar.Color.PURPLE,net.minecraft.entity.boss.BossBar.Style.PROGRESS);
    @Override public void onStartedTrackingBy(ServerPlayerEntity player){super.onStartedTrackingBy(player);bossBar.addPlayer(player);}
    @Override public void onStoppedTrackingBy(ServerPlayerEntity player){super.onStoppedTrackingBy(player);bossBar.removePlayer(player);}
    private final Map<Integer,Long> cooldown = new HashMap<>();
    private boolean auraOn=true;
    private boolean shieldOn=false;
    private boolean shieldBroken=false;
    private boolean mechanismsSpawned=false;
    private final dev.truchosafio4.logic.MechanismShield shield = new dev.truchosafio4.logic.MechanismShield();
    private float moveFwd,moveSide;
    private int desiredYaw;
    private long lastInput;
    private int quakeTick=-1;
    private int pullTicks=0;
    private int summonsTick=-1;
    public QumaraEntity(EntityType<? extends HostileEntity> type,World world){super(type,world);setPersistent();
        if(ModConfig.ACTIVE != null) {
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(ModConfig.ACTIVE.qumara.max_health);
            setHealth(getMaxHealth());
        }}
    public static DefaultAttributeContainer.Builder attributes(){return HostileEntity.createHostileAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH,1000).add(EntityAttributes.GENERIC_MOVEMENT_SPEED,.23)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,16).add(EntityAttributes.GENERIC_FOLLOW_RANGE,48).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,1.0);}
    @Override protected void initGoals(){
        goalSelector.add(0,new SwimGoal(this));goalSelector.add(1,new MeleeAttackGoal(this,1.0,false));
        goalSelector.add(3,new WanderAroundFarGoal(this,.7));
        targetSelector.add(1,new ActiveTargetGoal<>(this, PlayerEntity.class,true));
    }
    @Override protected boolean canAddPassenger(Entity passenger){return passenger instanceof PlayerEntity && getPassengerList().isEmpty();}
    // Vanilla must not give the rider's client vehicle-position authority: custom input is applied on the server.
    @Override public LivingEntity getControllingPassenger() {return null;}
    public PlayerEntity getAdminController() {return getFirstPassenger() instanceof PlayerEntity p ? p : null;}
    public boolean isControlled(){return getAdminController() instanceof PlayerEntity;}
    public boolean mount(ServerPlayerEntity player){
        if(!player.hasPermissionLevel(2) || !ModConfig.ACTIVE.qumara.allow_admin_control || !getPassengerList().isEmpty())return false;
        if(!player.startRiding(this,true))return false;
        setAiDisabled(true);getNavigation().stop();setTarget(null);
        lastInput= getWorld().getTime(); desiredYaw=Math.round(player.getYaw());moveFwd=0;moveSide=0;return true;
    }
    public void releaseControl(){for(Entity e:List.copyOf(getPassengerList()))e.stopRiding();setAiDisabled(false);setVelocity(Vec3d.ZERO);}
    public void receiveInput(ServerPlayerEntity player,int action,int yaw,int fwd,int side){
        if(getAdminController()!=player || !player.hasPermissionLevel(2) || !ModConfig.ACTIVE.qumara.allow_admin_control)return;
        if(action==0){desiredYaw=Math.floorMod(yaw,360);moveFwd=Math.max(-1,Math.min(1,fwd));moveSide=Math.max(-1,Math.min(1,side));lastInput=getWorld().getTime();}
        else cast(action);
    }
    public void setAura(boolean on){auraOn=on;}
    public boolean isAuraOn(){return auraOn;}
    private boolean available(int ability){return getWorld().getTime()>=cooldown.getOrDefault(ability,0L);}
    private void startCd(int ability,int ticks){cooldown.put(ability,getWorld().getTime()+ticks);}
    public boolean cast(int ability){
        if(!(getWorld() instanceof ServerWorld) || !available(ability))return false;
        switch(ability){
            case 1 -> {startCd(ability,100);quakeTick=29;animateAttack();return true;}
            case 2 -> {startCd(ability,240);pullTicks=50;animateSpecial();return true;}
            case 3 -> {
                if(shieldBroken)return false;
                startCd(ability,20);
                shieldOn=!shieldOn;
                if(shieldOn && !mechanismsSpawned) createMechanisms();
                triggerAnim("ability","shield");return true;
            }
            case 4 -> {if(!ModConfig.ACTIVE.qumara.summon_raptors)return false;startCd(ability,700);summonsTick=35;triggerAnim("ability","summon");return true;}
            default -> {return false;}
        }
    }
    private void createMechanisms(){
        if(!(getWorld() instanceof ServerWorld sw))return;
        mechanismsSpawned=true;
        int total=14;
        for(int i=0;i<total;i++){
            QumaraMechanismEntity mech=ModEntities.MECHANISM.create(sw);
            if(mech==null)continue;
            double ang=i*Math.PI*2/total;
            mech.assign(getUuid(),i);
            mech.refreshPositionAndAngles(getX()+Math.cos(ang)*12,getY()+2,getZ()+Math.sin(ang)*12,0,0);
            if(sw.spawnEntity(mech))shield.add(i);
        }
    }
    public void mechanismDestroyed(int slot){
        shield.destroy(slot);
        if(!shield.protects()){shieldOn=false;shieldBroken=true;}
    }
    public int remainingMechanisms(){return shield.remaining();}
    @Override public boolean damage(net.minecraft.entity.damage.DamageSource source,float amount){
        if(shieldOn && !shieldBroken && getWorld() instanceof ServerWorld sw){
            if(shield.protects())return false;
            shieldOn=false;shieldBroken=true;
        }
        return super.damage(source,amount);
    }
    @Override public void tick(){
        super.tick();
        if(!(getWorld() instanceof ServerWorld sw))return;
        bossBar.setPercent(Math.max(0,Math.min(1,getHealth()/getMaxHealth())));
        bossBar.setName(net.minecraft.text.Text.literal("Qumara"+(shieldOn?" — mecanismos: "+shield.remaining()+"/14":"")));
        if(getAdminController() instanceof ServerPlayerEntity controller &&
            (!controller.hasPermissionLevel(2) || !ModConfig.ACTIVE.qumara.allow_admin_control || !controller.isAlive())) releaseControl();
        if(isControlled()){
            if(!isAiDisabled())setAiDisabled(true);
            if(getWorld().getTime()-lastInput>10){moveFwd=0;moveSide=0;}
            float a=(float)Math.toRadians(desiredYaw);
            Vec3d forward=new Vec3d(-Math.sin(a),0,Math.cos(a));
            Vec3d right=new Vec3d(Math.cos(a),0,Math.sin(a));
            Vec3d motion=forward.multiply(moveFwd).add(right.multiply(moveSide));
            if(motion.lengthSquared()>1)motion=motion.normalize();
            setYaw(desiredYaw);setBodyYaw(desiredYaw);setHeadYaw(desiredYaw);
            setVelocity(motion.multiply(.27).add(0,getVelocity().y,0));
        }else if(isAiDisabled()){setAiDisabled(false);moveFwd=0;moveSide=0;}
        if(quakeTick>=0 && --quakeTick==0){quakeTick=-1;shockwave(sw);}
        if(pullTicks>0){pullTicks--;vacuum(sw);}
        if(summonsTick>=0 && --summonsTick==0){summonsTick=-1;summonRaptors(sw);}
        if(age%20==0 && auraOn && ModConfig.ACTIVE.qumara.poison_aura_enabled){
            for(ServerPlayerEntity p:sw.getPlayers()){
                if(p.isCreative()||p.isSpectator()||p==getAdminController())continue;
                if(p.squaredDistanceTo(this)<64)p.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,60,0));
            }
        }
        if(shieldOn && age%4==0) {
            for(int i=0;i<32;i++) {
                double a=i*Math.PI*2/32, y=3.5+Math.sin(age*.05+i)*2.5;
                sw.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,getX()+Math.cos(a)*6,getY()+y,getZ()+Math.sin(a)*6,1,0,0,0,0);
            }
        }
        if(age%40==0 && shieldOn && !shieldBroken && !shield.protects()){shieldOn=false;shieldBroken=true;}
    }
    @Override public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("LivingMechanisms",shield.mask());
        nbt.putBoolean("AuraOn",auraOn);nbt.putBoolean("ShieldOn",shieldOn);
        nbt.putBoolean("ShieldBroken",shieldBroken);nbt.putBoolean("MechanismsSpawned",mechanismsSpawned);
        for(int i=1;i<=4;i++) nbt.putLong("AbilityCooldown"+i,Math.max(0,cooldown.getOrDefault(i,0L)-getWorld().getTime()));
    }
    @Override public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if(nbt.contains("AuraOn"))auraOn=nbt.getBoolean("AuraOn");
        shield.restore(nbt.getInt("LivingMechanisms"));
        shieldOn=nbt.getBoolean("ShieldOn");shieldBroken=nbt.getBoolean("ShieldBroken");
        mechanismsSpawned=nbt.getBoolean("MechanismsSpawned");
        for(int i=1;i<=4;i++)cooldown.put(i,getWorld().getTime()+nbt.getLong("AbilityCooldown"+i));
        setAiDisabled(false);
    }
    private void shockwave(ServerWorld sw){
        for(ServerPlayerEntity player:sw.getPlayers()){
            if(player.isCreative()||player.isSpectator()||player==getAdminController()||player.squaredDistanceTo(this)>49)continue;
            player.damage(player.getDamageSources().mobAttack(this),16f);
            player.getItemCooldownManager().set(Items.SHIELD,100);
            Vec3d delta=player.getPos().subtract(getPos()).normalize();
            player.addVelocity(delta.x*.8,.5,delta.z*.8);player.velocityModified=true;
        }
    }
    private void vacuum(ServerWorld sw){
        Vec3d front=Vec3d.fromPolar(0,getYaw()).multiply(7).add(getPos());
        for(ServerPlayerEntity player:sw.getPlayers()){
            if(player==getAdminController()||player.isCreative()||player.isSpectator())continue;
            Vec3d diff=front.subtract(player.getPos());if(diff.lengthSquared()>256||diff.lengthSquared()<1)continue;
            Vec3d force=diff.normalize().multiply(pullTicks==1?-.8:.18);
            player.addVelocity(force.x,force.y*.5,force.z);player.velocityModified=true;
        }
    }
    private void summonRaptors(ServerWorld sw){
        int count=sw.getEntitiesByClass(RaptorEntity.class,getBoundingBox().expand(48),RaptorEntity::isAlive).size();
        if(count>=8)return;
        int toSpawn=Math.min(3,8-count);
        for(int i=0;i<toSpawn;i++){
            RaptorEntity r=ModEntities.RAPTOR.create(sw);if(r==null)continue;
            double ang=i*2*Math.PI/toSpawn;
            r.refreshPositionAndAngles(getX()+Math.cos(ang)*6,getY()+1,getZ()+Math.sin(ang)*6,0,0);
            sw.spawnEntity(r);
        }
    }
}
