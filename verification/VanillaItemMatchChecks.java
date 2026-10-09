import studio.canvas.client.ItemMatchCatalog;
import studio.canvas.client.ItemMatcher;
import studio.canvas.client.ImageImport;
import studio.canvas.DrawItem;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.level.material.MapColor;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class VanillaItemMatchChecks {
 static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 public static void main(String[] args)throws Exception{
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  check(DrawItem.validItem("minecraft:diamond_sword"),"Vanilla item accepted");check(DrawItem.validItem("minecraft:stone"),"Block item accepted");
  for(String bad:new String[]{"minecraft:air","minecraft:missing_item","othermod:diamond","invalid id"})check(!DrawItem.validItem(bad),"Bad item rejected: "+bad);
  check(!DrawItem.validItem(null),"Null item rejected");
  int[] palette=new int[248];for(int i=4;i<248;i++)palette[i]=MapColor.getColorFromPackedId(i);
  check(Byte.toUnsignedInt(ImageImport.nearest(0xffffff,palette))==34,"Exchange blank canvas color agrees with editor");
  try(var jar=new ZipFile("build/vanilla-validation/minecraft-26.2.jar")){
   Map<Identifier,Resource> assets=new HashMap<>();var entries=jar.entries();
   while(entries.hasMoreElements()){
    var e=entries.nextElement();String path=e.getName();
    if(path.startsWith("assets/minecraft/textures/")&&path.endsWith(".png")){
     var id=Identifier.fromNamespaceAndPath("minecraft",path.substring("assets/minecraft/".length()));
     assets.put(id,new Resource(null,()->jar.getInputStream(e)));
    }
   }
   ResourceManager resources=new ResourceManager(){
    public Set<String> getNamespaces(){return Set.of("minecraft");}
    public Optional<Resource> getResource(Identifier id){return Optional.ofNullable(assets.get(id));}
    public List<Resource> getResourceStack(Identifier id){return getResource(id).stream().toList();}
    public Map<Identifier,Resource> listResources(String path,java.util.function.Predicate<Identifier> selector){var out=new HashMap<Identifier,Resource>();assets.forEach((id,r)->{if(id.getPath().startsWith(path+"/")&&selector.test(id))out.put(id,r);});return out;}
    public Map<Identifier,List<Resource>> listResourceStacks(String path,java.util.function.Predicate<Identifier> selector){return Map.of();}
    public Stream<PackResources> listPacks(){return Stream.empty();}
   };
   var refs=ItemMatchCatalog.load(resources).get(90,TimeUnit.SECONDS);
   long itemCount=refs.stream().map(ItemMatcher.Reference::id).distinct().count();check(itemCount>300,"Substantial actual vanilla catalogue: "+itemCount);
   System.out.println("Vanilla texture catalogue: "+itemCount+" distinct inventory items, "+refs.size()+" references");
   int hits=0;
   for(String name:new String[]{"diamond","diamond_sword","apple","stick","bread","iron_pickaxe","golden_apple","emerald","bow"}){
    var entry=jar.getEntry("assets/minecraft/textures/item/"+name+".png");check(entry!=null,"Actual item asset: "+name);
    var image=ImageImport.resize(ImageIO.read(jar.getInputStream(entry)),false);byte[] pixels=ImageImport.quantize(image,palette);
    for(int p=0;p<pixels.length;p++)image.setRGB(p%128,p/128,palette[Byte.toUnsignedInt(pixels[p])]);
    var result=ItemMatcher.match(image,refs,3);boolean hit=result.stream().anyMatch(m->m.id().equals("minecraft:"+name));
    if(hit)hits++;System.out.println(name+" in top 3: "+hit+" -> "+result);
   }
   check(hits==9,"Actual quantized item sprite matching, hits="+hits+"/9");
   System.out.println("PASS actual vanilla asset indexing/matching and server item whitelist validation");
  }
 }
}
