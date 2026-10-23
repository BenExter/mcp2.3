package dev.truchosafio4.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class AnimatedHostileEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected AnimatedHostileEntity(EntityType<? extends HostileEntity> type, World world) { super(type, world); }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "locomotion", 5, state -> {
            state.setAndContinue(RawAnimation.begin().thenLoop(state.isMoving()
                ? "animation.creature.walk" : "animation.creature.fly"));
            return PlayState.CONTINUE;
        }));
        registrar.add(new AnimationController<>(this, "ability", state -> PlayState.STOP)
            .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.creature.attack"))
            .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.creature.special"))
            .triggerableAnim("shield", RawAnimation.begin().thenPlay("animation.creature.shield"))
            .triggerableAnim("summon", RawAnimation.begin().thenPlay("animation.creature.summon")));
    }
    protected void animateAttack() { if(!getWorld().isClient) triggerAnim("ability", "attack"); }
    protected void animateSpecial() { if(!getWorld().isClient) triggerAnim("ability", "special"); }
}
