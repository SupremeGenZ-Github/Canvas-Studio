import java.nio.file.*;
import javax.imageio.ImageIO;

/** Checks game-sized item sprites and catches unintended holes in canvas paper. */
public class TextureChecks {
 public static void main(String[] args)throws Exception {
  Path root=Path.of("src/main/resources/assets/canvasstudio/textures/item");
  for(String name:new String[]{"canvas","quill","molder_canvas","molder_quill","infinity_canvas","infinity_quill"}) {
   var image=ImageIO.read(root.resolve(name+".png").toFile());
   if(image==null||image.getWidth()!=32||image.getHeight()!=32||!image.getColorModel().hasAlpha())throw new AssertionError("32x32 alpha sprite required: "+name);
   int clear=0,opaque=0;
   for(int y=0;y<32;y++)for(int x=0;x<32;x++) {
    int alpha=image.getRGB(x,y)>>>24;
    if(alpha==0)clear++;else if(alpha==255)opaque++;else throw new AssertionError("Unfiltered pixel alpha required: "+name);
   }
   if(clear==0||opaque<50)throw new AssertionError("Transparent exterior and visible sprite required: "+name);
   if(name.endsWith("canvas")||name.equals("canvas"))for(int y=10;y<22;y++)for(int x=10;x<22;x++) {
    int pixel=image.getRGB(x,y);
    if((pixel>>>24)!=255||((pixel>>>16)&255)<120||((pixel>>>8)&255)<100)throw new AssertionError("Opaque blank paper required: "+name+" at "+x+","+y);
   }
   System.out.println("PASS 32x32 pixel texture, alpha and canvas paper: "+name);
  }
 }
}
