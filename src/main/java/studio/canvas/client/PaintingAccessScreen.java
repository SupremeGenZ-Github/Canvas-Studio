package studio.canvas.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
/** A lock controls other players' access; it never makes the owner's artwork uneditable. */
public final class PaintingAccessScreen extends Screen {
 private final CanvasScreen parent;
 PaintingAccessScreen(CanvasScreen parent){super(Component.literal("Reopen locked painting"));this.parent=parent;}
 @Override protected void init(){
  addRenderableWidget(Button.builder(Component.literal("Edit keeping locked"),b->{if(!parent.changingLock())minecraft.setScreenAndShow(parent);}).bounds(width/2-120,62,240,20).build());
  addRenderableWidget(Button.builder(Component.literal("Unlock & edit"),b->{if(!parent.changingLock())parent.unlockAndEdit();}).bounds(width/2-120,90,240,20).build());
  addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(width/2-70,height-28,140,20).build());
 }
 @Override public void onClose(){if(!parent.changingLock())minecraft.setScreenAndShow(null);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
  g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"REOPEN LOCKED PAINTING",width/2,14,0xfff5ddaa);
  g.centeredText(font,"Owners and admins can edit saved or placed artwork.",width/2,36,0xffbec8d8);
  g.centeredText(font,"Unlocking allows public editing and takes effect now.",width/2,128,0xffbec8d8);
  g.centeredText(font,"Artwork, animation, size and frame are preserved.",width/2,145,0xffbec8d8);
  g.centeredText(font,"You can lock it again in Display / access when saving.",width/2,162,0xffbec8d8);
  g.centeredText(font,font.plainSubstrByWidth(parent.status,width-12),width/2,height-46,parent.changingLock()?0xfff5ddaa:0xffdddddd);
  super.extractRenderState(g,x,y,tick);
 }
}
