package dev.truchosafio4.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.*;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.*;

/** Uses the actual GeckoLib 4.7.7 loaders, not a replacement JSON schema. No rendering is claimed. */
public final class GeckoResourceTest {
    public static void main(String[] args)throws Exception {
        Path assets=Path.of("src/main/resources/assets/truchosafio4");
        int models=0,animations=0;
        try(var paths=Files.list(assets.resolve("geo"))){
            for(Path path:paths.filter(p->p.toString().endsWith(".geo.json")).sorted().toList()){
                String name=path.getFileName().toString().replace(".geo.json","");
                Model raw=KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(path),Model.class);
                var baked=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(raw));
                if(baked.topLevelBones().isEmpty())throw new AssertionError(name+" empty model");
                JsonObject animationJson=JsonParser.parseString(Files.readString(assets.resolve("animations/"+name+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");
                BakedAnimations parsed=KeyFramesAdapter.GEO_GSON.fromJson(animationJson,BakedAnimations.class);
                if(parsed.animations().size()!=animationJson.size())throw new AssertionError(name+" dropped animations");
                models++;animations+=parsed.animations().size();
            }
        }
        if(models!=8 || animations!=48)throw new AssertionError("Unexpected model/animation count");
        System.out.println("PASS: actual GeckoLib loaders baked "+models+" models and "+animations+" animations; no GPU or game client tested");
    }
}
