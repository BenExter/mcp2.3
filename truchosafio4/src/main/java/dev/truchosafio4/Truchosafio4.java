package dev.truchosafio4;
import dev.truchosafio4.command.ModCommands;
import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.network.FlashPayload;
import dev.truchosafio4.network.QumaraControlPayload;
import dev.truchosafio4.entity.QumaraEntity;
import dev.truchosafio4.registry.ModEntities;
import dev.truchosafio4.registry.ModItems;
import dev.truchosafio4.system.HeartDrainManager;
import dev.truchosafio4.system.PossessionManager;
import dev.truchosafio4.system.SpawnManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class Truchosafio4 implements ModInitializer {
    public static final String ID="truchosafio4";
    public static final Logger LOGGER=LoggerFactory.getLogger("Truchosafio4");
    public static Identifier id(String key){return Identifier.of(ID,key);}
    public static final SoundEvent FLASH_SOUND=Registry.register(Registries.SOUND_EVENT,id("flashbang"),SoundEvent.of(id("flashbang")));
    @Override public void onInitialize(){
        ModConfig.load();ModEntities.registerAttributes();ModItems.initialize();
        PayloadTypeRegistry.playS2C().register(FlashPayload.ID,FlashPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(QumaraControlPayload.ID,QumaraControlPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(QumaraControlPayload.ID,(pkt,ctx)->{
            if(ctx.player().getVehicle() instanceof QumaraEntity boss)
                boss.receiveInput(ctx.player(),pkt.action(),pkt.yaw(),pkt.forward(),pkt.strafe());
        });
        ModCommands.register();
        ServerLifecycleEvents.SERVER_STARTED.register(HeartDrainManager::init);
        ServerLifecycleEvents.SERVER_STOPPING.register(PossessionManager::stop);
        ServerLifecycleEvents.SERVER_STARTED.register(server->PossessionManager.clear());
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->HeartDrainManager.apply(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP,newP,alive)->HeartDrainManager.apply(newP));
        ServerTickEvents.END_SERVER_TICK.register(server->{
            SpawnManager.tick(server);PossessionManager.tick(server);
        });
        LOGGER.info("Truchosafio4: entidades y control de spawn registrados; spawn natural APAGADO por defecto");
    }
}
