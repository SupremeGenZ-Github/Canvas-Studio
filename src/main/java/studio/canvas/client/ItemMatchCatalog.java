package studio.canvas.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;

/** Indexes item sprites and block textures from the currently loaded resource packs. */
public final class ItemMatchCatalog {
 public record Source(String id, Resource texture, boolean block) {}
 private ItemMatchCatalog() {}
 public static CompletableFuture<List<ItemMatcher.Reference>> load(ResourceManager resources) {return load(resources,false);}
 public static CompletableFuture<List<ItemMatcher.Reference>> load(ResourceManager resources,boolean modded) {
  // Snapshot resource handles and registry IDs on the client thread. Decode off-thread.
  Map<Identifier,Resource> sprites=resources.listResources("textures/item",id->id.getPath().endsWith(".png"));
  Map<Identifier,Resource> blocks=resources.listResources("textures/block",id->id.getPath().endsWith(".png"));
  List<Source> sources=new ArrayList<>();
  for(Item item:BuiltInRegistries.ITEM){
   if(item==Items.AIR)continue;Identifier id=BuiltInRegistries.ITEM.getKey(item);
   if(!modded&&!id.getNamespace().equals("minecraft"))continue;
   if(sources.size()>=5000)break;
   Resource sprite=sprites.get(Identifier.fromNamespaceAndPath(id.getNamespace(),"textures/item/"+id.getPath()+".png"));
   if(sprite==null&&modded&&!id.getNamespace().equals("minecraft"))sprite=modelTexture(resources,id);
   if(sprite!=null){sources.add(new Source(id.toString(),sprite,false));continue;}
   if(item instanceof BlockItem block){
    Identifier bid=BuiltInRegistries.BLOCK.getKey(block.getBlock());
    for(String suffix:new String[]{"","_side","_top","_front"}){
     Resource texture=blocks.get(Identifier.fromNamespaceAndPath(bid.getNamespace(),"textures/block/"+bid.getPath()+suffix+".png"));
     if(texture!=null){sources.add(new Source(id.toString(),texture,true));break;}
    }
   }
  }
  return CompletableFuture.supplyAsync(()->{
   List<ItemMatcher.Reference> refs=new ArrayList<>();
   for(Source source:sources)try(var input=source.texture.open()){
    BufferedImage image=readTexture(input);if(image==null||image.getWidth()>1024||image.getHeight()>8192)continue;
    // Animated texture sheets: use the first square frame.
    if(image.getHeight()>image.getWidth())image=image.getSubimage(0,0,image.getWidth(),image.getWidth());
    if(source.block){add(refs,source.id,image);add(refs,source.id,cube(image));}
    else {
     add(refs,source.id,image);
     // Also compare against how the sprite actually looks in the map-color canvas.
     // This preserves thin silhouettes after canvas resizing and palette conversion.
     BufferedImage canvas=ImageImport.resize(image,false);
     int[] palette=canvasPalette();byte[] pixels=ImageImport.quantize(canvas,palette);
     for(int p=0;p<pixels.length;p++)canvas.setRGB(p%128,p/128,palette[Byte.toUnsignedInt(pixels[p])]);
     var descriptor=ItemMatcher.describe(canvas,true);
     if(descriptor!=null)refs.add(new ItemMatcher.Reference(source.id,descriptor));
    }
   }catch(java.io.IOException|RuntimeException ignored){ /* Skip unsupported pack assets. */ }
   return List.copyOf(refs);
  });
 }
 private static Resource modelTexture(ResourceManager manager,Identifier id){
  try{
   var item=manager.getResource(Identifier.fromNamespaceAndPath(id.getNamespace(),"items/"+id.getPath()+".json"));if(item.isEmpty())return null;
   var definition=json(item.get()).getAsJsonObject("model");if(definition==null||!"minecraft:model".equals(definition.get("type").getAsString()))return null;
   Identifier model=Identifier.parse(definition.get("model").getAsString());java.util.Map<String,String> textures=new java.util.HashMap<>();
   for(int depth=0;depth<8;depth++){
    var resource=manager.getResource(Identifier.fromNamespaceAndPath(model.getNamespace(),"models/"+model.getPath()+".json"));if(resource.isEmpty())break;var object=json(resource.get());
    if(object.has("textures"))for(var entry:object.getAsJsonObject("textures").entrySet())textures.putIfAbsent(entry.getKey(),entry.getValue().getAsString());
    if(!object.has("parent"))break;model=Identifier.parse(object.get("parent").getAsString());
   }
   String texture=null;for(String key:new String[]{"layer0","all","side","front","particle"})if(textures.containsKey(key)){texture=textures.get(key);break;}
   for(int i=0;i<8&&texture!=null&&texture.startsWith("#");i++)texture=textures.get(texture.substring(1));
   if(texture==null||texture.startsWith("#"))return null;Identifier loc=Identifier.parse(texture);return manager.getResource(Identifier.fromNamespaceAndPath(loc.getNamespace(),"textures/"+loc.getPath()+".png")).orElse(null);
  }catch(Exception e){return null;}
 }
 private static com.google.gson.JsonObject json(Resource resource)throws java.io.IOException{try(var input=resource.open()){byte[] b=input.readNBytes(65537);if(b.length>65536)throw new java.io.IOException("Model metadata too large");return com.google.gson.JsonParser.parseString(new String(b,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();}}
 private static BufferedImage readTexture(java.io.InputStream stream)throws java.io.IOException{
  byte[] bytes=stream.readNBytes(4*1024*1024+1);if(bytes.length>4*1024*1024)return null;
  try(var input=ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes))){var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())return null;var reader=readers.next();try{reader.setInput(input);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||w>1024||h>8192)return null;return reader.read(0);}finally{reader.dispose();}}
 }
 private static int[] canvasPalette(){
  int[] palette=new int[248];for(int i=4;i<248;i++)palette[i]=net.minecraft.world.level.material.MapColor.getColorFromPackedId(i);return palette;
 }
 private static void add(List<ItemMatcher.Reference> refs,String id,BufferedImage image){var d=ItemMatcher.describe(image,false);if(d!=null)refs.add(new ItemMatcher.Reference(id,d));}
 /** Texture-based cube reference for inventory blocks, without a GPU readback. */
 public static BufferedImage cube(BufferedImage texture){
  BufferedImage image=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
  double w=texture.getWidth(),h=texture.getHeight();
  face(g,texture,new AffineTransform(26/w,13/w,-26/h,13/h,32,3),0);
  face(g,texture,new AffineTransform(26/w,13/w,0,30/h,6,16),.18f);
  face(g,texture,new AffineTransform(26/w,-13/w,0,30/h,32,29),.32f);
  g.dispose();return image;
 }
 private static void face(Graphics2D g,BufferedImage texture,AffineTransform transform,float shade){
  Graphics2D face=(Graphics2D)g.create();face.transform(transform);face.drawImage(texture,0,0,null);
  if(shade>0){face.setColor(new Color(0,0,0,shade));face.fillRect(0,0,texture.getWidth(),texture.getHeight());}face.dispose();
 }
}
