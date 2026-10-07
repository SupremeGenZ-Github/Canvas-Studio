import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.InteractionHand;
import studio.canvas.client.CanvasScreen;
import java.lang.reflect.Field;
import java.util.Arrays;

/** Headless input fixture; it never starts a game or opens a window. */
public class CanvasInputChecks {
 static void check(boolean result,String message){if(!result)throw new AssertionError(message);}
 static Field field(String name)throws Exception{var f=CanvasScreen.class.getDeclaredField(name);f.setAccessible(true);return f;}
 static MouseButtonEvent event(double x,double y,int button){return new MouseButtonEvent(x,y,new MouseButtonInfo(button,0));}
 public static void main(String[] args)throws Exception {
  // Screen's constructor requires the client singleton. Allocate a test-only shell
  // without running Minecraft's constructor, graphics initialization or world loading.
  var uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);
  var unsafe=(sun.misc.Unsafe)uf.get(null);
  var instance=Minecraft.class.getDeclaredField("instance");instance.setAccessible(true);
  Object previous=instance.get(null);instance.set(null,unsafe.allocateInstance(Minecraft.class));
  try {
   for(int scale=1;scale<=3;scale++) {
    var screen=new CanvasScreen(InteractionHand.MAIN_HAND);
    int left=40,top=46,side=128*scale,tools=left+side+12;
    field("left").setInt(screen,left);field("top").setInt(screen,top);
    field("scale").setInt(screen,scale);field("side").setInt(screen,side);field("tools").setInt(screen,tools);
    byte[] pixels=(byte[])field("pixels").get(screen);byte white=pixels[0];
    check(screen.mouseClicked(event(left+64*scale+.5,top+64*scale+.5,InputConstants.MOUSE_BUTTON_LEFT),false),"Left click handled");
    byte ink=pixels[64*128+64];check(ink!=white,"Left click paints instead of erasing at scale "+scale);
    screen.mouseDragged(event(left+80*scale+.5,top+80*scale+.5,InputConstants.MOUSE_BUTTON_LEFT),16*scale,16*scale);
    for(int p=64;p<=80;p++)check(pixels[p*128+p]==ink,"Continuous drag line at "+p);
    screen.mouseReleased(event(left+80*scale,top+80*scale,InputConstants.MOUSE_BUTTON_LEFT));
    screen.mouseClicked(event(left+64*scale+.5,top+64*scale+.5,InputConstants.MOUSE_BUTTON_RIGHT),false);
    check(pixels[64*128+64]==white,"Right click erases");
    screen.mouseDragged(event(left+80*scale+.5,top+80*scale+.5,InputConstants.MOUSE_BUTTON_RIGHT),16*scale,16*scale);
    for(int p=64;p<=80;p++)check(pixels[p*128+p]==white,"Continuous eraser line");
    screen.mouseReleased(event(left+80*scale,top+80*scale,InputConstants.MOUSE_BUTTON_RIGHT));
    byte[] swatches=(byte[])field("swatches").get(screen);
    check(screen.mouseClicked(event(tools+1,top+12+1,InputConstants.MOUSE_BUTTON_LEFT),false),"Palette click handled");
    byte chosen=field("selected").getByte(screen);check(chosen==swatches[8],"Palette chooses red swatch");
    byte[] before=pixels.clone();screen.mouseClicked(event(left+32*scale,top+32*scale,InputConstants.MOUSE_BUTTON_MIDDLE),false);
    check(Arrays.equals(before,pixels),"Middle click does not paint");
    field("fillTool").setBoolean(screen,true);
    screen.mouseClicked(event(left+10*scale,top+10*scale,InputConstants.MOUSE_BUTTON_LEFT),false);
    for(byte color:pixels)check(color==chosen,"Fill paints the connected blank canvas");
    screen.mouseClicked(event(left+10*scale,top+10*scale,InputConstants.MOUSE_BUTTON_RIGHT),false);
    for(byte color:pixels)check(color==white,"Right-click fill erases");
    System.out.println("PASS editor input at scale "+scale+": left draw/drag, right erase/drag, palette, middle-click rejection, fill");
   }
  }finally{instance.set(null,previous);}
 }
}
