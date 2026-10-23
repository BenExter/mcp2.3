package dev.truchosafio4.mixin.client;

import dev.truchosafio4.entity.QumaraEntity;
import dev.truchosafio4.network.QumaraControlPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Route mouse actions before vanilla can hit the boss or place/use held items. */
@Mixin(MinecraftClient.class)
public abstract class QumaraInputMixin {
    private boolean truchosafio4$send(int action){
        MinecraftClient client=(MinecraftClient)(Object)this;
        if(client.player==null || !(client.player.getVehicle() instanceof QumaraEntity))return false;
        ClientPlayNetworking.send(new QumaraControlPayload(action,Math.round(client.player.getYaw()),0,0));
        return true;
    }
    @Inject(method="handleInputEvents",at=@At("HEAD"))
    private void truchosafio4$noOffhandSwap(CallbackInfo ci){
        MinecraftClient client=(MinecraftClient)(Object)this;
        if(client.player!=null && client.player.getVehicle() instanceof QumaraEntity)
            while(client.options.swapHandsKey.wasPressed()) { /* the mount owns F */ }
    }
    @Inject(method="doAttack",at=@At("HEAD"),cancellable=true)
    private void truchosafio4$attack(CallbackInfoReturnable<Boolean> cir){
        if(truchosafio4$send(1))cir.setReturnValue(false);
    }
    @Inject(method="doItemUse",at=@At("HEAD"),cancellable=true)
    private void truchosafio4$use(CallbackInfo ci){if(truchosafio4$send(2))ci.cancel();}
    @Inject(method="handleBlockBreaking",at=@At("HEAD"),cancellable=true)
    private void truchosafio4$noMining(boolean breaking,CallbackInfo ci){
        MinecraftClient client=(MinecraftClient)(Object)this;
        if(client.player!=null && client.player.getVehicle() instanceof QumaraEntity)ci.cancel();
    }
}
