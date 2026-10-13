package studio.canvas;
/** Apply the physical wall aspect ratio to square artwork without increasing map resolution. */
public final class PaintLayout {
 private PaintLayout(){}
 public static byte[] apply(byte[] pixels,PaintingSize size,int mode){
  ArtBundle.validatePixels(pixels);if(mode<0||mode>2)throw new IllegalArgumentException("Invalid layout");
  if(mode==0||size.width()==size.height())return pixels.clone();
  byte[] out=new byte[ArtBundle.PIXELS];double aspect=(double)size.width()/size.height();
  double sx=mode==1?Math.max(1,aspect):Math.min(1,aspect),sy=mode==1?Math.max(1,1/aspect):Math.min(1,1/aspect);
  for(int y=0;y<128;y++)for(int x=0;x<128;x++){
   double u=((x+.5)/128-.5)*sx+.5,v=((y+.5)/128-.5)*sy+.5;
   out[y*128+x]=u<0||u>=1||v<0||v>=1?34:pixels[Math.min(127,(int)(v*128))*128+Math.min(127,(int)(u*128))];
  }return out;
 }
}
