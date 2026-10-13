package studio.canvas.client;
import studio.canvas.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import java.util.*;
public final class ServerGalleryScreen extends Screen {
 final CanvasScreen parent;private UUID request,loadRequest,selected;private int page,total;private List<StudioStore.Entry> entries=List.of();private String status="Loading server gallery...";private boolean requested;
 ServerGalleryScreen(CanvasScreen p){super(Component.literal("Server art gallery"));parent=p;}
 private void send(int action,UUID entry){request=UUID.randomUUID();if(action==GalleryRequest.LOAD)loadRequest=request;status=action==GalleryRequest.LOAD?"Importing artwork...":"Loading gallery...";ClientPacketDistributor.sendToServer(new GalleryRequest(request,action,page,entry));}
 boolean expects(UUID id){return id.equals(loadRequest);}
 public static void receive(GalleryPage p){var mc=Minecraft.getInstance();if(!(mc.gui.screen() instanceof ServerGalleryScreen s)||!p.request().equals(s.request))return;s.page=p.page();s.total=p.total();s.entries=p.entries();s.status=p.message();s.clearWidgets();s.init();}
 void reply(CanvasReply p){parent.reply(p);status=p.message();if(p.code()==5)send(GalleryRequest.LIST,CanvasReply.NONE);}
 private void button(int x,int y,int w,String label,Runnable action){addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,20).build());}
 @Override protected void init(){
  int left=width/2-148;
  button(left,30,94,"Publish current",()->{parent.publishGallery();status="Publishing current artwork...";});button(left+100,30,94,"Import selected",()->{if(selected!=null)send(GalleryRequest.LOAD,selected);});button(left+200,30,94,"Delete selected",()->{if(selected!=null)send(GalleryRequest.DELETE,selected);});
  for(int i=0;i<entries.size();i++){var e=entries.get(i);String text=(e.id().equals(selected)?"> ":"")+e.title()+" | "+e.author()+" | "+e.width()+"x"+e.height()+" | "+e.frames()+"f";button(left,56+i*22,294,font.plainSubstrByWidth(text,284),()->{selected=e.id();clearWidgets();init();});}
  button(left,height-28,70,"Previous",()->{if(page>0){page--;send(GalleryRequest.LIST,CanvasReply.NONE);}});button(width/2-70,height-28,140,"Back to drawing",this::onClose);button(left+224,height-28,70,"Next",()->{if((page+1)*GalleryPage.PAGE_SIZE<total){page++;send(GalleryRequest.LIST,CanvasReply.NONE);}});
  if(!requested){requested=true;send(GalleryRequest.LIST,CanvasReply.NONE);}
 }
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"SERVER ART GALLERY - page "+(page+1),width/2,12,0xfff5ddaa);g.text(font,font.plainSubstrByWidth(status,width-16),8,height-46,0xffbec8d8,false);super.extractRenderState(g,x,y,tick);}
}
