package studio.canvas.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import studio.canvas.client.art.PixelTools;
public final class StudioPanel extends Screen {
 private final CanvasScreen parent;private final int page;
 StudioPanel(CanvasScreen parent,int page){super(Component.literal("Studio tools"));this.parent=parent;this.page=page;}
 private void add(int index,String label,Runnable action){int w=Math.min(145,(width-30)/2),x=width/2-w-3+(index%2)*(w+6),y=38+(index/2)*25;addRenderableWidget(Button.builder(Component.literal(label),b->{action.run();}).bounds(x,y,w,20).build());}
 @Override protected void init(){
  if(page==0){int i=0;for(var tool:PixelTools.Tool.values()){add(i++,tool.name().replace('_',' '),()->{parent.selectTool(tool);onClose();});}}
  if(page==1){
   add(0,"Mirror X: "+parent.document.mirrorX,()->{parent.document.mirrorX=!parent.document.mirrorX;refresh();});add(1,"Mirror Y: "+parent.document.mirrorY,()->{parent.document.mirrorY=!parent.document.mirrorY;refresh();});
   add(2,"Grid: "+parent.document.grid,()->{parent.document.grid=!parent.document.grid;refresh();});add(3,"Snap: "+parent.document.snap,()->{parent.document.snap=parent.document.snap==1?4:parent.document.snap==4?8:1;refresh();});
   add(4,"Zoom: "+parent.zoom+"x",()->{parent.changeZoom();refresh();});add(5,"Palette: "+new String[]{"Default","Grayscale","Retro 8"}[parent.preset],()->{parent.cyclePalette();refresh();});
   add(6,"Reset pan",()->{parent.panX=parent.panY=0;onClose();});
  }
  if(page==2){
   add(0,"Import PNG / JPEG",()->{onClose();parent.pickImage();});add(1,"Fit / centre crop",()->{parent.toggleCrop();onClose();});
   add(2,"Export PNG",()->{parent.exportImage();onClose();});add(3,"Save editable project",()->{parent.saveProject(false);onClose();});
   add(4,"Layers",()->minecraft.setScreenAndShow(new LayerScreen(parent)));add(5,"Preview / wall",()->minecraft.setScreenAndShow(new PaintingPreview(parent)));
   add(6,parent.tier.canExchange()?"Draw to Get Item":"Get Item: locked",()->{onClose();parent.finder(false);});add(7,"Browse catalogue",()->parent.finder(true));
   add(9,"Painting size",()->minecraft.setScreenAndShow(new PaintingSizeScreen(parent)));
   add(10,"Display / access",()->minecraft.setScreenAndShow(new PaintingOptionsScreen(parent)));
   add(11,"Animation",()->minecraft.setScreenAndShow(new AnimationScreen(parent)));
   add(12,"Minecraft paintings",()->minecraft.setScreenAndShow(new MinecraftPaintingsScreen(parent)));
   add(13,"Server art gallery",()->minecraft.setScreenAndShow(new ServerGalleryScreen(parent)));
   add(8,"Clear active layer",()->{parent.clearLayer();onClose();});
  }
  addRenderableWidget(Button.builder(Component.literal("Back to drawing"),b->onClose()).bounds(width/2-70,height-28,140,20).build());
 }
 private void refresh(){clearWidgets();init();}
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float t){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,page==0?"Drawing tools":page==1?"Drawing options":"Canvas Studio+",width/2,12,0xfff5ddaa);if(page==1)g.centeredText(font,"Shift-drag: constrain line / square / circle. Middle-drag: pan.",width/2,height-48,0xffbec8d8);super.extractRenderState(g,x,y,t);}
}
