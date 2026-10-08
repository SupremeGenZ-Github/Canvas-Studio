import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import java.util.*;
import net.minecraft.advancements.Advancement;
import java.nio.file.*;
import java.util.stream.Stream;

/** Exercises the game codecs, including the old-format regression from the user's log. */
public class RecipeDataChecks {
 static String standIns(String s){return s.replace("canvasstudio:molder_canvas","minecraft:iron_ingot").replace("canvasstudio:infinity_canvas","minecraft:diamond").replace("canvasstudio:molder_quill","minecraft:iron_nugget").replace("canvasstudio:infinity_quill","minecraft:diamond_sword").replace("canvasstudio:canvas","minecraft:brick").replace("canvasstudio:quill","minecraft:stick");}
 static void check(boolean v,String s){if(!v)throw new AssertionError(s);}
 public static void main(String[] args) throws Exception {
  SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
  // 26.3 normally binds item components during world registry loading. Minimal
  // stand-in defaults suffice for headless crafting; no world is loaded here.
  var defaults=net.minecraft.core.component.DataComponentMap.builder().set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE,64).build();
  BuiltInRegistries.ITEM.listElements().forEach(holder -> holder.bindComponents(defaults));
  var recipes=new MappedRegistry<Recipe<?>>(Registries.RECIPE,Lifecycle.stable());
  var lookup=HolderLookup.Provider.create(Stream.concat(BuiltInRegistries.REGISTRY.stream(),Stream.of(recipes)));
  var ops=lookup.createSerializationContext(JsonOps.INSTANCE);
  Path root=Path.of(args.length==0?"src/main/resources/data/canvasstudio":args[0]);
  for(String name:new String[]{"canvas","molder_canvas","infinity_canvas","quill","molder_quill","infinity_quill"}) {
   // Recipes use vanilla stand-in items because standalone bootstrap freezes items.
   var json=JsonParser.parseString(standIns(Files.readString(root.resolve("recipe/"+name+".json"))));
   var recipe=Recipe.DIRECT_CODEC.parse(ops,json).getOrThrow();
   var object=json.getAsJsonObject();
   List<ItemStack> grid=new ArrayList<>();
   if(recipe instanceof ShapedRecipe shaped){
    check(shaped.getWidth()==3&&shaped.getHeight()==3,"Quill requires 3x3");
    var key=object.getAsJsonObject("key");
    for(var row:object.getAsJsonArray("pattern"))for(char c:row.getAsString().toCharArray())grid.add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(key.get(String.valueOf(c)).getAsString()))));
    var input=CraftingInput.of(3,3,grid);check(shaped.matches(input,null),"Shaped craft "+name);
    check(shaped.assemble(input).getCount()==1,"One quill output");
    for(int slot=0;slot<9;slot++){var missing=new ArrayList<>(grid);missing.set(slot,ItemStack.EMPTY);check(!shaped.matches(CraftingInput.of(3,3,missing),null),"All nine slots required "+name);}
    Collections.swap(grid,0,4);check(!shaped.matches(CraftingInput.of(3,3,grid),null),"Quill must be centered");
   }else if(recipe instanceof ShapelessRecipe shapeless){
    for(var ingredient:object.getAsJsonArray("ingredients"))grid.add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(ingredient.getAsString()))));
    while(grid.size()<9)grid.add(ItemStack.EMPTY);
    for(int rotation=0;rotation<9;rotation++){Collections.rotate(grid,1);var input=CraftingInput.of(3,3,grid);check(shapeless.matches(input,null),"Shapeless placement "+name);check(shapeless.assemble(input).getCount()==1,"One crafted output");}
    if(name.endsWith("canvas")||name.equals("canvas")){
     var shortcut=CraftingInput.of(3,1,List.of(new ItemStack(Items.LEATHER),new ItemStack(Items.PAPER),new ItemStack(Items.FEATHER)));
     check(!shapeless.matches(shortcut,null),"Feather shortcut rejected");
    }
   }else throw new AssertionError("Unexpected recipe type");
   Registry.register(recipes,Identifier.fromNamespaceAndPath("canvasstudio",name),recipe);
  }
  check(!Files.exists(root.resolve("recipe/canvas_with_feather.json"))&&!Files.exists(root.resolve("advancement/recipes/canvas_with_feather.json")),"Shortcut recipe and advancement removed");
  recipes.freeze();
  for(String name:new String[]{"canvas","molder_canvas","infinity_canvas","quill","molder_quill","infinity_quill"}) {
   var json=JsonParser.parseString(Files.readString(root.resolve("advancement/recipes/"+name+".json"))).getAsJsonObject();
   var material=json.getAsJsonObject("criteria").getAsJsonObject("has_material").getAsJsonObject("conditions").getAsJsonArray("items").get(0).getAsJsonObject();
   material.addProperty("items",standIns(material.get("items").getAsString()));
   Advancement.CODEC.parse(ops,json).getOrThrow();
   var old=json.deepCopy();
   var conditions=old.getAsJsonObject("criteria").getAsJsonObject("has_the_recipe").getAsJsonObject("conditions");
   conditions.remove("recipes");conditions.addProperty("recipe","canvasstudio:"+name);
   if(Advancement.CODEC.parse(ops,old).isSuccess())throw new AssertionError("Old schema unexpectedly accepted");
   System.out.println("PASS game codecs + old-schema rejection: "+name);
  }
 }
}
