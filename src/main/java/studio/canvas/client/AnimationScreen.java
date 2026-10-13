package studio.canvas.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import studio.canvas.PaintLayout;
import studio.canvas.PaintingSize;
import java.util.*;
public final class AnimationScreen extends Screen {
 private final CanvasScreen parent;private boolean playing;private int ticks;private List<byte[]> preview;
 AnimationScreen(CanvasScreen p){super(Component.literal("Painting animation"));parent=p;parent.document.syncFrame();}
 private void button(int col,int y,String label,Runnable action){int w=Math.min(140,(width-24)/2),left=width/2-w-3;addRenderableWidget(Button.builder(Component.literal(label),b->{action.run();clearWidgets();init();}).bounds(left+col*(w+6),y,w,20).build());}
 private void stop(){playing=false;preview=null;}
 @Override protected void init(){
  var d=parent.document;
  button(0,36,"Previous frame",()->{stop();d.selectFrame(Math.floorMod(d.animationIndex-1,d.timeline.size()));});button(1,36,"Next frame",()->{stop();d.selectFrame((d.animationIndex+1)%d.timeline.size());});
  button(0,60,"Add copied frame",()->{stop();d.addFrame(false);});button(1,60,"Add blank frame",()->{stop();d.addFrame(true);});
  button(0,84,"Delete frame",()->{stop();d.deleteFrame();});button(1,84,"Delay: "+(d.frameDelay/20.0)+" s",()->{int[] values={10,20,40,80,160};int index=0;while(index<values.length-1&&values[index]!=d.frameDelay)index++;d.frameDelay=values[(index+1)%values.length];});
  button(0,108,playing?"Pause preview":"Play preview",()->{if(playing)stop();else{preview=d.imageSequence();ticks=0;playing=true;}});button(1,108,"Edit selected frame",this::onClose);
  addRenderableWidget(Button.builder(Component.literal("Back to drawing"),b->onClose()).bounds(width/2-70,height-28,140,20).build());
 }
 @Override public void tick(){ticks++;}
 @Override public void onClose(){stop();parent.syncPixels();parent.status="Each animation frame keeps its own editable layers.";minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
  g.fill(0,0,width,height,0xff181c26);var d=parent.document;g.centeredText(font,"ANIMATION "+(d.animationIndex+1)+" / "+d.timeline.size()+" - maximum 16",width/2,12,0xfff5ddaa);
  int side=Math.max(40,Math.min(96,height-176));byte[] pixels=playing&&preview!=null?preview.get((ticks/d.frameDelay)%preview.size()):d.flatten();ArtRender.draw(g,PaintLayout.apply(pixels,new PaintingSize(d.wallWidth,d.wallHeight),d.fitMode),parent.palette,width/2-side/2,136,side);
  super.extractRenderState(g,x,y,tick);
 }
}
