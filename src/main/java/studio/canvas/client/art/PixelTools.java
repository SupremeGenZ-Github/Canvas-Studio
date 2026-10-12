package studio.canvas.client.art;
import java.util.*;
/** Integer raster tools, independent of Minecraft widgets and display scaling. */
public final class PixelTools {
 public enum Tool { BRUSH,ERASER,FILL,LINE,RECTANGLE,FILLED_RECTANGLE,ELLIPSE,FILLED_ELLIPSE,EYEDROPPER,DOTTED,DASHED,SPRAY }
 private PixelTools(){}
 public static int snap(int n,int step){return Math.max(0,Math.min(127,Math.round((float)n/step)*step));}
 public static void pixel(Artwork d,int x,int y,byte c){write(d.pixels(),x,y,c);if(d.mirrorX)write(d.pixels(),127-x,y,c);if(d.mirrorY)write(d.pixels(),x,127-y,c);if(d.mirrorX&&d.mirrorY)write(d.pixels(),127-x,127-y,c);}
 private static void write(byte[] p,int x,int y,byte c){if(x>=0&&x<128&&y>=0&&y<128)p[y*128+x]=c;}
 public static void stamp(Artwork d,int x,int y,byte c){for(int yy=y-d.brush/2;yy<y-d.brush/2+d.brush;yy++)for(int xx=x-d.brush/2;xx<x-d.brush/2+d.brush;xx++)pixel(d,xx,yy,c);}
 public static void line(Artwork d,int x0,int y0,int x1,int y1,byte c,Tool tool,int offset){int steps=Math.max(Math.abs(x1-x0),Math.abs(y1-y0));for(int i=0;i<=steps;i++){int n=offset+i;if(tool==Tool.DOTTED&&n%4!=0||tool==Tool.DASHED&&n%10>=6)continue;int x=steps==0?x0:x0+(x1-x0)*i/steps,y=steps==0?y0:y0+(y1-y0)*i/steps;if(tool==Tool.SPRAY){var random=new Random((long)n*73471+x*31+y);for(int k=0;k<Math.max(2,d.brush);k++)pixel(d,x+random.nextInt(d.brush*2+1)-d.brush,y+random.nextInt(d.brush*2+1)-d.brush,c);}else stamp(d,x,y,c);}}
 public static void shape(Artwork d,Tool t,int ax,int ay,int bx,int by,byte c){int x0=Math.min(ax,bx),x1=Math.max(ax,bx),y0=Math.min(ay,by),y1=Math.max(ay,by);if(t==Tool.LINE){line(d,ax,ay,bx,by,c,Tool.BRUSH,0);return;}
  if(t==Tool.RECTANGLE||t==Tool.FILLED_RECTANGLE){if(t==Tool.FILLED_RECTANGLE)for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++)pixel(d,x,y,c);else{line(d,x0,y0,x1,y0,c,Tool.BRUSH,0);line(d,x1,y0,x1,y1,c,Tool.BRUSH,0);line(d,x1,y1,x0,y1,c,Tool.BRUSH,0);line(d,x0,y1,x0,y0,c,Tool.BRUSH,0);}return;}
  double rx=(x1-x0)/2.0,ry=(y1-y0)/2.0,cx=(x0+x1)/2.0,cy=(y0+y1)/2.0;if(rx==0||ry==0){line(d,x0,y0,x1,y1,c,Tool.BRUSH,0);return;}
  if(t==Tool.FILLED_ELLIPSE){for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++)if(Math.pow((x-cx)/rx,2)+Math.pow((y-cy)/ry,2)<=1.001)pixel(d,x,y,c);}else{int n=Math.max(16,(int)(Math.max(rx,ry)*8));for(int i=0;i<n;i++){double a=2*Math.PI*i/n;stamp(d,(int)Math.round(cx+rx*Math.cos(a)),(int)Math.round(cy+ry*Math.sin(a)),c);}}
 }
 public static void flood(Artwork d,int x,int y,byte c){fill(d,x,y,c);if(d.mirrorX)fill(d,127-x,y,c);if(d.mirrorY)fill(d,x,127-y,c);if(d.mirrorX&&d.mirrorY)fill(d,127-x,127-y,c);}
 private static void fill(Artwork d,int x,int y,byte c){byte[] p=d.pixels();byte old=p[y*128+x];if(old==c)return;var q=new ArrayDeque<Integer>();q.add(y*128+x);p[y*128+x]=c;while(!q.isEmpty()){int n=q.removeFirst(),px=n%128,py=n/128;if(px>0)offer(p,q,n-1,old,c);if(px<127)offer(p,q,n+1,old,c);if(py>0)offer(p,q,n-128,old,c);if(py<127)offer(p,q,n+128,old,c);}}
 private static void offer(byte[] p,ArrayDeque<Integer> q,int n,byte old,byte c){if(p[n]==old){p[n]=c;q.add(n);}}
}
