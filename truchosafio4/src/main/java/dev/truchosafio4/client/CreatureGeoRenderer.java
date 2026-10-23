package dev.truchosafio4.client;
import dev.truchosafio4.entity.AnimatedHostileEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
public class CreatureGeoRenderer<T extends AnimatedHostileEntity> extends GeoEntityRenderer<T> {
    public CreatureGeoRenderer(EntityRendererFactory.Context context,String id,float shadow){super(context,new CreatureGeoModel<>(id));this.shadowRadius=shadow;}
}
