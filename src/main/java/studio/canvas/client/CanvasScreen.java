package studio.canvas.client;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import studio.canvas.*;
import studio.canvas.client.art.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class CanvasScreen extends Screen {
 private static Artwork draft;
 private static final UUID RECOVERY=UUID.fromString("00000000-0000-0000-0000-000000000001");
 final InteractionHand hand; final CanvasTier tier;
 private final UUID openRequest=UUID.randomUUID(); UUID exchangeToken;
 private boolean sessionRequested; long uses;int maximum;boolean moddedRewards;
 Artwork document; private byte[] pixels; final int[] palette=new int[248];
 private final byte[] swatches=new byte[32];
 private EditBox titleBox,hexBox;
 private int left,top,scale,side,tools,brush=2,lastX=-1,lastY=-1,startX,startY,stroke,ticks;
 private byte selected,white; private boolean fillTool,crop,importing,dragging,shapeDrag,saving;
 int zoom=1,panX,panY,preset; String status="Left: draw | Right: erase | Shift: constrain | Middle: pan";
 private BufferedImage original; private UUID projectId; boolean editingPainting; UUID paintingToken=CanvasReply.NONE;
 PixelTools.Tool tool=PixelTools.Tool.BRUSH;
 public CanvasScreen(InteractionHand hand){this(hand,CanvasTier.BLANK);}
 public CanvasScreen(InteractionHand hand,CanvasTier tier){
  super(Component.literal("Canvas Studio+ - made by SuprixZ"));this.hand=hand;this.tier=tier;CanvasClient.activeCanvas=this;
  for(int i=4;i<248;i++)palette[i]=MapColor.getColorFromPackedId(i);
  white=ImageImport.nearest(0xffffff,palette);selected=ImageImport.nearest(0x202020,palette);
  document=draft==null?new Artwork():draft.copy();syncPixels();refreshPalette();
 }
 String usageLabel(){return tier==CanvasTier.INFINITY?"Uses: "+uses+" / "+(maximum==0?"unlimited":maximum):tier.availability;}
 void reply(CanvasReply reply){
  if(saving&&reply.request().equals(paintingToken)){
   saving=false;status=reply.message();if(reply.code()==4){draft=null;minecraft.setScreenAndShow(null);}return;
  }
  if(!reply.request().equals(openRequest)&&!reply.request().equals(exchangeToken))return;
  uses=reply.uses();maximum=reply.maximum();moddedRewards=reply.modded();
  if(reply.code()==0||reply.code()==2)exchangeToken=reply.token();status=reply.message();
 }
 CompletableFuture<java.util.List<ItemMatcher.Reference>> matchReferences(){return ItemMatchCatalog.load(minecraft.getResourceManager());}
 private void refreshPalette(){int[] colors={0x202020,0x555555,0x999999,0xffffff,0x6c3020,0xb87438,0xdfad6b,0xffdd44,0xa31616,0xff3333,0xff7733,0xffbb33,0x1e692c,0x45a63a,0x99ce51,0xcde8a0,0x143567,0x315dc2,0x3d98c9,0x91d9e5,0x591d8d,0x964bd0,0xd844ab,0xffa3c5,0x452d22,0x815e43,0xd5c7a0,0xede5cd,0x087e80,0x47bba7,0xababdf,0xeecccc};for(int i=0;i<32;i++){int c=preset==1?(i*255/31)*0x010101:preset==2?colors[(i/4)*4]:colors[i];swatches[i]=ImageImport.nearest(c,palette);}}
 @Override protected void init(){
  syncTitle();scale=Math.max(1,Math.min(3,Math.min((width-160)/128,(height-112)/128)));side=128*scale;left=Math.max(6,(width-side-146)/2);top=42;tools=left+side+10;
  titleBox=new EditBox(font,left,21,Math.min(width-left-6,side+138),17,Component.literal("Painting title"));titleBox.setMaxLength(64);titleBox.setValue(document.title);addRenderableWidget(titleBox);
  hexBox=new EditBox(font,left,top+side+5,Math.max(60,side-50),18,Component.literal("Hex color"));hexBox.setMaxLength(7);hexBox.setValue(String.format("#%06X",palette[Byte.toUnsignedInt(selected)]&0xffffff));addRenderableWidget(hexBox);
  button(left+side-46,top+side+5,46,"Color",()->{try{selected=ImageImport.nearest(Integer.parseInt(hexBox.getValue().replace("#",""),16),palette);document.selected=selected;status="Selected nearest Minecraft map color.";}catch(NumberFormatException e){status="Use a color like #FF8800.";}});
  button(tools,top+52,132,tool.toString().replace('_',' '),()->openPanel(0));
  button(tools,top+72,64,"Size: "+brush,()->{brush=brush==1?2:brush==2?4:brush==4?8:1;document.brush=brush;rebuild();});
  button(tools+68,top+72,64,"Options",()->openPanel(1));
  button(tools,top+92,64,"Undo",()->{finishStroke();document.undo();syncPixels();});button(tools+68,top+92,64,"Redo",()->{finishStroke();document.redo();syncPixels();});
  button(tools,top+112,132,"Projects",()->{finishStroke();syncTitle();minecraft.setScreenAndShow(new ProjectScreen(this));});
  button(tools,top+132,132,"Studio menu",()->openPanel(2));button(tools,top+152,132,"Close",this::onClose);
  button(left,top+side+27,side,editingPainting?"Save edited painting":"Save Painting",this::savePainting);
 }
 private void openPanel(int page){finishStroke();syncTitle();minecraft.setScreenAndShow(new StudioPanel(this,page));}
 void syncTitle(){if(titleBox!=null)document.title=titleBox.getValue();}
 void syncPixels(){pixels=document.pixels();brush=document.brush;selected=document.selected;try{tool=PixelTools.Tool.valueOf(document.tool);}catch(Exception e){tool=PixelTools.Tool.BRUSH;}fillTool=tool==PixelTools.Tool.FILL;}
 void rebuild(){syncTitle();clearWidgets();init();}
 private Button button(int x,int y,int w,String label,Runnable action){return addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,18).build());}
 void selectTool(PixelTools.Tool t){tool=t;document.tool=t.name();fillTool=t==PixelTools.Tool.FILL;}
 void clearDraftAfterExchange(){draft=null;}
 @Override public boolean isPauseScreen(){return false;}
 private void finishStroke(){if(dragging){document.commit();dragging=false;}lastX=lastY=-1;}
 @Override public void onClose(){if(saving){status="Waiting for server save confirmation...";return;}if(importing){status="Wait for image import.";return;}finishStroke();syncTitle();draft=document.copy();saveProject(true);super.onClose();}
 @Override public void removed(){finishStroke();}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float tick){
  g.fill(0,0,width,height,0xf0181c26);g.centeredText(font,"CANVAS STUDIO+ - made by SuprixZ",width/2,7,0xfff5ddaa);
  g.fill(left-2,top-2,left+side+2,top+side+2,0xffaa8050);byte[] flat=document.flatten();int unit=scale*zoom,visible=128/zoom;
  for(int y=0;y<visible;y++)for(int x=0;x<visible;){int start=x,c=Byte.toUnsignedInt(flat[(y+panY)*128+x+panX]);while(x<visible&&Byte.toUnsignedInt(flat[(y+panY)*128+x+panX])==c)x++;g.fill(left+start*unit,top+y*unit,left+x*unit,top+(y+1)*unit,palette[c]);}
  if(document.grid&&unit>=3)for(int i=0;i<=visible;i++){g.fill(left+i*unit,top,left+i*unit+1,top+side,0x55333333);g.fill(left,top+i*unit,left+side,top+i*unit+1,0x55333333);}
  for(int i=0;i<32;i++){int x=tools+i%8*16,y=top+i/8*12;g.fill(x,y,x+15,y+11,swatches[i]==selected?0xffffffff:0xff363b48);g.fill(x+1,y+1,x+14,y+10,palette[Byte.toUnsignedInt(swatches[i])]);}
  g.text(font,font.plainSubstrByWidth((editingPainting?"Editing saved painting":tier.label+" | "+usageLabel())+" | "+zoom+"x",width-12),6,height-23,0xffbec8d8,false);
  g.text(font,font.plainSubstrByWidth(status,width-12),6,height-11,0xffdddddd,false);super.extractRenderState(g,mx,my,tick);
 }
 private int px(double x){return PixelTools.snap((int)(x-left)/(scale*zoom)+panX,document.snap);}
 private int py(double y){return PixelTools.snap((int)(y-top)/(scale*zoom)+panY,document.snap);}
 private boolean inside(double x,double y){return x>=left&&x<left+side&&y>=top&&y<top+side;}
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
  if(e.button()==InputConstants.MOUSE_BUTTON_LEFT&&e.x()>=tools&&e.x()<tools+128&&e.y()>=top&&e.y()<top+48){selected=swatches[(int)(e.y()-top)/12*8+(int)(e.x()-tools)/16];document.selected=selected;return true;}
  if((e.button()==InputConstants.MOUSE_BUTTON_LEFT||e.button()==InputConstants.MOUSE_BUTTON_RIGHT)&&inside(e.x(),e.y())&&!importing){
   if(getFocused()!=null)setFocused(null);int x=px(e.x()),y=py(e.y());
   if(tool==PixelTools.Tool.EYEDROPPER&&e.button()==InputConstants.MOUSE_BUTTON_LEFT){selected=document.flatten()[y*128+x];document.selected=selected;return true;}
   document.begin();dragging=true;startX=lastX=x;startY=lastY=y;stroke=0;shapeDrag=tool==PixelTools.Tool.LINE||tool==PixelTools.Tool.RECTANGLE||tool==PixelTools.Tool.FILLED_RECTANGLE||tool==PixelTools.Tool.ELLIPSE||tool==PixelTools.Tool.FILLED_ELLIPSE;
   byte color=e.button()==InputConstants.MOUSE_BUTTON_RIGHT||tool==PixelTools.Tool.ERASER?(document.active==0?white:0):selected;
   if(fillTool||tool==PixelTools.Tool.FILL){PixelTools.flood(document,x,y,color);finishStroke();}else if(shapeDrag)PixelTools.shape(document,tool,x,y,x,y,color);else PixelTools.line(document,x,y,x,y,color,tool,stroke);return true;
  }return super.mouseClicked(e,twice);
 }
 @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
  if(e.button()==InputConstants.MOUSE_BUTTON_MIDDLE&&inside(e.x(),e.y())&&zoom>1){panX=Math.max(0,Math.min(128-128/zoom,panX-(int)(dx/(scale*zoom))));panY=Math.max(0,Math.min(128-128/zoom,panY-(int)(dy/(scale*zoom))));return true;}
  if(dragging&&(e.button()==InputConstants.MOUSE_BUTTON_LEFT||e.button()==InputConstants.MOUSE_BUTTON_RIGHT)&&!importing){int x=px(Math.max(left,Math.min(left+side-1,e.x()))),y=py(Math.max(top,Math.min(top+side-1,e.y())));byte c=e.button()==InputConstants.MOUSE_BUTTON_RIGHT||tool==PixelTools.Tool.ERASER?(document.active==0?white:0):selected;
   if(e.hasShiftDown()){int ax=x-startX,ay=y-startY;if(shapeDrag&&tool!=PixelTools.Tool.LINE){int n=Math.max(Math.abs(ax),Math.abs(ay));x=Math.max(0,Math.min(127,startX+(ax<0?-n:n)));y=Math.max(0,Math.min(127,startY+(ay<0?-n:n)));}else if(Math.abs(ax)>Math.abs(ay)*2)y=startY;else if(Math.abs(ay)>Math.abs(ax)*2)x=startX;else{int n=Math.max(Math.abs(ax),Math.abs(ay));x=Math.max(0,Math.min(127,startX+(ax<0?-n:n)));y=Math.max(0,Math.min(127,startY+(ay<0?-n:n)));}}
   if(shapeDrag){document.previewReset();PixelTools.shape(document,tool,startX,startY,x,y,c);}else{PixelTools.line(document,lastX,lastY,x,y,c,tool,stroke);stroke+=Math.max(Math.abs(x-lastX),Math.abs(y-lastY));}lastX=x;lastY=y;return true;
  }return super.mouseDragged(e,dx,dy);
 }
 @Override public boolean mouseReleased(MouseButtonEvent e){boolean handled=dragging;finishStroke();return handled||super.mouseReleased(e);}
 public boolean applyDrawingShortcut(KeyEvent e,boolean typing){
  if(typing)return false;
  if(e.isEscape()&&dragging){document.cancel();dragging=false;syncPixels();return true;}
  if(e.hasControlDown()&&e.key()==InputConstants.KEY_Z){finishStroke();document.undo();syncPixels();return true;}
  if(e.hasControlDown()&&e.key()==InputConstants.KEY_Y){finishStroke();document.redo();syncPixels();return true;}
  if(e.hasControlDown()||e.hasAltDown())return false;
  if(e.key()==InputConstants.KEY_B)selectTool(PixelTools.Tool.BRUSH);
  else if(e.key()==InputConstants.KEY_E)selectTool(PixelTools.Tool.ERASER);
  else if(e.key()==InputConstants.KEY_I)selectTool(PixelTools.Tool.EYEDROPPER);
  else if(e.key()==InputConstants.KEY_G)document.grid=!document.grid;
  else return false;
  return true;
 }
 @Override public boolean keyPressed(KeyEvent e){if(applyDrawingShortcut(e,getFocused() instanceof EditBox)){rebuild();return true;}return super.keyPressed(e);}
 void changeZoom(){zoom=zoom==1?2:zoom==2?4:1;panX=Math.min(panX,128-128/zoom);panY=Math.min(panY,128-128/zoom);}
 void cyclePalette(){preset=(preset+1)%3;refreshPalette();}
 void clearLayer(){document.begin();Arrays.fill(pixels,document.active==0?white:(byte)0);document.commit();status="Layer cleared; Undo restores it.";}
 void toggleCrop(){crop=!crop;if(original!=null){document.begin();applyImage();document.commit();}status=crop?"Centre crop":"Whole image fit";}
 private void savePainting(){if(importing||saving)return;finishStroke();syncTitle();if(minecraft.player==null)return;saveProject(true);saving=true;status="Saving painting on server...";ClientPacketDistributor.sendToServer(new SavePainting(hand,document.title,document.flatten(),paintingToken));}
 void finder(boolean browse){finishStroke();syncTitle();if(!browse&&!tier.canExchange()){status="Get Item requires a Molten or Infinity Canvas.";return;}minecraft.setScreenAndShow(new ItemMatchScreen(this,hand,document.flatten(),palette,browse));}
 void saveProject(boolean recovery){
  syncTitle();Artwork copy=document.copy();UUID id=recovery?RECOVERY:projectId==null?(projectId=UUID.randomUUID()):projectId;Path root=minecraft.gameDirectory.toPath();
  CompletableFuture.runAsync(()->{try{new ProjectStore(root).save(id,copy);}catch(Exception e){throw new java.util.concurrent.CompletionException(e);}},ProjectStore.IO).whenComplete((v,e)->minecraft.execute(()->{if(!recovery)status=e==null?"Project saved in canvasstudio/projects.":"Project save failed: "+e.getCause().getMessage();}));
 }
 void loadProject(UUID id,Artwork d){document=d.copy();projectId=id.equals(RECOVERY)?null:id;titleBox=null;original=null;syncPixels();zoom=1;panX=panY=0;status="Project loaded; painting save is separate.";}
    @Override public void tick(){
        if(!sessionRequested && tier.canExchange()){sessionRequested=true;ClientPacketDistributor.sendToServer(new OpenCanvas(hand,openRequest));}
        if(++ticks%600==0 && !importing){saveProject(true);}
    }
    void pickImage(){
        if(importing)return;
        importing=true;status="Choose a PNG or JPEG image...";
        CompletableFuture.supplyAsync(()->{
            try(var stack=MemoryStack.stackPush()){
                var filters=stack.mallocPointer(3);
                filters.put(stack.UTF8("*.png")).put(stack.UTF8("*.jpg")).put(stack.UTF8("*.jpeg")).flip();
                return TinyFileDialogs.tinyfd_openFileDialog("Import image",null,filters,"PNG / JPEG",false);
            }
        }).whenComplete((path,pickerError)->minecraft.execute(()->{
            if(pickerError!=null){importing=false;status="File picker failed: "+pickerError.getMessage();return;}
            if(path==null){importing=false;status="Import cancelled.";return;}
            status="Loading image...";
            CompletableFuture.supplyAsync(()->{
                try{return ImageImport.load(Path.of(path));}catch(Exception ex){throw new java.util.concurrent.CompletionException(ex);}
            }).whenComplete((image,error)->minecraft.execute(()->{
                importing=false;
                if(error!=null){status="Import failed: "+error.getCause().getMessage();return;}
                document.begin();original=image;applyImage();document.commit();syncPixels();
            }));
        }));
    }

 private void applyImage(){BufferedImage fitted=ImageImport.resize(original,crop);byte[] converted=ImageImport.quantize(fitted,palette);System.arraycopy(converted,0,pixels,0,pixels.length);document.original=fitted.getRGB(0,0,128,128,null,0,128);status="Imported into active layer.";}
 void exportImage(){finishStroke();byte[] flat=document.flatten();Path root=minecraft.gameDirectory.toPath();CompletableFuture.runAsync(()->{try{BufferedImage image=new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);for(int p=0;p<flat.length;p++)image.setRGB(p%128,p/128,palette[Byte.toUnsignedInt(flat[p])]);Path folder=root.resolve("canvasstudio/exports");if(java.nio.file.Files.isSymbolicLink(folder)||java.nio.file.Files.isSymbolicLink(folder.getParent()))throw new java.io.IOException("Unsafe export directory");java.nio.file.Files.createDirectories(folder);javax.imageio.ImageIO.write(image,"png",folder.resolve("painting-"+UUID.randomUUID()+".png").toFile());}catch(Exception e){throw new java.util.concurrent.CompletionException(e);}},ProjectStore.IO).whenComplete((v,e)->minecraft.execute(()->status=e==null?"PNG exported to canvasstudio/exports.":"Export failed: "+e.getCause().getMessage()));}
}
