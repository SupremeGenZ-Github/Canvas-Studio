package studio.canvas.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.server.packs.resources.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/** Read paintings from the active registry and resource packs; no Mojang artwork is bundled. */
public final class MinecraftPaintingsScreen extends Screen {
 private record Entry(Identifier asset,String title,int width,int height,Resource resource){}
 private final CanvasScreen parent;private final List<Entry> all=new ArrayList<>();private List<Entry> shown=List.of();private int page;private EditBox search;private String query="",status="Choose a painting to import into the active layer.";private boolean busy;
 MinecraftPaintingsScreen(CanvasScreen p){super(Component.literal("Import Minecraft paintings"));parent=p;
  var mc=net.minecraft.client.Minecraft.getInstance();if(mc.level==null)return;
  var registry=mc.level.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT);
  registry.listElements().forEach(holder->{var v=holder.value();var asset=v.assetId();var texture=Identifier.fromNamespaceAndPath(asset.getNamespace(),"textures/painting/"+asset.getPath()+".png");mc.getResourceManager().getResource(texture).ifPresent(r->all.add(new Entry(asset,v.title().map(Component::getString).orElse(asset.getPath().replace('_',' ')),v.width(),v.height(),r)));});
  all.sort(Comparator.comparing(e->e.asset.toString()));filter();
 }
 private void filter(){String q=query.toLowerCase(Locale.ROOT);shown=all.stream().filter(e->e.title.toLowerCase(Locale.ROOT).contains(q)||e.asset.toString().contains(q)).toList();page=Math.min(page,Math.max(0,(shown.size()-1)/6));}
 private void button(int x,int y,int w,String label,Runnable action){addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,20).build());}
 @Override protected void init(){
  int left=width/2-148;search=new EditBox(font,left,30,216,18,Component.literal("Search Minecraft paintings"));search.setMaxLength(64);search.setValue(query);search.setResponder(s->query=s);addRenderableWidget(search);button(left+222,30,72,"Search",()->{page=0;filter();clearWidgets();init();});
  for(int i=0;i<6&&page*6+i<shown.size();i++){var e=shown.get(page*6+i);button(left,56+i*22,294,font.plainSubstrByWidth(e.title+" | "+e.width+"x"+e.height+" | "+e.asset,284),()->importPainting(e));}
  button(left,height-28,70,"Previous",()->{if(page>0){page--;clearWidgets();init();}});button(width/2-70,height-28,140,"Back to drawing",this::onClose);button(left+224,height-28,70,"Next",()->{if((page+1)*6<shown.size()){page++;clearWidgets();init();}});
 }
 public void importExisting(Identifier asset){var entry=all.stream().filter(e->e.asset.equals(asset)).findFirst();if(entry.isPresent())importPainting(entry.get());else status="This painting texture is unavailable in the active resource packs.";}
 private void importPainting(Entry entry){
  if(busy)return;busy=true;status="Loading "+entry.title+"...";
  CompletableFuture.supplyAsync(()->{try(var stream=entry.resource.open()){return PaintingImport.read(stream);}catch(Exception e){throw new java.util.concurrent.CompletionException(e);}}).whenComplete((image,error)->minecraft.execute(()->{
   busy=false;if(error!=null){status="Import failed: "+error.getCause().getMessage();return;}
   var resized=PaintingImport.square(image);
   var d=parent.document;d.begin();byte[] pixels=ImageImport.quantize(resized,parent.palette);System.arraycopy(pixels,0,d.pixels(),0,pixels.length);d.original=resized.getRGB(0,0,128,128,null,0,128);d.title=entry.title.replaceAll("[\\p{Cntrl}§]","");if(d.title.length()>64)d.title=d.title.substring(0,64);d.wallWidth=Math.max(1,Math.min(8,entry.width));d.wallHeight=Math.max(1,Math.min(8,entry.height));d.fitMode=0;d.commit();parent.syncPixels();parent.titleBoxReset();parent.status="Imported "+entry.asset+". Save Painting to create your editable copy.";minecraft.setScreenAndShow(parent);
  }));
 }
 @Override public void onClose(){if(!busy)minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"MINECRAFT PAINTINGS - "+shown.size()+" available",width/2,12,0xfff5ddaa);g.text(font,font.plainSubstrByWidth(status,width-16),8,height-46,0xffbec8d8,false);super.extractRenderState(g,x,y,tick);}
}
