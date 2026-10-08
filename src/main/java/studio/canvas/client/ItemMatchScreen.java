package studio.canvas.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import studio.canvas.CanvasStudio;
import studio.canvas.DrawItem;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ItemMatchScreen extends Screen {
 private final CanvasScreen parent;
 private final InteractionHand hand;
 private final byte[] pixels;
 private final BufferedImage drawing;
 private List<ItemMatcher.Match> matches=List.of();
 private String status="Reading Minecraft item textures...";
 private boolean started, pending;
 private java.util.UUID submitted;
 private int x,y,rowHeight,panelWidth;
 public ItemMatchScreen(CanvasScreen parent,InteractionHand hand,byte[] pixels,int[] palette){
  super(Component.literal("Canvas Studio+ - Get Item"));this.parent=parent;this.hand=hand;this.pixels=pixels.clone();
  drawing=new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);
  for(int p=0;p<pixels.length;p++)drawing.setRGB(p%128,p/128,palette[Byte.toUnsignedInt(pixels[p])]);
 }
 @Override protected void init(){
  panelWidth=Math.min(320,width-16);x=(width-panelWidth)/2;y=58;rowHeight=Math.max(36,Math.min(52,(height-115)/3));
  for(int i=0;i<matches.size();i++){
   final ItemMatcher.Match match=matches.get(i);
   addRenderableWidget(Button.builder(Component.literal(i==0?"Get closest":"Get this"),b->receive(match)).bounds(x+panelWidth-94,y+i*rowHeight+8,88,20).build());
  }
  addRenderableWidget(Button.builder(Component.literal("Back to drawing"),b->onClose()).bounds(width/2-70,height-32,140,20).build());
  if(!started){started=true;
   if(!DrawItem.validDrawing(pixels)||ItemMatcher.describe(drawing,true)==null){status="Draw an item or block first.";return;}
   parent.matchReferences()
    .thenCompose(refs->{minecraft.execute(()->status="Finding the closest item or block...");return CompletableFuture.supplyAsync(()->ItemMatcher.match(drawing,refs,3));})
    .whenComplete((result,error)->minecraft.execute(()->{
     if(error!=null){status="Matching failed. Return to drawing and try again.";return;}
     matches=result;status=result.isEmpty()?"No supported item textures found.":result.getFirst().similarity()<.55?"No strong match. Try adding detail or color.":"Closest matches to your drawing";
     clearWidgets();init();
    }));
  }
 }
 private void receive(ItemMatcher.Match match){
  if(pending)return;
  if(parent.exchangeToken==null){status="Waiting for server. Reopen the canvas if needed.";return;}
  if(minecraft.player==null||CanvasStudio.tier(minecraft.player.getItemInHand(hand))!=parent.tier||!parent.tier.canExchange()){status="Hold your Molder or Infinity Canvas.";return;}
  pending=true;submitted=parent.exchangeToken;
  ClientPacketDistributor.sendToServer(new DrawItem(hand,match.id(),pixels.clone(),submitted));
  status="Waiting for server exchange...";
 }
 void reply(studio.canvas.CanvasReply reply){
  parent.reply(reply);
  if(!pending||!reply.request().equals(submitted))return;
  pending=false;status=reply.message();
  if(reply.code()==1){parent.clearDraftAfterExchange();minecraft.setScreenAndShow(null);}
  else if(reply.code()==2){parent.exchangeToken=reply.token();}
  else {parent.exchangeToken=null;}
 }
 @Override public void onClose(){if(!pending)minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float tick){
  g.fill(0,0,width,height,0xf0181c26);
  g.centeredText(font,"CANVAS STUDIO+ - DRAW TO ITEM",width/2,12,0xfff5ddaa);
  g.centeredText(font,font.plainSubstrByWidth(status,width-16),width/2,30,0xffffffff);
  for(int i=0;i<matches.size();i++){
   var match=matches.get(i);int ry=y+i*rowHeight;var item=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(match.id())));
   g.fill(x,ry,x+panelWidth,ry+rowHeight-3,i==0?0xff344a40:0xff252c38);
   g.item(item,x+8,ry+9);g.text(font,font.plainSubstrByWidth(item.getHoverName().getString(),Math.max(24,panelWidth-132)),x+30,ry+8,0xffffffff,false);
   g.text(font,"Similarity: "+Math.round(match.similarity()*100)+"%",x+30,ry+21,0xffbec8d8,false);
  }
  g.centeredText(font,parent.tier==studio.canvas.CanvasTier.MOLDER?"Consumes Molder Canvas on success. Receive 1 item.":"Infinity Canvas retained. Receive 1 item per request.",width/2,height-48,0xffbec8d8);
  super.extractRenderState(g,mx,my,tick);
 }
}
