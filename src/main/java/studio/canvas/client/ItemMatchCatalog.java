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
 public static CompletableFuture<List<ItemMatcher.Reference>> load(ResourceManager resources) {
  // Snapshot resource handles and registry IDs on the client thread. Decode off-thread.
  Map<Identifier,Resource> sprites=resources.listResources("textures/item",id->id.getPath().endsWith(".png"));
  Map<Identifier,Resource> blocks=resources.listResources("textures/block",id->id.getPath().endsWith(".png"));
  List<Source> sources=new ArrayList<>();
  for(Item item:BuiltInRegistries.ITEM){
   if(item==Items.AIR)continue;Identifier id=BuiltInRegistries.ITEM.getKey(item);
   if(!id.getNamespace().equals("minecraft"))continue;
   Resource sprite=sprites.get(Identifier.fromNamespaceAndPath(id.getNamespace(),"textures/item/"+id.getPath()+".png"));
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
    BufferedImage image=ImageIO.read(input);if(image==null||image.getWidth()>1024||image.getHeight()>8192)continue;
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
