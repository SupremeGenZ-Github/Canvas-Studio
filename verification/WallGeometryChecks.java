import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.Direction;
import java.util.*;
public class WallGeometryChecks {
 record Vertex(float x,float y,float z,int color){}
 static final class Capture implements VertexConsumer {
  final List<Vertex> vertices=new ArrayList<>();private float x,y,z;
  public VertexConsumer addVertex(float x,float y,float z){this.x=x;this.y=y;this.z=z;return this;}
  public VertexConsumer setColor(int c){vertices.add(new Vertex(x,y,z,c));return this;}
  public VertexConsumer setColor(int r,int g,int b,int a){return setColor(a<<24|r<<16|g<<8|b);}
  public VertexConsumer setUv(float u,float v){return this;}
  public VertexConsumer setUv1(int u,int v){return this;}
  public VertexConsumer setUv2(int u,int v){return this;}
  public VertexConsumer setUv3(float u,float v){return this;}
  public VertexConsumer setNormal(float x,float y,float z){return this;}
  public VertexConsumer setLineWidth(float w){return this;}
 }
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void near(double a,double b,String why){check(Math.abs(a-b)<1e-5,why);}
 public static void main(String[] args)throws Exception{
  var type=Class.forName("studio.canvas.client.WallGeometry");var border=type.getDeclaredMethod("border",PoseStack.Pose.class,VertexConsumer.class,Direction.class,int.class,int.class,int.class);border.setAccessible(true);var ghost=type.getDeclaredMethod("ghost",PoseStack.Pose.class,VertexConsumer.class,Direction.class,int.class,int.class,int[].class);ghost.setAccessible(true);
  for(Direction face:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST})for(int w=1;w<=8;w++)for(int h=1;h<=8;h++){
   var capture=new Capture();border.invoke(null,new PoseStack().last(),capture,face,w,h,0xffe8bc35);check(capture.vertices.size()==16,"Four frame bars");var right=face.getCounterClockWise();double min=100,max=-100,minY=100,maxY=-100;
   for(var v:capture.vertices){double u=v.x*right.getStepX()+v.z*right.getStepZ(),depth=v.x*face.getStepX()+v.z*face.getStepZ();check(Math.abs(u)<=w/2.0+1e-5&&Math.abs(v.y)<=h/2.0+1e-5,"Frame stays inside painting bounds");near(depth,-.021,"Frame depth does not scale");check(v.color>>>24==255,"Opaque cosmetic border");min=Math.min(min,u);max=Math.max(max,u);minY=Math.min(minY,v.y);maxY=Math.max(maxY,v.y);}
   near(max-min,w,"Rendered border width");near(maxY-minY,h,"Rendered border height");
  }
  int[] pixels=new int[1024];Arrays.fill(pixels,0xff123456);var capture=new Capture();ghost.invoke(null,new PoseStack().last(),capture,Direction.SOUTH,8,3,pixels);check(capture.vertices.size()==4096,"Bounded 32x32 ghost raster");for(var v:capture.vertices)check(v.color==0x77123456,"Translucent preview pixels");capture.vertices.clear();Arrays.fill(pixels,0);ghost.invoke(null,new PoseStack().last(),capture,Direction.WEST,8,8,pixels);check(capture.vertices.isEmpty(),"Transparent map pixels skipped");
  System.out.println("PASS production wall geometry: all 64 sizes/four directions, cosmetic border dimensions/depth, preview alpha and bounded raster.");
 }
}
