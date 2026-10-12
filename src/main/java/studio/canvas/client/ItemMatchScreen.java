package studio.canvas.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import studio.canvas.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.*;
public final class ItemMatchScreen extends Screen {
 private final CanvasScreen parent;private final InteractionHand hand;private final byte[] pixels;private final BufferedImage drawing;
 private java.util.List<ItemMatcher.Reference> references=java.util.List.of();private java.util.List<ItemMatcher.Match> matches=java.util.List.of(),visible=java.util.List.of();
 private String status="Loading local catalogue...",query="";private boolean started,pending,loading,closed,browse;private UUID submitted;
 private int x,y,rowHeight,panelWidth,rows,page,limit=3,category,namespace,generation;
 private EditBox search;private FinderMemory memory=new FinderMemory();
 public ItemMatchScreen(CanvasScreen parent,InteractionHand hand,byte[] pixels,int[] palette){this(parent,hand,pixels,palette,false);}
 public ItemMatchScreen(CanvasScreen parent,InteractionHand hand,byte[] pixels,int[] palette,boolean browse){super(Component.literal("Canvas Studio+ catalogue"));this.parent=parent;this.hand=hand;this.pixels=pixels.clone();this.browse=browse;drawing=new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);for(int i=0;i<pixels.length;i++)drawing.setRGB(i%128,i/128,palette[Byte.toUnsignedInt(pixels[i])]);}
 private void button(int x,int y,int w,String label,Runnable action){var b=Button.builder(Component.literal(label),v->action.run()).bounds(x,y,w,18).build();b.active=!pending;addRenderableWidget(b);}
 @Override protected void init(){
  panelWidth=Math.min(390,width-16);x=(width-panelWidth)/2;y=88;rowHeight=36;rows=Math.max(1,(height-143)/rowHeight);
  search=new EditBox(font,x,25,panelWidth,18,Component.literal("Search names or item IDs"));search.setMaxLength(128);search.setValue(query);search.setResponder(value->{query=value;page=0;refresh();setFocused(search);search.setFocused(true);});addRenderableWidget(search);
  int w=(panelWidth-6)/3;button(x,47,w,browse?"Browse":"Visual matches",()->{browse=!browse;page=0;update();});button(x+w+3,47,w,new String[]{"All","Items","Blocks","Favorites","Recent"}[category],()->{category=(category+1)%5;page=0;refresh();});button(x+2*(w+3),47,w,new String[]{"Vanilla","Modded","All mods"}[namespace],()->{namespace=(namespace+1)%3;page=0;reload();});
  button(x,68,w,"Results: "+limit,()->{limit=limit==3?6:limit==6?12:limit==12?24:3;page=0;update();});button(x+w+3,68,w,"< Page "+(page+1),()->{page=Math.max(0,page-1);refresh();});button(x+2*(w+3),68,w,"Next >",()->{if((page+1)*rows<visible.size())page++;refresh();});
  filter();
  for(int i=0;i<rows;i++){int index=page*rows+i;if(index>=visible.size())break;var match=visible.get(index);final var chosen=match;button(x+panelWidth-28,y+i*rowHeight+7,24,memory.favorites.contains(match.id())?"*":"+",()->{memory.toggle(chosen.id());memory.save(minecraft.gameDirectory.toPath());refresh();});if(!browse&&parent.tier.canExchange())button(x+panelWidth-86,y+i*rowHeight+7,54,"Get",()->receive(chosen));}
  button(width/2-70,height-26,140,"Back to drawing",this::onClose);
  if(!started){started=true;var root=minecraft.gameDirectory.toPath();CompletableFuture.supplyAsync(()->FinderMemory.load(root),studio.canvas.client.art.ProjectStore.IO).thenAccept(m->minecraft.execute(()->{memory=m;if(!closed)refresh();}));reload();}
 }
 private void filter(){String q=query.toLowerCase(Locale.ROOT);visible=matches.stream().filter(m->{Identifier id=Identifier.parse(m.id());var item=BuiltInRegistries.ITEM.getValue(id);boolean block=item instanceof BlockItem;return(namespace==2||(namespace==0)==id.getNamespace().equals("minecraft"))&&(category==0||category==1&&!block||category==2&&block||category==3&&memory.favorites.contains(m.id())||category==4&&memory.recent.contains(m.id()))&&(m.id().toLowerCase(Locale.ROOT).contains(q)||item.getName().getString().toLowerCase(Locale.ROOT).contains(q));}).toList();}
 private void refresh(){if(closed)return;clearWidgets();init();}
 private void reload(){int request=++generation;loading=true;status="Reading supported textures...";ItemMatchCatalog.load(minecraft.getResourceManager(),namespace!=0).whenComplete((refs,error)->minecraft.execute(()->{if(closed||request!=generation)return;loading=false;if(error!=null){status="Catalogue load failed; reopen to retry.";return;}references=refs;update();}));}
 private void update(){if(loading)return;if(browse){matches=references.stream().map(ItemMatcher.Reference::id).distinct().sorted().map(id->new ItemMatcher.Match(id,0)).toList();status="Browse only. Draw to receive items; + marks a favorite.";refresh();return;}if(!DrawItem.validDrawing(pixels)||ItemMatcher.describe(drawing,true)==null){matches=java.util.List.of();status="Draw an item/block first, or switch to Browse.";refresh();return;}int request=++generation;loading=true;status="Finding visual matches...";var refs=references;int requestedLimit=limit;CompletableFuture.supplyAsync(()->ItemMatcher.match(drawing,refs,requestedLimit)).whenComplete((result,error)->minecraft.execute(()->{if(closed||request!=generation)return;loading=false;if(error!=null){status="Matching failed; reopen to retry.";return;}matches=result;memory.record(result);memory.save(minecraft.gameDirectory.toPath());status=result.isEmpty()?"No supported matches.":result.getFirst().similarity()<.55?"Weak match — try clearer shape or color.":"Approximate visual similarity; choose a result.";refresh();}));}
 private void receive(ItemMatcher.Match match){if(pending||loading||browse)return;if(parent.exchangeToken==null){status="Reopen the held canvas to request a server session.";return;}if(minecraft.player==null||CanvasStudio.tier(minecraft.player.getItemInHand(hand))!=parent.tier){status="Hold the same Molten or Infinity Canvas.";return;}if(parent.tier==CanvasTier.INFINITY&&parent.maximum>0&&parent.uses>=parent.maximum){status="Infinity Canvas usage limit reached.";return;}pending=true;submitted=parent.exchangeToken;ClientPacketDistributor.sendToServer(new DrawItem(hand,match.id(),pixels.clone(),submitted));status="Waiting for server exchange...";refresh();}
 void reply(CanvasReply reply){parent.reply(reply);if(!pending||!reply.request().equals(submitted))return;pending=false;status=reply.message();if(reply.code()==1){parent.clearDraftAfterExchange();closed=true;minecraft.setScreenAndShow(null);}else{parent.exchangeToken=reply.code()==2?reply.token():null;refresh();}}
 @Override public void onClose(){if(!pending){closed=true;generation++;minecraft.setScreenAndShow(parent);}}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float tick){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"CANVAS STUDIO+ — OFFLINE CATALOGUE",width/2,9,0xfff5ddaa);for(int i=0;i<rows;i++){int index=page*rows+i;if(index>=visible.size())break;var match=visible.get(index);int ry=y+i*rowHeight;var item=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(match.id())));g.fill(x,ry,x+panelWidth,ry+rowHeight-2,0xff252c38);g.item(item,x+5,ry+8);g.text(font,font.plainSubstrByWidth(item.getHoverName().getString(),panelWidth-122),x+26,ry+6,0xffffffff,false);g.text(font,browse?font.plainSubstrByWidth(match.id(),panelWidth-122):"Visual similarity: "+Math.round(match.similarity()*100)+"%",x+26,ry+20,0xffbec8d8,false);}g.centeredText(font,font.plainSubstrByWidth(status,width-12),width/2,height-48,0xffbec8d8);super.extractRenderState(g,mx,my,tick);}
}
