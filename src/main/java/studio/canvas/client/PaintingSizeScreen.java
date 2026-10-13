package studio.canvas.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
/** Size selection affects the next saved painting and is retained in editable projects. */
public final class PaintingSizeScreen extends Screen {
 private final CanvasScreen parent;private boolean square;
 PaintingSizeScreen(CanvasScreen parent){super(Component.literal("Painting wall size"));this.parent=parent;square=parent.document.wallWidth==parent.document.wallHeight;}
 private void button(int x,int y,int w,String text,Runnable action){addRenderableWidget(Button.builder(Component.literal(text),b->{action.run();clearWidgets();init();}).bounds(x,y,w,20).build());}
 @Override protected void init(){
  button(width/2-120,55,55,"Width -",()->change(-1,0));button(width/2+65,55,55,"Width +",()->change(1,0));
  button(width/2-120,85,55,"Height -",()->change(0,-1));button(width/2+65,85,55,"Height +",()->change(0,1));
  button(width/2-120,120,240,square?"Keep square: yes":"Keep square: no",()->{square=!square;if(square)parent.document.wallHeight=parent.document.wallWidth;});
  button(width/2-120,150,240,"Reset to 1 x 1",()->{parent.document.wallWidth=parent.document.wallHeight=1;});
  addRenderableWidget(Button.builder(Component.literal("Back to drawing"),b->onClose()).bounds(width/2-70,height-28,140,20).build());
 }
 private void change(int dx,int dy){int w=Math.max(1,Math.min(8,parent.document.wallWidth+dx)),h=Math.max(1,Math.min(8,parent.document.wallHeight+dy));if(square){int side=dx!=0?w:h;w=h=side;}parent.document.wallWidth=w;parent.document.wallHeight=h;}
 @Override public void onClose(){parent.status="Wall size: "+parent.document.wallWidth+" x "+parent.document.wallHeight+" blocks. Save Painting to apply.";minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
  g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"PAINTING SIZE — maximum 8 x 8 blocks",width/2,12,0xfff5ddaa);
  g.centeredText(font,"128 x 128 artwork scales to the selected wall size.",width/2,30,0xffbec8d8);
  g.centeredText(font,Integer.toString(parent.document.wallWidth),width/2,61,0xffffffff);g.centeredText(font,Integer.toString(parent.document.wallHeight),width/2,91,0xffffffff);
  g.centeredText(font,"Placement grows up and right from the clicked wall block.",width/2,height-49,0xffbec8d8);super.extractRenderState(g,x,y,tick);
 }
}
