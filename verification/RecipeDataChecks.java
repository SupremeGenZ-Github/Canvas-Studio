import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.advancements.Advancement;
import java.nio.file.*;
import java.util.stream.Stream;

/** Exercises the game codecs, including the old-format regression from the user's log. */
public class RecipeDataChecks {
 public static void main(String[] args) throws Exception {
  SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
  var recipes=new MappedRegistry<Recipe<?>>(Registries.RECIPE,Lifecycle.stable());
  var lookup=HolderLookup.Provider.create(Stream.concat(BuiltInRegistries.REGISTRY.stream(),Stream.of(recipes)));
  var ops=lookup.createSerializationContext(JsonOps.INSTANCE);
  Path root=Path.of(args.length==0?"src/main/resources/data/canvasstudio":args[0]);
  for(String name:new String[]{"canvas","canvas_with_feather","quill"}) {
   // Recipes use vanilla stand-in items because standalone bootstrap freezes items.
   var json=JsonParser.parseString(Files.readString(root.resolve("recipe/"+name+".json")).replace("canvasstudio:canvas","minecraft:brick").replace("canvasstudio:quill","minecraft:stick"));
   var recipe=Recipe.DIRECT_CODEC.parse(ops,json).getOrThrow();
   Registry.register(recipes,Identifier.fromNamespaceAndPath("canvasstudio",name),recipe);
  }
  recipes.freeze();
  for(String name:new String[]{"canvas","canvas_with_feather","quill"}) {
   var json=JsonParser.parseString(Files.readString(root.resolve("advancement/recipes/"+name+".json"))).getAsJsonObject();
   Advancement.CODEC.parse(ops,json).getOrThrow();
   var old=json.deepCopy();
   var conditions=old.getAsJsonObject("criteria").getAsJsonObject("has_the_recipe").getAsJsonObject("conditions");
   conditions.remove("recipes");conditions.addProperty("recipe","canvasstudio:"+name);
   if(Advancement.CODEC.parse(ops,old).isSuccess())throw new AssertionError("Old schema unexpectedly accepted");
   System.out.println("PASS game codecs + old-schema rejection: "+name);
  }
 }
}
