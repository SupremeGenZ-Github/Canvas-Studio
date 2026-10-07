package studio.canvas.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;
import studio.canvas.SavePainting;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public final class CanvasScreen extends Screen {
    private static byte[] draft;
    private static String draftTitle = "Custom Painting";
    private final InteractionHand hand;
    private final int[] palette = new int[248];
    private final byte[] pixels;
    private final ArrayDeque<byte[]> undo = new ArrayDeque<>();
    private final byte[] swatches = new byte[32];
    private EditBox titleBox, hexBox;
    private int left, top, scale, side, tools, brush=2, lastX=-1, lastY=-1;
    private byte selected, white;
    private boolean fillTool, crop, importing, receiveMode;
    private String status = "Left: draw | Right: erase";
    private BufferedImage original;
    private CompletableFuture<java.util.List<ItemMatcher.Reference>> itemReferences;
    CompletableFuture<java.util.List<ItemMatcher.Reference>> matchReferences(){
        if(itemReferences==null)itemReferences=ItemMatchCatalog.load(minecraft.getResourceManager());
        return itemReferences;
    }

    public CanvasScreen(InteractionHand hand) {
        super(Component.literal("Canvas Studio - made by SuprixZ")); this.hand = hand;
        for (int i=4;i<palette.length;i++) {
            // Minecraft 26.3 exposes packed map colors as ARGB, matching the GUI.
            int c = MapColor.getColorFromPackedId(i);
            palette[i] = c;
        }
        white = ImageImport.nearest(0xffffff, palette); selected = ImageImport.nearest(0x202020,palette);
        pixels = draft == null ? new byte[SavePainting.PIXELS] : draft.clone();
        if (draft == null) Arrays.fill(pixels, white);
        int[] colors={0x202020,0x555555,0x999999,0xffffff,0x6c3020,0xb87438,0xdfad6b,0xffdd44,
            0xa31616,0xff3333,0xff7733,0xffbb33,0x1e692c,0x45a63a,0x99ce51,0xcde8a0,
            0x143567,0x315dc2,0x3d98c9,0x91d9e5,0x591d8d,0x964bd0,0xd844ab,0xffa3c5,
            0x452d22,0x815e43,0xd5c7a0,0xede5cd,0x087e80,0x47bba7,0xababdf,0xeecccc};
        for(int i=0;i<colors.length;i++)swatches[i]=ImageImport.nearest(colors[i],palette);
    }
    @Override protected void init() {
        if (titleBox != null) draftTitle = titleBox.getValue();
        scale = Math.max(1, Math.min(3, Math.min((width-170)/128, (height-122)/128)));
        side = 128*scale; left = Math.max(8,(width-side-152)/2); top=46; tools=left+side+12;
        titleBox = new EditBox(font,left,22,side+140,20,Component.literal("Painting title"));
        titleBox.setMaxLength(64); titleBox.setValue(draftTitle); addRenderableWidget(titleBox);
        hexBox = new EditBox(font,left,top+side+6,Math.max(55,side-52),20,Component.literal("Hex color"));
        hexBox.setMaxLength(7); hexBox.setValue("#202020"); addRenderableWidget(hexBox);
        button(left+side-48,top+side+6,48,"Color",()->{
            try { selected=ImageImport.nearest(Integer.parseInt(hexBox.getValue().replace("#",""),16),palette);status="Color selected (map palette)."; }
            catch(NumberFormatException ex){status="Use a color like #FF8800.";}
        });
        button(tools,top+54,64,"Brush: "+brush,()->{brush=brush==1?2:brush==2?4:brush==4?8:1;rebuild();});
        button(tools+68,top+54,64,fillTool?"Fill":"Draw",()->{fillTool=!fillTool;rebuild();});
        button(tools,top+78,132,"Import PNG / JPEG",this::pickImage);
        button(tools,top+102,132,crop?"Fit: centre crop":"Fit: whole image",()->{
            crop=!crop;if(original!=null){snapshot();applyImage();}rebuild();
        });
        button(tools,top+126,64,"Undo",()->{if(!undo.isEmpty())System.arraycopy(undo.removeLast(),0,pixels,0,pixels.length);});
        button(tools+68,top+126,64,"Clear",()->{snapshot();Arrays.fill(pixels,white);status="Cleared; Undo restores it.";});
        button(tools,top+174,132,receiveMode?"Mode: Get Item":"Mode: Painting",()->{receiveMode=!receiveMode;rebuild();});
        button(tools,top+150,132,receiveMode?"Find Item / Block":"Save Painting",()->{
            if(importing){status="Wait for image import.";return;}
            if(receiveMode){minecraft.setScreenAndShow(new ItemMatchScreen(this,hand,pixels,palette));return;}
            if(minecraft.player==null || !minecraft.player.getItemInHand(hand).is(studio.canvas.CanvasStudio.CANVAS.get())){status="Hold your Blank Canvas to save.";return;}
            ClientPacketDistributor.sendToServer(new SavePainting(hand,titleBox.getValue(),pixels.clone()));
            draft=null;draftTitle="Custom Painting";minecraft.setScreenAndShow(null);
        });
        button(left,top+side+30,64,"Export",this::exportImage);
        button(left+side-64,top+side+30,64,"Close",this::onClose);
    }
    void clearDraftAfterExchange(){draft=null;draftTitle="Custom Painting";}
    private void rebuild() {draftTitle=titleBox.getValue();String color=hexBox.getValue();clearWidgets();init();hexBox.setValue(color);}
    private void button(int x,int y,int w,String label,Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,20).build());
    }
    private void snapshot(){if(undo.size()==10)undo.removeFirst();undo.addLast(pixels.clone());}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){if(importing){status="Wait for the image to finish loading.";return;}draft=pixels.clone();draftTitle=titleBox.getValue();super.onClose();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        g.fill(0,0,width,height,0xf0181c26);
        g.centeredText(font,Component.literal("CANVAS STUDIO - made by SuprixZ"),width/2,10,0xfff5ddaa);
        g.fill(left-2,top-2,left+side+2,top+side+2,0xffaa8050);
        // Coalesce horizontal runs, avoiding a separate draw call for every pixel in flat areas.
        for(int y=0;y<128;y++)for(int x=0;x<128;){
            int start=x, color=Byte.toUnsignedInt(pixels[y*128+x]);
            while(x<128 && Byte.toUnsignedInt(pixels[y*128+x])==color)x++;
            g.fill(left+start*scale,top+y*scale,left+x*scale,top+(y+1)*scale,palette[color]);
        }
        for(int i=0;i<swatches.length;i++){
            int x=tools+(i%8)*16,y=top+(i/8)*12;
            g.fill(x,y,x+15,y+11,swatches[i]==selected?0xffffffff:0xff363b48);
            g.fill(x+1,y+1,x+14,y+10,palette[Byte.toUnsignedInt(swatches[i])]);
        }
        g.text(font,font.plainSubstrByWidth(status,width-16),8,height-13,0xffdddddd,false);
        super.extractRenderState(g,mouseX,mouseY,partialTick);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        double mx=event.x(),my=event.y();
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT && mx>=tools && mx<tools+128 && my>=top && my<top+48){
            selected=swatches[(int)(my-top)/12*8+(int)(mx-tools)/16];return true;
        }
        if((event.button()==InputConstants.MOUSE_BUTTON_LEFT||event.button()==InputConstants.MOUSE_BUTTON_RIGHT)&&inside(mx,my)&&!importing){
            snapshot();lastX=(int)(mx-left)/scale;lastY=(int)(my-top)/scale;
            byte color=event.button()==InputConstants.MOUSE_BUTTON_RIGHT?white:selected;
            if(fillTool)flood(lastX,lastY,color);else stamp(lastX,lastY,color);return true;
        }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        if((event.button()==InputConstants.MOUSE_BUTTON_LEFT||event.button()==InputConstants.MOUSE_BUTTON_RIGHT)&&inside(event.x(),event.y())&&!importing&&!fillTool&&lastX>=0){
            int x=(int)(event.x()-left)/scale,y=(int)(event.y()-top)/scale;
            int steps=Math.max(Math.abs(x-lastX),Math.abs(y-lastY));
            for(int i=0;i<=steps;i++)stamp(steps==0?x:lastX+(x-lastX)*i/steps,steps==0?y:lastY+(y-lastY)*i/steps,event.button()==InputConstants.MOUSE_BUTTON_RIGHT?white:selected);
            lastX=x;lastY=y;return true;
        }
        if(!inside(event.x(),event.y())){lastX=-1;lastY=-1;}
        return super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){lastX=-1;lastY=-1;return super.mouseReleased(event);}
    private boolean inside(double x,double y){return x>=left&&x<left+side&&y>=top&&y<top+side;}
    private void stamp(int x,int y,byte color){
        for(int yy=y-brush/2;yy<y-brush/2+brush;yy++)for(int xx=x-brush/2;xx<x-brush/2+brush;xx++)
            if(xx>=0&&xx<128&&yy>=0&&yy<128)pixels[yy*128+xx]=color;
    }
    private void flood(int x,int y,byte color){
        byte old=pixels[y*128+x];if(old==color)return;
        ArrayDeque<Integer> queue=new ArrayDeque<>();queue.add(y*128+x);pixels[y*128+x]=color;
        while(!queue.isEmpty()){
            int p=queue.removeFirst(),px=p%128,py=p/128;
            if(px>0)offer(queue,p-1,old,color);if(px<127)offer(queue,p+1,old,color);
            if(py>0)offer(queue,p-128,old,color);if(py<127)offer(queue,p+128,old,color);
        }
    }
    private void offer(ArrayDeque<Integer> q,int p,byte old,byte color){if(pixels[p]==old){pixels[p]=color;q.add(p);}}
    private SDL_DialogFileCallback imageDialog;
    private SDL_DialogFileFilter.Buffer imageFilters;
    private java.nio.ByteBuffer filterName, filterPattern;
    private volatile boolean dialogReturned;
    private void releaseDialog(){
        if(imageDialog!=null){imageDialog.free();imageDialog=null;}
        if(imageFilters!=null){imageFilters.free();imageFilters=null;}
        if(filterName!=null){MemoryUtil.memFree(filterName);filterName=null;}
        if(filterPattern!=null){MemoryUtil.memFree(filterPattern);filterPattern=null;}
        dialogReturned=false;
    }
    @Override public void tick(){
        // Linux portal dialogs require SDL event processing. Never block Minecraft.
        if(imageDialog!=null){
            org.lwjgl.sdl.SDLEvents.SDL_PumpEvents();
            if(dialogReturned)releaseDialog();
        }
    }
    private void pickImage(){
        if(importing||imageDialog!=null)return;
        importing=true;status="Choose a PNG or JPEG image...";
        try{
            filterName=MemoryUtil.memUTF8("PNG or JPEG");
            filterPattern=MemoryUtil.memUTF8("png;jpg;jpeg");
            imageFilters=SDL_DialogFileFilter.calloc(1);
            imageFilters.get(0).name(filterName).pattern(filterPattern);
            imageDialog=SDL_DialogFileCallback.create((userdata,files,filter)->{
                String path=files==0?null:MemoryUtil.memUTF8Safe(MemoryUtil.memGetAddress(files));
                String failure=files==0?org.lwjgl.sdl.SDLError.SDL_GetError():null;
                minecraft.execute(()->{
                    if(path==null){importing=false;status=failure==null?"Import cancelled.":"File picker failed: "+failure;return;}
                    status="Loading image...";
                    CompletableFuture.supplyAsync(()->{
                        try{return ImageImport.load(Path.of(path));}catch(Exception ex){throw new java.util.concurrent.CompletionException(ex);}
                    }).whenComplete((image,error)->minecraft.execute(()->{
                        importing=false;
                        if(error!=null){status="Import failed: "+error.getCause().getMessage();return;}
                        snapshot();original=image;applyImage();
                    }));
                });
                dialogReturned=true;
            });
            SDLDialog.SDL_ShowOpenFileDialog(imageDialog,0L,0L,imageFilters,(java.nio.ByteBuffer)null,false);
        }catch(Exception | LinkageError ex){releaseDialog();importing=false;status="Could not open file picker: "+ex.getMessage();}
    }
    private void applyImage(){
        byte[] converted=ImageImport.quantize(ImageImport.resize(original,crop),palette);
        System.arraycopy(converted,0,pixels,0,pixels.length);status="Imported! Draw over it, or Save Painting.";
    }
    private void exportImage(){
        try{
            BufferedImage image=new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);
            for(int y=0;y<128;y++)for(int x=0;x<128;x++)image.setRGB(x,y,palette[Byte.toUnsignedInt(pixels[y*128+x])]);
            Path folder=minecraft.gameDirectory.toPath().resolve("canvasstudio/exports");java.nio.file.Files.createDirectories(folder);
            Path path=folder.resolve("painting-"+java.util.UUID.randomUUID()+".png");
            javax.imageio.ImageIO.write(image,"png",path.toFile());status="Exported to canvasstudio/exports.";
        }catch(Exception ex){status="Export failed: "+ex.getMessage();}
    }
}
