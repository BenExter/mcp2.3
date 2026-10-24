package dev.truchosafio4.client;
import dev.truchosafio4.network.FlashPayload;
import dev.truchosafio4.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
public final class Truchosafio4Client implements ClientModInitializer {
    @Override public void onInitializeClient(){
        EntityRendererRegistry.register(ModEntities.FLASHBANG,ctx->new CreatureGeoRenderer<>(ctx,"flashbang",.3f));
        ClientPlayNetworking.registerGlobalReceiver(FlashPayload.ID,(payload,ctx)->ctx.client().execute(()->FlashOverlay.trigger(payload.durationTicks())));
        HudRenderCallback.EVENT.register(FlashOverlay::render);
    }
}
