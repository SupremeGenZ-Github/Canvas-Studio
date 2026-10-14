import studio.canvas.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.RegistryAccess;
import io.netty.buffer.Unpooled;
import com.mojang.serialization.JsonOps;
import java.util.*;
import java.nio.file.*;
import java.io.*;
public class PaintingAccessChecks {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void reject(Runnable r,String why){try{r.run();throw new AssertionError(why);}catch(IllegalArgumentException expected){}}
 public static void main(String[] args)throws Exception{
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  var defaults=DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build();BuiltInRegistries.ITEM.listElements().forEach(h->h.bindComponents(defaults));
  UUID owner=UUID.randomUUID(),stranger=UUID.randomUUID();
  for(int frame=0;frame<12;frame++)for(boolean admin:new boolean[]{false,true}){
   var properties=new PaintingProperties(owner,new PaintOptions(frame,2,40,true),7);var item=new ItemStack(Items.FILLED_MAP);
   item.set(PaintingProperties.TYPE,properties);item.set(PaintingSize.TYPE,new PaintingSize(8,3));item.set(PaintingAnimation.TYPE,new PaintingAnimation(List.of(42,43,44)));
   item.set(DataComponents.MAP_ID,new MapId(43));item.set(DataComponents.CUSTOM_NAME,Component.literal("Saved art"));item.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Original lore"))));
   byte[] pixels=new byte[ArtBundle.PIXELS];Arrays.fill(pixels,(byte)34);pixels[17]=116;
   var art=new ArtBundle("Saved art",new PaintingSize(8,3),properties.options(),List.of(pixels,pixels,pixels));var store=new StudioStore(Files.createTempDirectory("unlock-art"));store.saveMap(42,art);
   reject(()->properties.changeLock(stranger,false,false),"Non-owner cannot unlock locked art");
   var unlocked=PaintingAccess.change(item,admin?stranger:owner,admin,false,changed->store.saveMap(42,new ArtBundle(art.title(),art.size(),changed.options(),art.frames())));
   var changed=unlocked.get(PaintingProperties.TYPE);
   check(changed.owner().equals(owner)&&changed.revision()==8&&!changed.options().locked(),"Unlock keeps owner and advances revision");
   check(changed.options().frame()==frame&&changed.options().fit()==2&&changed.options().delay()==40,"Unlock preserves material/layout/animation speed");
   check(item.get(PaintingProperties.TYPE).equals(properties),"Original stack remains intact for atomic replacement");
   check(unlocked.get(PaintingSize.TYPE).equals(item.get(PaintingSize.TYPE))&&unlocked.get(PaintingAnimation.TYPE).equals(item.get(PaintingAnimation.TYPE))&&unlocked.get(DataComponents.MAP_ID).equals(item.get(DataComponents.MAP_ID)),"Size, all animation IDs and current map preserved");
   check(unlocked.get(DataComponents.CUSTOM_NAME).equals(item.get(DataComponents.CUSTOM_NAME))&&unlocked.get(DataComponents.LORE).equals(item.get(DataComponents.LORE)),"Name and unrelated metadata preserved by access transaction");
   var reloaded=store.readMap(42).orElseThrow();check(!reloaded.options().locked()&&reloaded.frames().size()==3&&Arrays.equals(reloaded.frames().get(0),pixels),"Unlock persists existing source pixels without new maps");
   check(changed.mayEdit(owner,false)&&changed.mayEdit(stranger,false),"Unlock permits public editing");reject(()->changed.changeLock(stranger,false,true),"Public editor cannot relock someone else's artwork");
   var relocked=PaintingAccess.change(unlocked,owner,false,true,p->{});check(relocked.get(PaintingProperties.TYPE).options().locked()&&relocked.get(PaintingProperties.TYPE).revision()==9,"Owner can relock after editing");
   check(properties.mayEdit(owner,false)&&properties.mayEdit(stranger,true)&&!properties.mayEdit(stranger,false),"Locked art remains owner/admin editable");
   try{PaintingAccess.change(item,owner,false,false,p->{throw new IOException("Simulated write failure");});throw new AssertionError("Failed unlock accepted");}catch(IOException expected){}
   check(item.get(PaintingProperties.TYPE).equals(properties),"Persistence failure leaves lock and painting unchanged");
   check(changed.equals(PaintingProperties.CODEC.parse(JsonOps.INSTANCE,PaintingProperties.CODEC.encodeStart(JsonOps.INSTANCE,changed).getOrThrow()).getOrThrow()),"New access state survives item persistence");
  }
  var legacy=PaintingProperties.DEFAULT.changeLock(owner,false,true);check(legacy.owner().equals(owner)&&legacy.options().locked(),"Unowned legacy painting claims its first locking owner");
  var limit=new PaintingProperties(owner,PaintOptions.DEFAULT,Integer.MAX_VALUE);reject(()->limit.changeLock(owner,false,true),"Revision overflow rejected");check(limit.changeLock(owner,false,false)==limit,"No-op retains identity/revision");
  for(var hand:InteractionHand.values())for(boolean wall:new boolean[]{false,true}){
   UUID request=UUID.randomUUID(),token=UUID.randomUUID();var packet=new PaintingLock(request,hand,token,wall,false);var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
   try{PaintingLock.CODEC.encode(b,packet);check(PaintingLock.CODEC.decode(b).equals(packet)&&b.readableBytes()==0,"Lock packet binds both hands/wall target/token");}finally{b.release();}
   var old=new StudioSessions.Session(token,hand,null,null,42,7,200);var fresh=new StudioSessions.Session(UUID.randomUUID(),hand,null,null,42,8,200);
   check(!old.matches(token,hand,42,8,100)&&!fresh.matches(token,hand,42,8,100),"Stale revision/token cannot replay unlock");
  }
  System.out.println("PASS saved painting access: owner/admin unlock, public edit vs owner-only relock, kept-lock editing, immutable replacement, persistence failure, preserved art/animation/size/frame/maps, source reload, legacy claiming, revision overflow, both-hand/wall packet codecs and stale capabilities.");
 }
}
