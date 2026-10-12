package studio.canvas.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
final class ArtRender {
 static void draw(GuiGraphicsExtractor g,byte[] pixels,int[] palette,int x,int y,int size){int[] colors=new int[pixels.length];for(int i=0;i<pixels.length;i++)colors[i]=palette[Byte.toUnsignedInt(pixels[i])];draw(g,colors,x,y,size);}
 static void draw(GuiGraphicsExtractor g,int[] colors,int x,int y,int size){for(int yy=0;yy<128;yy++)for(int xx=0;xx<128;){int start=xx,c=colors[yy*128+xx];while(xx<128&&colors[yy*128+xx]==c)xx++;int y0=y+yy*size/128,y1=y+(yy+1)*size/128;if(y1>y0)g.fill(x+start*size/128,y0,x+xx*size/128,y1,c);}}
}
