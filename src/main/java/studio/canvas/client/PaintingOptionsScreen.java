package studio.canvas.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import studio.canvas.*;
public final class PaintingOptionsScreen extends Screen {
 private final CanvasScreen parent;
 PaintingOptionsScreen(CanvasScreen p){super(Component.literal("Painting display and access"));parent=p;}
 private void button(int y,String label,Runnable action){addRenderableWidget(Button.builder(Component.literal(label),b->{action.run();clearWidgets();init();}).bounds(width/2-130,y,260,20).build());}
 @Override protected void init(){
  button(42,"Frame: "+PaintOptions.FRAMES[parent.document.frameStyle],()->parent.document.frameStyle=FrameMaterials.next(parent.document.frameStyle));
  button(68,"Layout: "+PaintOptions.FITS[parent.document.fitMode],()->parent.document.fitMode=(parent.document.fitMode+1)%3);
  button(94,parent.document.locked?"Unlock painting on save":"Lock painting on save",()->parent.document.locked=!parent.document.locked);
  button(120,"Change wall size",()->minecraft.setScreenAndShow(new PaintingSizeScreen(parent)));
  addRenderableWidget(Button.builder(Component.literal("Back to drawing"),b->onClose()).bounds(width/2-70,height-28,140,20).build());
 }
 @Override public void onClose(){parent.status="Display options apply when you save the painting.";minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
  g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"DISPLAY AND ACCESS",width/2,12,0xfff5ddaa);
  int frame=parent.document.frameStyle;boolean costs=FrameMaterials.costsMaterial(parent.savedFrameStyle,frame);
  boolean ready=!costs||(minecraft.player!=null&&FrameMaterials.has(minecraft.player.getInventory(),minecraft.player.getOffhandItem(),frame));
  String cost=costs?"Cost: 1 "+FrameMaterials.material(frame)+(ready?" (ready)":" (missing)"):"Cost: none (keep or remove the saved frame)";
  g.centeredText(font,font.plainSubstrByWidth(cost,width-12),width/2,154,ready?0xffa8dbad:0xffffa080);
  g.centeredText(font,"Material is spent only after a successful save.",width/2,171,0xffbec8d8);
  g.centeredText(font,"Changing frames costs again; removal gives no refund.",width/2,188,0xffbec8d8);
  super.extractRenderState(g,x,y,tick);
 }
}
