package dev.truchosafio4.client;
import dev.truchosafio4.Truchosafio4;
import dev.truchosafio4.entity.AnimatedHostileEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
public final class CreatureGeoModel<T extends AnimatedHostileEntity> extends GeoModel<T> {
    private final String name;
    public CreatureGeoModel(String name){this.name=name;}
    @Override public Identifier getModelResource(T entity){return Truchosafio4.id("geo/"+name+".geo.json");}
    @Override public Identifier getTextureResource(T entity){return Truchosafio4.id("textures/entity/"+name+".png");}
    @Override public Identifier getAnimationResource(T entity){return Truchosafio4.id("animations/"+name+".animation.json");}
}
