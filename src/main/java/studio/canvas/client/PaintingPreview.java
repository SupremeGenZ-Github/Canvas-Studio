package studio.canvas.client;
import studio.canvas.client.art.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
/** 2D palette/frame comparison and six-slot wall composition; no world placement. */
public final class PaintingPreview extends Screen {
 private final CanvasScreen parent;private boolean wall,glow;private int source,sizeMode=1;private final byte[][] slots=new byte[6][];
 private java.util.List<ProjectStore.Entry> projects=java.util.List.of();private String status="2D preview; actual world lighting can differ.";
 PaintingPreview(CanvasScreen parent){super(Component.literal("Painting preview"));this.parent=parent;slots[0]=parent.document.flatten();}
 @Override protected void init(){int w=Math.min(100,(width-30)/3);button(width/2-w*3/2-4,height-52,w,wall?"Compare colors":"Wall layout",()->{wall=!wall;refresh();});button(width/2-w/2,height-52,w,glow?"Glowing frame":"Normal frame",()->{glow=!glow;refresh();});button(width/2+w/2+4,height-52,w,"Size: "+sizeMode,()->{sizeMode=sizeMode==3?1:sizeMode+1;refresh();});button(width/2-145,height-28,180,source==0?"Source: current drawing":"Source: project "+source,()->{source=(source+1)%(projects.size()+1);refresh();});button(width/2+55,height-28,90,"Back",this::onClose);
  if(projects.isEmpty()){var path=minecraft.gameDirectory.toPath();CompletableFuture.supplyAsync(()->{try{return new ProjectStore(path).list();}catch(Exception e){return java.util.List.<ProjectStore.Entry>of();}},ProjectStore.IO).thenAccept(result->minecraft.execute(()->projects=result));}
 }
 private void button(int x,int y,int w,String label,Runnable r){addRenderableWidget(Button.builder(Component.literal(label),b->r.run()).bounds(x,y,w,20).build());}
 private void refresh(){clearWidgets();init();}
 private int cell(){return Math.max(20,Math.min((width-32)/3,(height-112)/2));}
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){if(wall){int cell=cell(),x0=(width-cell*3)/2,y0=38;int x=(int)e.x()-x0,y=(int)e.y()-y0;if(x>=0&&x<cell*3&&y>=0&&y<cell*2){int index=y/cell*3+x/cell;if(e.button()==1)slots[index]=null;else if(e.button()==0)slots[index]=source==0?parent.document.flatten():projects.get(Math.min(source-1,projects.size()-1)).artwork().flatten();return true;}}return super.mouseClicked(e,twice);}
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 private void frame(GuiGraphicsExtractor g,int x,int y,int size){g.fill(x-4,y-4,x+size+4,y+size+4,glow?0xffffdd77:0xff805a34);g.fill(x-2,y-2,x+size+2,y+size+2,0xff35251d);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float t){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,wall?"WALL — left click: place / right click: clear":"SOURCE IMAGE / FINAL MAP COLORS",width/2,10,0xfff5ddaa);g.centeredText(font,font.plainSubstrByWidth(status,width-12),width/2,25,0xffbec8d8);
  if(wall){int cell=cell(),x0=(width-cell*3)/2,y0=38;g.fill(x0-6,y0-6,x0+cell*3+6,y0+cell*2+6,0xff625b51);for(int i=0;i<6;i++){int size=Math.max(12,(cell-12)*sizeMode/3),xx=x0+i%3*cell+(cell-size)/2,yy=y0+i/3*cell+(cell-size)/2;frame(g,xx,yy,size);if(slots[i]!=null)ArtRender.draw(g,slots[i],parent.palette,xx,yy,size);}}
  else{int size=Math.max(24,Math.min((width-40)/2,height-115)),x0=width/2-size-10,yy=45;frame(g,x0,yy,size);frame(g,width/2+10,yy,size);if(parent.document.original!=null)ArtRender.draw(g,parent.document.original,x0,yy,size);else ArtRender.draw(g,parent.document.flatten(),parent.palette,x0,yy,size);ArtRender.draw(g,parent.document.flatten(),parent.palette,width/2+10,yy,size);g.centeredText(font,parent.document.original==null?"No original image stored":"Fitted imported source",x0+size/2,yy+size+6,0xffbec8d8);}
  super.extractRenderState(g,x,y,t);
 }
}
