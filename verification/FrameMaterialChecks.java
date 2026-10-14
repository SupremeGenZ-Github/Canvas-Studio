import studio.canvas.*;
import studio.canvas.client.art.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.RegistryAccess;
import io.netty.buffer.Unpooled;
import com.mojang.serialization.JsonOps;
import java.util.*;
import java.io.*;
public class FrameMaterialChecks {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  var defaults=DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build();
  BuiltInRegistries.ITEM.listElements().forEach(h->h.bindComponents(defaults));
  // Registry tag binding is package-private; install isolated vanilla tag fixtures.
  var bindTags=Items.OAK_PLANKS.builtInRegistryHolder().getClass().getDeclaredMethod("bindTags",Collection.class);bindTags.setAccessible(true);
  bindTags.invoke(Items.OAK_PLANKS.builtInRegistryHolder(),List.of(ItemTags.PLANKS));
  bindTags.invoke(Items.BAMBOO_PLANKS.builtInRegistryHolder(),List.of(ItemTags.PLANKS));
  Item[] material={Items.AIR,Items.OAK_PLANKS,Items.GOLD_INGOT,Items.IRON_INGOT,Items.DIAMOND,Items.STONE,Items.COPPER_INGOT,Items.AMETHYST_SHARD,Items.LAPIS_LAZULI,Items.EMERALD,Items.QUARTZ,Items.NETHERITE_INGOT};
  var colors=new HashSet<Integer>();var order=new HashSet<Integer>();int next=0;
  for(int n=0;n<12;n++){check(order.add(next),"Cycle visits each style once");next=FrameMaterials.next(next);}
  check(next==0&&order.size()==12,"All eleven materials plus None reachable");
  check(Arrays.equals(Arrays.copyOf(FrameMaterials.NAMES,5),new String[]{"None","Wood","Gold","Iron","Diamond"}),"Legacy IDs retain names");
  int[] legacy={0,0xff976239,0xffe8bc35,0xffbbc3ce,0xff3acac4};for(int f=0;f<5;f++)check(FrameMaterials.color(f)==legacy[f],"Legacy appearance unchanged");
  for(int f=0;f<12;f++){
   var options=new PaintOptions(f,2,20,true);check(PaintOptions.CODEC.parse(JsonOps.INSTANCE,PaintOptions.CODEC.encodeStart(JsonOps.INSTANCE,options).getOrThrow()).getOrThrow().equals(options),"All materials persist");
   var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);try{options.write(buffer);check(PaintOptions.read(buffer).equals(options),"All material packet IDs roundtrip");}finally{buffer.release();}
   var document=new Artwork();document.frameStyle=f;check(ProjectCodec.decode(ProjectCodec.encode(document)).frameStyle==f,"CSP preserves selected material");
   var art=new ArtBundle("Framed",new PaintingSize(8,8),options,List.of(Artwork.white()));check(ArtBundle.decode(art.encode()).options().frame()==f,"Gallery/source keeps frame choice");
   if(f==0)continue;
   check(colors.add(FrameMaterials.color(f))&&(FrameMaterials.color(f)>>>24)==255,"Distinct opaque material colors");
   check(FrameMaterials.matches(f,new ItemStack(material[f])),"Correct material accepted");
   check(!FrameMaterials.matches(f,ItemStack.EMPTY)&&!FrameMaterials.matches(f,new ItemStack(Items.PAPER)),"Empty/wrong item rejected");
   for(int other=1;other<12;other++)check(FrameMaterials.matches(f,new ItemStack(material[other]))==(f==other),"Materials cannot substitute for one another");
   for(int slot:new int[]{0,18,35,40}){
    var inventory=new SimpleContainer(41);inventory.setItem(slot,new ItemStack(material[f],3));int[] saves={0};
    final int frame=f;
    FrameMaterials.save(0,frame,inventory,()->saves[0]++);
    check(saves[0]==1&&inventory.getItem(slot).getCount()==2,"Exactly one item consumed from hotbar/main/off-hand");
    FrameMaterials.save(frame,frame,inventory,()->saves[0]++);check(saves[0]==2&&inventory.getItem(slot).getCount()==2,"Same-style edit/resize does not charge twice");
    FrameMaterials.save(frame,0,inventory,()->saves[0]++);check(saves[0]==3&&inventory.getItem(slot).getCount()==2,"Removal is free without refund");
    FrameMaterials.save(0,frame,inventory,()->saves[0]++);check(saves[0]==4&&inventory.getItem(slot).getCount()==1,"Reapplying after removal costs again");
    try{FrameMaterials.save(0,frame,inventory,()->{throw new IOException("Simulated persistence failure");});throw new AssertionError("Failed save accepted");}catch(IOException expected){}
    check(inventory.getItem(slot).getCount()==1,"Persistence failure preserves material");
    FrameMaterials.save(frame==1?2:1,frame,inventory,()->saves[0]++);check(inventory.getItem(slot).isEmpty(),"Changing frame consumes its new material");
    try{FrameMaterials.save(0,frame,inventory,()->saves[0]++);throw new AssertionError("Missing material accepted");}catch(IOException expected){check(expected.getMessage().contains(FrameMaterials.material(frame)),"Missing material explanation");}
    check(saves[0]==5,"Missing material rejects before creating maps or mutating painting");
    FrameMaterials.save(frame,frame,inventory,()->saves[0]++);check(saves[0]==6,"Legacy frame can be retained without inventory material");
   }
  }
  for(int f=1;f<12;f++){
   var main=new SimpleContainer(36);var offHand=new ItemStack(material[f],2);final int frame=f;
   check(FrameMaterials.has(main,offHand,frame),"Explicit off-hand fallback is available");
   FrameMaterials.save(0,frame,main,offHand,()->{});check(offHand.getCount()==1,"Off-hand payment with main-only inventory");
   main.setItem(0,new ItemStack(material[f],2));FrameMaterials.save(0,frame,main,offHand,()->{});check(main.getItem(0).getCount()==1&&offHand.getCount()==1,"Only one region pays even when both contain materials");
  }
  check(FrameMaterials.matches(1,new ItemStack(Items.BAMBOO_PLANKS)),"Wood uses the planks tag");
  check(!FrameMaterials.matches(1,new ItemStack(Items.OAK_LOG))&&!FrameMaterials.matches(5,new ItemStack(Items.COBBLESTONE))&&!FrameMaterials.matches(11,new ItemStack(Items.NETHERITE_SCRAP)),"Similar but incorrect resources rejected");
  for(int old=0;old<12;old++)for(int wanted=0;wanted<12;wanted++)check(FrameMaterials.costsMaterial(old,wanted)==(wanted!=0&&old!=wanted),"Every old/new material transition");
  for(int invalid:new int[]{-1,12,255})try{new PaintOptions(invalid,0,20,false);throw new AssertionError("Invalid frame accepted");}catch(IllegalArgumentException expected){}
  System.out.println("PASS material frames: all eleven resources/tag matching, all transitions and inventory regions, exact one-item cost, missing/wrong material, save-failure retention, free same-frame edits/removal, legacy names/colors, distinct styles, persistent/network/CSP/gallery codecs.");
 }
}
