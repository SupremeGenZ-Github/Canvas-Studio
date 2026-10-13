package studio.canvas.client;
import studio.canvas.WallPainting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
/** Scale only the two wall-plane axes; preserve vanilla map orientation, lighting and depth. */
public final class WallPaintingRenderer extends ItemFrameRenderer<WallPainting> {
 public static final class State extends ItemFrameRenderState {public int width=1,height=1,frame;public boolean preview,valid;public int[] pixels;}
 public WallPaintingRenderer(EntityRendererProvider.Context context){super(context);}
 @Override public State createRenderState(){return new State();}
 @Override public void extractRenderState(WallPainting painting,ItemFrameRenderState state,float partialTick){
  super.extractRenderState(painting,state,partialTick);var size=painting.displaySize();var s=(State)state;s.width=size.width();s.height=size.height();s.frame=studio.canvas.StudioSessions.properties(painting.getItem()).options().frame();s.preview=painting.preview;s.valid=painting.previewValid;s.pixels=null;
  if(s.preview){var data=net.minecraft.world.item.MapItem.getSavedData(painting.getItem(),painting.level());if(data!=null){s.pixels=new int[32*32];for(int y=0;y<32;y++)for(int x=0;x<32;x++)s.pixels[y*32+x]=net.minecraft.world.level.material.MapColor.getColorFromPackedId(Byte.toUnsignedInt(data.colors[(y*4)*128+x*4]));}}
 }
 // Vanilla applies a vertical render offset before submit and cancels it inside submit.
 // Return zero so enlarging the wall plane cannot multiply that cancellation offset.
 @Override public net.minecraft.world.phys.Vec3 getRenderOffset(ItemFrameRenderState state){return net.minecraft.world.phys.Vec3.ZERO;}
 public static void scaleWall(PoseStack pose,Direction face,int width,int height){if(face.getAxis()==Direction.Axis.X)pose.scale(1,height,width);else pose.scale(width,height,1);}
 @Override public void submit(ItemFrameRenderState state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){
  var s=(State)state;
  if(!s.preview){pose.pushPose();try{scaleWall(pose,state.direction,s.width,s.height);super.submit(state,pose,collector,camera);}finally{pose.popPose();}}
  if(s.preview||s.frame!=0){int[] colors={0,0xff976239,0xffe8bc35,0xffbbc3ce,0xff3acac4};int color=s.preview?(s.valid?0xff4bdf78:0xffef5555):colors[s.frame];
   var direction=state.direction;int width=s.width,height=s.height;boolean preview=s.preview;int[] pixels=s.pixels;
   collector.submitCustomGeometry(pose,net.minecraft.client.renderer.rendertype.RenderTypes.debugQuads(),(transform,out)->{if(preview&&pixels!=null)WallGeometry.ghost(transform,out,direction,width,height,pixels);WallGeometry.border(transform,out,direction,width,height,color);});
  }
 }
}
