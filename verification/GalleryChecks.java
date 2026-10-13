import studio.canvas.*;
import studio.canvas.client.art.*;
import studio.canvas.client.PaintingImport;
import studio.canvas.client.ImageImport;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import io.netty.buffer.Unpooled;
import com.mojang.serialization.JsonOps;
import com.google.gson.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.ZipFile;
public class GalleryChecks {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void reject(Runnable action,String why){try{action.run();throw new AssertionError(why);}catch(IllegalArgumentException expected){}}
 public static void main(String[] args)throws Exception{
  UUID owner=UUID.randomUUID(),stranger=UUID.randomUUID(),token=UUID.randomUUID();
  for(boolean locked:new boolean[]{false,true}){var properties=new PaintingProperties(owner,new PaintOptions(4,1,10,locked),17);check(properties.mayEdit(owner,false),"Owner edit");check(properties.mayEdit(stranger,true),"Admin edit");check(properties.mayEdit(stranger,false)==!locked,"Public/locked stranger edit");check(!properties.mayChangeLock(stranger,false)&&properties.mayChangeLock(owner,false)&&properties.mayChangeLock(stranger,true),"Lock authority");var encoded=PaintingProperties.CODEC.encodeStart(JsonOps.INSTANCE,properties).getOrThrow();check(properties.equals(PaintingProperties.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow()),"Owner/settings persistence");}
  check(PaintingProperties.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{\"owner\":\"broken\",\"options\":{\"frame\":0,\"fit\":0,\"delay\":20,\"locked\":false},\"revision\":0}")).error().isPresent(),"Malformed UUID is a codec error");
  var session=new StudioSessions.Session(token,InteractionHand.OFF_HAND,null,null,42,17,200);
  check(session.matches(token,InteractionHand.OFF_HAND,42,17,200),"Bound session");check(!session.matches(UUID.randomUUID(),InteractionHand.OFF_HAND,42,17,100),"Wrong token");check(!session.matches(token,InteractionHand.MAIN_HAND,42,17,100),"Wrong hand");check(!session.matches(token,InteractionHand.OFF_HAND,43,17,100),"Wrong painting");check(!session.matches(token,InteractionHand.OFF_HAND,42,18,100),"Stale revision");check(!session.matches(token,InteractionHand.OFF_HAND,42,17,201),"Expired edit");
  byte[] pixels=Artwork.white();pixels[64*128+64]=116;var assembly=new ArtAssembly(token,2,100);assembly.add(token,0,2,100,pixels);check(!assembly.complete(),"Incomplete does not save");reject(()->assembly.add(token,0,2,100,pixels),"Duplicate frame");reject(()->assembly.add(token,1,3,100,pixels),"Count changed");reject(()->assembly.add(token,1,2,301,pixels),"Expired upload");assembly.add(token,1,2,200,pixels);check(assembly.complete()&&assembly.frames().size()==2,"Complete transfer");reject(()->assembly.add(token,2,2,200,pixels),"Cannot append/replay completed transfer");
  var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);try{
   var packet=new ArtUpload(token,ArtUpload.WALL,InteractionHand.OFF_HAND,token,"Animated",new PaintingSize(8,4),new PaintOptions(3,2,40,true),0,16,pixels);ArtUpload.CODEC.encode(buffer,packet);check(buffer.readableBytes()<32767,"Serverbound payload below Minecraft limit");var decoded=ArtUpload.CODEC.decode(buffer);check(packet.sameHeader(decoded)&&Arrays.equals(decoded.pixels(),pixels)&&buffer.readableBytes()==0,"Upload codec");
   var down=new ArtDownload(token,ArtUpload.HELD,InteractionHand.MAIN_HAND,token,"Animated",new PaintingSize(3,8),new PaintOptions(2,1,10,false),1,2,pixels);ArtDownload.CODEC.encode(buffer,down);var copy=ArtDownload.CODEC.decode(buffer);check(copy.size().equals(down.size())&&copy.options().equals(down.options())&&copy.count()==2&&Arrays.equals(copy.pixels(),pixels),"Download codec");
   var animation=new PaintingAnimation(List.of(12,19,43));PaintingAnimation.STREAM.encode(buffer,animation);check(animation.equals(PaintingAnimation.STREAM.decode(buffer)),"Map sequence codec");check(animation.index(0,10)==0&&animation.index(10,10)==1&&animation.index(30,10)==0,"Animation clock and wrap");
   buffer.clear();buffer.writeByte(17);try{PaintingAnimation.STREAM.decode(buffer);throw new AssertionError("Unbounded maps");}catch(IllegalArgumentException expected){}
  }finally{buffer.release();}
  reject(()->new PaintOptions(5,0,20,false),"Unknown border");reject(()->new PaintOptions(0,3,20,false),"Unknown layout");reject(()->new PaintOptions(0,0,9,false),"Too-fast animation");reject(()->new ArtAssembly(token,17,0),"Too many frames");
  byte[] pattern=new byte[Artwork.PIXELS];for(int y=0;y<128;y++)for(int x=0;x<128;x++)pattern[y*128+x]=(byte)(4+x);
  for(int w=1;w<=8;w++)for(int h=1;h<=8;h++){var size=new PaintingSize(w,h);check(Arrays.equals(PaintLayout.apply(pattern,size,0),pattern),"Stretch preserves raster");for(int mode=1;mode<=2;mode++)ArtBundle.validatePixels(PaintLayout.apply(pattern,size,mode));}
  var fit=PaintLayout.apply(pattern,new PaintingSize(8,2),1);check(fit[0]==34&&fit[64*128+64]!=34,"Fit letterboxes and preserves center");var crop=PaintLayout.apply(pattern,new PaintingSize(2,8),2);check(Byte.toUnsignedInt(crop[0])>4&&Byte.toUnsignedInt(crop[127])<131,"Crop trims source edges");
  var d=new Artwork();d.addLayer();d.layers.get(1).name="a".repeat(64);d.duplicateLayer();check(d.layers.get(2).name.length()==64,"Duplicate names remain saveable");d.pixels()[9]=116;d.addFrame(false);d.pixels()[9]=(byte)172;d.selectFrame(0);check(d.pixels()[9]==116&&d.layers.size()==3,"Frame keeps editable layers");d.selectFrame(1);check(d.pixels()[9]==(byte)172,"Frame independence");d.deleteFrame();check(d.timeline.size()==1&&d.undo()&&d.timeline.size()==2,"Undo frame deletion");d.wallWidth=8;d.wallHeight=4;d.frameStyle=4;d.fitMode=2;d.frameDelay=40;d.locked=true;d.original=new int[Artwork.PIXELS];Arrays.fill(d.original,0xffabcdef);
  var encoded=ProjectCodec.encode(d);var loaded=ProjectCodec.decode(encoded);check(loaded.timeline.size()==2&&loaded.layers.size()==3&&loaded.frameStyle==4&&loaded.fitMode==2&&loaded.frameDelay==40&&loaded.locked&&loaded.wallWidth==8,"CSP4 animation/layers/display roundtrip");check(Arrays.equals(loaded.original,d.original),"Frame source preserved");
  for(int version=1;version<=3;version++){var old=ProjectCodec.decode(LegacyProjects.encode(d,version));check(old.imageSequence().size()==1,"Legacy project is one static frame");check(old.wallWidth==(version>=3?8:1),"Legacy size migration");}
  var art=new ArtBundle("Gallery",new PaintingSize(2,3),new PaintOptions(1,1,20,true),List.of(pixels,pixels));check(ArtBundle.decode(art.encode()).frames().size()==2,"Artwork file codec");try{ArtBundle.decode(Arrays.copyOf(art.encode(),10));throw new AssertionError("Truncated artwork");}catch(IOException expected){}
  Path root=Files.createTempDirectory("studio-gallery");var store=new StudioStore(root);store.saveMap(42,art);check(store.readMap(42).orElseThrow().size().equals(art.size()),"Raw source persistence");var entry=store.publish(owner,"Artist",art);check(new StudioStore(root).list().size()==1,"Gallery restart persistence");check(!store.delete(entry.id(),stranger,false),"Stranger cannot delete");check(store.read(entry.id()).frames().size()==2,"Published animation import");check(store.delete(entry.id(),stranger,true)&&store.list().isEmpty(),"Admin delete");
  Path victim=root.resolve("victim");Files.writeString(victim,"untouched");Path unsafe=root.resolve("canvasstudio/paintings/43.csa");try{Files.createSymbolicLink(unsafe,victim);try{store.saveMap(43,art);throw new AssertionError("Symlink write");}catch(IOException expected){}check(Files.readString(victim).equals("untouched"),"Symlink target preserved");}catch(UnsupportedOperationException expected){}
  for(int i=0;i<64;i++)store.publish(owner,"Artist",art);try{store.publish(owner,"Artist",art);throw new AssertionError("Gallery artist limit");}catch(IOException expected){}
  int imported=0;int[] palette=new int[248];for(int i=4;i<248;i++)palette[i]=net.minecraft.world.level.material.MapColor.getColorFromPackedId(i);
  try(var jar=new ZipFile(System.getProperty("minecraftValidationJar"))){var entries=jar.entries();while(entries.hasMoreElements()){var e=entries.nextElement();if(!e.getName().startsWith("data/minecraft/painting_variant/")||!e.getName().endsWith(".json"))continue;JsonObject json;try(var input=jar.getInputStream(e)){json=JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();}String id=json.get("asset_id").getAsString();String[] parts=id.split(":",2);String texture="assets/"+parts[0]+"/textures/painting/"+parts[1]+".png";var png=jar.getEntry(texture);check(png!=null,"Native painting texture exists: "+id);try(var input=jar.getInputStream(png)){var image=PaintingImport.read(input);var square=PaintingImport.square(image);ArtBundle.validatePixels(ImageImport.quantize(square,palette));check(square.getWidth()==128&&square.getHeight()==128,"Native painting conversion");}imported++;}}
  check(imported>=20,"All built-in painting variants exercised");
  System.out.println("PASS Living Gallery: ownership/admin locks, bound edit capabilities/revisions, chunked uploads/replay/limits, wire/persistent codecs, all layouts/sizes, animation/layers/CSP1-4, world/gallery restart and symlink safety; imported actual Minecraft painting textures="+imported);
 }
}
