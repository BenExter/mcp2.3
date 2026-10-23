package dev.truchosafio4.client;
import dev.truchosafio4.entity.QumaraEntity;
import dev.truchosafio4.network.FlashPayload;
import dev.truchosafio4.network.QumaraControlPayload;
import dev.truchosafio4.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
public final class Truchosafio4Client implements ClientModInitializer {
    private static KeyBinding shield,raptors;
    @Override public void onInitializeClient(){
        EntityRendererRegistry.register(ModEntities.FLASHBANG,ctx->new CreatureGeoRenderer<>(ctx,"flashbang",.3f));
        EntityRendererRegistry.register(ModEntities.RED,ctx->new CreatureGeoRenderer<>(ctx,"rojo",.5f));
        EntityRendererRegistry.register(ModEntities.BLACK,ctx->new CreatureGeoRenderer<>(ctx,"negro",.6f));
        EntityRendererRegistry.register(ModEntities.YELLOW,ctx->new CreatureGeoRenderer<>(ctx,"amarillo",.4f));
        EntityRendererRegistry.register(ModEntities.WHITE,ctx->new CreatureGeoRenderer<>(ctx,"blanco",.4f));
        EntityRendererRegistry.register(ModEntities.QUMARA,ctx->new CreatureGeoRenderer<>(ctx,"qumara",3.3f));
        EntityRendererRegistry.register(ModEntities.RAPTOR,ctx->new CreatureGeoRenderer<>(ctx,"raptor",.55f));
        EntityRendererRegistry.register(ModEntities.MECHANISM,ctx->new CreatureGeoRenderer<>(ctx,"mecanismo",.5f));
        ClientPlayNetworking.registerGlobalReceiver(FlashPayload.ID,(payload,ctx)->ctx.client().execute(()->FlashOverlay.trigger(payload.durationTicks())));
        HudRenderCallback.EVENT.register(FlashOverlay::render);
        shield=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.truchosafio4.shield",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_R,"category.truchosafio4.boss"));
        raptors=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.truchosafio4.raptors",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_F,"category.truchosafio4.boss"));
        ClientTickEvents.START_CLIENT_TICK.register(Truchosafio4Client::tick);
    }
    private static void tick(MinecraftClient client){
        if(client.player==null||!(client.player.getVehicle() instanceof QumaraEntity))return;
        if(client.currentScreen!=null)return;
        int yaw=Math.round(client.player.getYaw());
        int fwd=(client.options.forwardKey.isPressed()?1:0)-(client.options.backKey.isPressed()?1:0);
        int side=(client.options.rightKey.isPressed()?1:0)-(client.options.leftKey.isPressed()?1:0);
        if(client.player.age%2==0)send(0,yaw,fwd,side);
        while(shield.wasPressed())send(3,yaw,0,0);
        while(raptors.wasPressed())send(4,yaw,0,0);
    }
    private static void send(int action,int yaw,int f,int s){ClientPlayNetworking.send(new QumaraControlPayload(action,yaw,f,s));}
}
