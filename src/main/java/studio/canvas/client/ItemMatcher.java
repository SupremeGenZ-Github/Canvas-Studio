package studio.canvas.client;

import java.awt.image.BufferedImage;
import java.util.*;

/** Offline shape/color matching. Similarity is a visual distance, not an AI confidence. */
public final class ItemMatcher {
 public static final int N=32;
 public record Descriptor(boolean[] mask, float[] edgeDistance, double[] colors, double aspect, double chroma) {}
 public record Reference(String id, Descriptor descriptor) {}
 public record Match(String id, double similarity) {}
 private ItemMatcher() {}
 public static Descriptor describe(BufferedImage image, boolean whiteBackground) {
  int w=image.getWidth(),h=image.getHeight(),minX=w,minY=h,maxX=-1,maxY=-1,count=0;
  boolean[] raw=new boolean[w*h];double red=0,green=0,blue=0;
  for(int y=0;y<h;y++)for(int x=0;x<w;x++) {
   int c=image.getRGB(x,y),r=c>>16&255,g=c>>8&255,b=c&255;
   boolean ink=(c>>>24)>32 && (!whiteBackground || (255-r)*(255-r)+(255-g)*(255-g)+(255-b)*(255-b)>900);
   raw[y*w+x]=ink;
   if(ink){minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);count++;red+=r;green+=g;blue+=b;}
  }
  if(count<8 || Math.max(maxX-minX,maxY-minY)<2)return null;
  int bw=maxX-minX+1,bh=maxY-minY+1;double scale=26.0/Math.max(bw,bh);
  int rw=Math.max(1,(int)Math.round(bw*scale)),rh=Math.max(1,(int)Math.round(bh*scale));
  int ox=(N-rw)/2,oy=(N-rh)/2;
  boolean[] mask=new boolean[N*N];int[] color=new int[N*N];
  // Area coverage preserves thin pen strokes when downsampling a large sketch.
  for(int y=0;y<rh;y++)for(int x=0;x<rw;x++) {
   int sx0=minX+x*bw/rw,sx1=Math.max(sx0+1,minX+(x+1)*bw/rw);
   int sy0=minY+y*bh/rh,sy1=Math.max(sy0+1,minY+(y+1)*bh/rh);
   int hits=0;long rr=0,gg=0,bb=0;
   for(int yy=sy0;yy<sy1;yy++)for(int xx=sx0;xx<sx1;xx++)if(raw[yy*w+xx]){
    int c=image.getRGB(xx,yy);hits++;rr+=c>>16&255;gg+=c>>8&255;bb+=c&255;
   }
   int p=(oy+y)*N+ox+x;
   if(hits>0){mask[p]=true;color[p]=(int)(rr/hits)<<16|(int)(gg/hits)<<8|(int)(bb/hits);}
  }
  // A closed outline describes the enclosed object too, not just its thin ink.
  boolean[] outside=new boolean[N*N];ArrayDeque<Integer> q=new ArrayDeque<>();outside[0]=true;q.add(0);
  while(!q.isEmpty()){int p=q.removeFirst(),x=p%N,y=p/N;
   if(x>0)visit(p-1,mask,outside,q);if(x<N-1)visit(p+1,mask,outside,q);
   if(y>0)visit(p-N,mask,outside,q);if(y<N-1)visit(p+N,mask,outside,q);
  }
  int mean=(int)(red/count)<<16|(int)(green/count)<<8|(int)(blue/count);
  for(int p=0;p<mask.length;p++)if(!mask[p]&&!outside[p]){mask[p]=true;color[p]=mean;}
  List<Integer> edges=new ArrayList<>();double[] histogram=new double[12];double chroma=0;int filled=0;
  for(int p=0;p<mask.length;p++)if(mask[p]){
   int x=p%N,y=p/N,c=color[p],r=c>>16&255,g=c>>8&255,b=c&255;
   if(x==0||y==0||x==N-1||y==N-1||!mask[p-1]||!mask[p+1]||!mask[p-N]||!mask[p+N])edges.add(p);
   int max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b));double saturation=max-min;
   chroma+=saturation/255;filled++;
   if(saturation<28){histogram[max<85?9:max<180?10:11]++;}
   else {float[] hsv=java.awt.Color.RGBtoHSB(r,g,b,null);histogram[Math.min(8,(int)(hsv[0]*9))]++;}
  }
  for(int i=0;i<histogram.length;i++)histogram[i]/=filled;
  float[] distance=new float[N*N];
  for(int p=0;p<distance.length;p++){
   int x=p%N,y=p/N,best=N*N;
   for(int e:edges){int dx=x-e%N,dy=y-e/N;best=Math.min(best,dx*dx+dy*dy);}
   distance[p]=(float)Math.sqrt(best)/N;
  }
  return new Descriptor(mask,distance,histogram,(double)bw/bh,chroma/filled);
 }
 private static void visit(int p,boolean[] mask,boolean[] outside,ArrayDeque<Integer> q){if(!mask[p]&&!outside[p]){outside[p]=true;q.add(p);}}
 public static double similarity(Descriptor sketch,Descriptor reference) {
  int union=0,intersection=0,edgeCount=0;double edge=0,color=0;
  for(int p=0;p<N*N;p++){
   if(sketch.mask[p]||reference.mask[p])union++;
   if(sketch.mask[p]&&reference.mask[p])intersection++;
   if(sketch.edgeDistance[p]==0){edge+=reference.edgeDistance[p];edgeCount++;}
   if(reference.edgeDistance[p]==0){edge+=sketch.edgeDistance[p];edgeCount++;}
  }
  double shape=union==0?1:1-(double)intersection/union;
  double contour=Math.min(1,edge/Math.max(1,edgeCount)*7);
  double aspect=Math.min(1,Math.abs(Math.log(sketch.aspect/reference.aspect))/2);
  for(int i=0;i<sketch.colors.length;i++)color+=Math.abs(sketch.colors[i]-reference.colors[i])/2;
  double colorWeight=sketch.chroma<.12?.08:.28;
  double distance=(1-colorWeight)*(.45*shape+.45*contour+.10*aspect)+colorWeight*color;
  return Math.max(0,Math.min(1,1-distance));
 }
 public static BufferedImage rotate(BufferedImage source,double angle){
  int w=source.getWidth(),h=source.getHeight();double c=Math.abs(Math.cos(angle)),s=Math.abs(Math.sin(angle));
  int rw=(int)Math.ceil(w*c+h*s),rh=(int)Math.ceil(h*c+w*s);
  var rotated=new BufferedImage(rw,rh,BufferedImage.TYPE_INT_RGB);var g=rotated.createGraphics();
  g.setColor(java.awt.Color.WHITE);g.fillRect(0,0,rw,rh);g.translate(rw/2.0,rh/2.0);g.rotate(angle);g.translate(-w/2.0,-h/2.0);g.drawImage(source,0,0,null);g.dispose();return rotated;
 }
 public static List<Match> match(BufferedImage sketch,List<Reference> references,int limit){
  Descriptor descriptor=describe(sketch,true);if(descriptor==null||limit<1)return List.of();
  List<Descriptor> orientations=new ArrayList<>();orientations.add(descriptor);
  for(int angle=45;angle<360;angle+=45){Descriptor rotated=describe(rotate(sketch,Math.toRadians(angle)),true);if(rotated!=null)orientations.add(rotated);}
  Map<String,Double> best=new HashMap<>();
  for(Reference r:references){double score=0;for(Descriptor oriented:orientations)score=Math.max(score,similarity(oriented,r.descriptor));best.merge(r.id,score,Math::max);}
  return best.entrySet().stream().map(e->new Match(e.getKey(),e.getValue()))
   .sorted(Comparator.comparingDouble(Match::similarity).reversed().thenComparing(Match::id)).limit(limit).toList();
 }
}
