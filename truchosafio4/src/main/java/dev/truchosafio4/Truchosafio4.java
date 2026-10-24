package dev.truchosafio4;
import dev.truchosafio4.command.ModCommands;
import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.network.FlashPayload;
import dev.truchosafio4.registry.ModEntities;
import dev.truchosafio4.registry.ModItems;
import dev.truchosafio4.system.SpawnManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
        ModCommands.register();
        ServerTickEvents.END_SERVER_TICK.register(SpawnManager::tick);
        LOGGER.info("Truchosafio4: Flashbang registrado; spawn natural APAGADO por defecto");
    }
}
