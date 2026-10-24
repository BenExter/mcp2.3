package dev.truchosafio4.command;

import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.registry.ModEntities;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import com.mojang.brigadier.context.CommandContext;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ModCommands {
    public static void register(){
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->{
            var root=literal("truchosafio").requires(source->source.hasPermissionLevel(2));
            root.then(literal("spawn").then(literal("flashbang")
                .executes(ctx->spawn(ctx,ctx.getSource().getWorld(),ctx.getSource().getPosition()))
                .then(argument("dimension",DimensionArgumentType.dimension())
                    .then(argument("pos",Vec3ArgumentType.vec3()).executes(ctx->spawn(ctx,
                        DimensionArgumentType.getDimensionArgument(ctx,"dimension"),Vec3ArgumentType.getVec3(ctx,"pos")))))));
            var natural=literal("flashbang");
            for(String name:new String[]{"overworld","nether"}){
                String dim=name.equals("overworld")?"minecraft:overworld":"minecraft:the_nether";
                natural.then(literal(name).then(literal("on").executes(ctx->setSpawn(ctx,dim,true)))
                    .then(literal("off").executes(ctx->setSpawn(ctx,dim,false))));
            }
            root.then(literal("natural").then(natural));
            root.then(literal("reload").executes(ctx->{
                try{
                    ModConfig.load();
                    ctx.getSource().sendFeedback(()->Text.literal("[Truchosafio4] Configuración de Flashbang recargada"),false);
                    return 1;
                }catch(IllegalStateException e){ctx.getSource().sendError(Text.literal(e.getMessage()));return 0;}
            }));
            dispatcher.register(root);
        });
    }
    private static int setSpawn(CommandContext<ServerCommandSource> ctx,String dim,boolean enabled){
        var config=ModConfig.ACTIVE;
        var rule=config.mobs.get("flashbang");
        rule.dimension_enabled.put(dim,enabled);
        if(!rule.dimension_whitelist.contains(dim)){
            var dimensions=new java.util.ArrayList<>(rule.dimension_whitelist);
            dimensions.add(dim);rule.dimension_whitelist=dimensions;
        }
        rule.natural_spawn_enabled=rule.dimension_enabled.values().stream().anyMatch(Boolean::booleanValue);
        if(enabled)config.natural_spawning_enabled=true;
        ModConfig.save();
        ctx.getSource().sendFeedback(()->Text.literal("flashbang / "+dim+": "+(enabled?"ON":"OFF")),true);
        return 1;
    }
    private static int spawn(CommandContext<ServerCommandSource> ctx,ServerWorld world,Vec3d pos){
        var flashbang=ModEntities.FLASHBANG.create(world);
        if(flashbang==null)return 0;
        flashbang.refreshPositionAndAngles(pos.x,pos.y,pos.z,0,0);
        if(!world.spawnEntity(flashbang))return 0;
        ctx.getSource().sendFeedback(()->Text.literal("Invocado Flashbang en "+world.getRegistryKey().getValue()),true);
        return 1;
    }
}
