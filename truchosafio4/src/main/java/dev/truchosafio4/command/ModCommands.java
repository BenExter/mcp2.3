package dev.truchosafio4.command;
import dev.truchosafio4.config.ModConfig;
import dev.truchosafio4.entity.QumaraEntity;
import dev.truchosafio4.registry.ModEntities;
import dev.truchosafio4.system.HeartDrainManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import com.mojang.brigadier.context.CommandContext;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public final class ModCommands {
    public static void register(){CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment)->{
        var root=literal("truchosafio").requires(src->src.hasPermissionLevel(2));
        var spawn=literal("spawn");
        for(String kind:new String[]{"flashbang","rojo","negro","amarillo","blanco","qumara","raptor"}){
            spawn.then(literal(kind).executes(ctx->spawnLocal(ctx,kind))
                .then(argument("dimension",DimensionArgumentType.dimension())
                    .then(argument("pos",Vec3ArgumentType.vec3())
                        .executes(ctx->spawnTarget(ctx,kind)))));
        }
        root.then(spawn);
        var natural=literal("natural");
        for(String kind:ModConfig.KINDS){
            var mob=literal(kind);
            for(String name:new String[]{"overworld","nether"}){
                String dim=name.equals("overworld")?"minecraft:overworld":"minecraft:the_nether";
                mob.then(literal(name).then(literal("on").executes(ctx->setSpawn(ctx,kind,dim,true)))
                     .then(literal("off").executes(ctx->setSpawn(ctx,kind,dim,false))));
            }
            natural.then(mob);
        }
        root.then(natural);
        root.then(literal("reload").executes(ctx->{
            try{ModConfig.load();ctx.getSource().sendFeedback(()->Text.literal("[Truchosafio4] Configuración recargada"),false);return 1;}
            catch(IllegalStateException e){ctx.getSource().sendError(Text.literal(e.getMessage()));return 0;}
        }));
        root.then(literal("qumara").then(literal("mount").executes(ctx->{
            ServerPlayerEntity p=ctx.getSource().getPlayerOrThrow();
            var list=p.getServerWorld().getEntitiesByClass(QumaraEntity.class,p.getBoundingBox().expand(25),Entity::isAlive);
            if(list.isEmpty())return 0;
            list.sort(java.util.Comparator.comparingDouble(q->q.squaredDistanceTo(p)));
            boolean yes=list.get(0).mount(p);return yes?1:0;
        })).then(literal("release").executes(ctx->{
            ServerPlayerEntity p=ctx.getSource().getPlayerOrThrow();
            if(p.getVehicle() instanceof QumaraEntity q){q.releaseControl();return 1;}return 0;
        })).then(literal("aura").then(literal("on").executes(ctx->aura(ctx,true)))
           .then(literal("off").executes(ctx->aura(ctx,false)))));
        root.then(literal("hearts").then(literal("get").then(argument("jugador",EntityArgumentType.player()).executes(ctx->{
            ServerPlayerEntity p=EntityArgumentType.getPlayer(ctx,"jugador");int stolen=HeartDrainManager.get(p);
            ctx.getSource().sendFeedback(()->Text.literal("Corazones robados: "+stolen),false);return stolen;
        }))).then(literal("restore").then(argument("jugador",EntityArgumentType.player()).then(argument("cantidad",integer(1,7)).executes(ctx->{
            ServerPlayerEntity p=EntityArgumentType.getPlayer(ctx,"jugador");HeartDrainManager.restore(p,getInteger(ctx,"cantidad"));return 1;
        })))));
        dispatcher.register(root);
        dispatcher.register(literal("qumara").requires(src->src.hasPermissionLevel(2))
            .then(literal("spawn").executes(ctx->spawnLocal(ctx,"qumara")))
            .then(literal("control").executes(ctx->{ServerPlayerEntity p=ctx.getSource().getPlayerOrThrow();
                var near=p.getServerWorld().getEntitiesByClass(QumaraEntity.class,p.getBoundingBox().expand(26),Entity::isAlive);
                near.sort(java.util.Comparator.comparingDouble(q->q.squaredDistanceTo(p)));
                return !near.isEmpty() && near.get(0).mount(p)?1:0;}))
            .then(literal("release").executes(ctx->{ServerPlayerEntity p=ctx.getSource().getPlayerOrThrow();
                if(p.getVehicle() instanceof QumaraEntity q){q.releaseControl();return 1;}return 0;})));
    });}
    private static int aura(CommandContext<ServerCommandSource> ctx,boolean on)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=ctx.getSource().getPlayerOrThrow();
        if(p.getVehicle() instanceof QumaraEntity q){q.setAura(on);return 1;}
        return 0;
    }
    private static int setSpawn(CommandContext<ServerCommandSource> ctx,String kind,String dim,boolean enabled){
        var c=ModConfig.ACTIVE;var rule=c.mobs.get(kind);if(rule==null)return 0;
        rule.natural_spawn_enabled=enabled || rule.natural_spawn_enabled;
        rule.dimension_enabled.put(dim,enabled);
        if(!rule.dimension_whitelist.contains(dim)){
            var names=new java.util.ArrayList<>(rule.dimension_whitelist);names.add(dim);rule.dimension_whitelist=names;
        }
        if(!enabled && rule.dimension_enabled.values().stream().noneMatch(Boolean::booleanValue))rule.natural_spawn_enabled=false;
        if(enabled)c.natural_spawning_enabled=true;
        ModConfig.save();ctx.getSource().sendFeedback(()->Text.literal(kind+" / "+dim+": "+(enabled?"ON":"OFF")),true);
        return 1;
    }
    private static int spawnLocal(CommandContext<ServerCommandSource> ctx,String kind){return spawn(ctx,kind,ctx.getSource().getWorld(),ctx.getSource().getPosition());}
    private static int spawnTarget(CommandContext<ServerCommandSource> ctx,String kind)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        return spawn(ctx,kind,DimensionArgumentType.getDimensionArgument(ctx,"dimension"),Vec3ArgumentType.getVec3(ctx,"pos"));
    }
    private static int spawn(CommandContext<ServerCommandSource> ctx,String kind,ServerWorld world,Vec3d pos){
        EntityType<?> type=ModEntities.of(kind);if(type==null)return 0;
        Entity entity=type.create(world);if(entity==null)return 0;
        entity.refreshPositionAndAngles(pos.x,pos.y,pos.z,0,0);
        if(!world.spawnEntity(entity))return 0;
        ctx.getSource().sendFeedback(()->Text.literal("Invocado "+kind+" en "+world.getRegistryKey().getValue()),true);
        return 1;
    }
}
