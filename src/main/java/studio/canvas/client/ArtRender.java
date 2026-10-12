package studio.canvas.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
final class ArtRender {
 static void draw(GuiGraphicsExtractor g,byte[] pixels,int[] palette,int x,int y,int size){drawPixels(g,i->palette[Byte.toUnsignedInt(pixels[i])],x,y,size);}
 static void draw(GuiGraphicsExtractor g,int[] colors,int x,int y,int size){drawPixels(g,i->colors[i],x,y,size);}
 private static void drawPixels(GuiGraphicsExtractor g,java.util.function.IntUnaryOperator color,int x,int y,int size){int n=Math.min(128,size);for(int yy=0;yy<n;yy++)for(int xx=0;xx<n;){int start=xx,c=color.applyAsInt((yy*128/n)*128+xx*128/n);while(xx<n&&color.applyAsInt((yy*128/n)*128+xx*128/n)==c)xx++;g.fill(x+start*size/n,y+yy*size/n,x+xx*size/n,y+(yy+1)*size/n,c);}}
}
