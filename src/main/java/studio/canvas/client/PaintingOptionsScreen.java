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
  button(42,"Frame: "+PaintOptions.FRAMES[parent.document.frameStyle],()->parent.document.frameStyle=(parent.document.frameStyle+1)%5);
  button(68,"Layout: "+PaintOptions.FITS[parent.document.fitMode],()->parent.document.fitMode=(parent.document.fitMode+1)%3);
  button(94,parent.document.locked?"Access: owner / admins only":"Access: public editing",()->parent.document.locked=!parent.document.locked);
  button(120,"Change wall size",()->minecraft.setScreenAndShow(new PaintingSizeScreen(parent)));
  addRenderableWidget(Button.builder(Component.literal("Back to drawing"),b->onClose()).bounds(width/2-70,height-28,140,20).build());
 }
 @Override public void onClose(){parent.status="Display options apply when you save the painting.";minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"DISPLAY AND ACCESS",width/2,12,0xfff5ddaa);g.centeredText(font,"Frames are cosmetic; no extra crafting cost.",width/2,154,0xffbec8d8);g.centeredText(font,"Only owners/admins can change an existing lock.",width/2,171,0xffbec8d8);super.extractRenderState(g,x,y,tick);}
}
