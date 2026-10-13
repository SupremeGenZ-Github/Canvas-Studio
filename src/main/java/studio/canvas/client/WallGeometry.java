package studio.canvas.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
/** Colored quads in the actual wall plane; no external textures or renderer state changes. */
final class WallGeometry {
 private WallGeometry(){}
 static void quad(PoseStack.Pose pose,VertexConsumer out,Direction face,float x0,float y0,float x1,float y1,float depth,int color){
  var r=face.getCounterClockWise();vertex(pose,out,r,face,x0,y0,depth,color);vertex(pose,out,r,face,x1,y0,depth,color);vertex(pose,out,r,face,x1,y1,depth,color);vertex(pose,out,r,face,x0,y1,depth,color);
 }
 private static void vertex(PoseStack.Pose p,VertexConsumer v,Direction r,Direction f,float x,float y,float z,int c){v.addVertex(p,r.getStepX()*x+f.getStepX()*z,y,r.getStepZ()*x+f.getStepZ()*z).setColor(c);}
 static int shade(int c,float amount){return (c&0xff000000)|Math.min(255,(int)(((c>>16)&255)*amount))<<16|Math.min(255,(int)(((c>>8)&255)*amount))<<8|Math.min(255,(int)((c&255)*amount));}
 static void border(PoseStack.Pose pose,VertexConsumer out,Direction face,int width,int height,int color){
  float x=width/2f,y=height/2f,t=.0625f,d=-.021f;
  quad(pose,out,face,-x,-y,x,-y+t,d,shade(color,.65f));quad(pose,out,face,-x,y-t,x,y,d,shade(color,1.2f));
  quad(pose,out,face,-x,-y+t,-x+t,y-t,d,shade(color,.8f));quad(pose,out,face,x-t,-y+t,x,y-t,d,color);
 }
 static void ghost(PoseStack.Pose pose,VertexConsumer out,Direction face,int width,int height,int[] colors){
  int side=32;float stepX=width/(float)side,stepY=height/(float)side;
  for(int y=0;y<side;y++)for(int x=0;x<side;x++){int color=colors[y*side+x];if((color>>>24)==0)continue;float left=-width/2f+x*stepX,top=height/2f-y*stepY;quad(pose,out,face,left,top-stepY,left+stepX,top,-.018f,(color&0xffffff)|0x77000000);}
 }
}
