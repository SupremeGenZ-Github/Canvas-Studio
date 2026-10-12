package studio.canvas.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public final class LayerScreen extends Screen {
 private final CanvasScreen parent;private EditBox name;
 LayerScreen(CanvasScreen parent){super(Component.literal("Layers"));this.parent=parent;}
 private void add(int index,String label,Runnable run){int w=Math.min(135,(width-30)/2);addRenderableWidget(Button.builder(Component.literal(label),b->{run.run();parent.syncPixels();clearWidgets();init();}).bounds(width/2-w-3+index%2*(w+6),78+index/2*24,w,20).build());}
 @Override protected void init(){var d=parent.document;name=new EditBox(font,width/2-135,48,270,20,Component.literal("Layer name"));name.setMaxLength(64);name.setValue(d.layers.get(d.active).name);addRenderableWidget(name);
  add(0,"Previous layer",()->d.active=Math.max(0,d.active-1));add(1,"Next layer",()->d.active=Math.min(d.layers.size()-1,d.active+1));
  add(2,"Add (max 16)",d::addLayer);add(3,"Duplicate",d::duplicateLayer);
  add(4,"Move down",()->d.moveLayer(-1));add(5,"Move up",()->d.moveLayer(1));
  add(6,d.layers.get(d.active).visible?"Visible: yes":"Visible: no",()->{d.begin();d.layers.get(d.active).visible=!d.layers.get(d.active).visible;d.commit();});
  add(7,"Rename",()->{d.begin();d.layers.get(d.active).name=name.getValue();d.commit();});add(8,"Merge down",d::mergeDown);add(9,"Delete layer",()->{if(d.layers.size()>1){d.begin();d.layers.remove(d.active);d.active=Math.min(d.active,d.layers.size()-1);d.commit();}});
  addRenderableWidget(Button.builder(Component.literal("Back to drawing"),b->onClose()).bounds(width/2-70,height-28,140,20).build());
 }
 @Override public void onClose(){parent.syncPixels();minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float t){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"LAYERS — bottom to top",width/2,10,0xfff5ddaa);g.centeredText(font,"Active: "+(parent.document.active+1)+" / "+parent.document.layers.size()+" | Undo restores layer changes",width/2,29,0xffbec8d8);super.extractRenderState(g,x,y,t);}
}
