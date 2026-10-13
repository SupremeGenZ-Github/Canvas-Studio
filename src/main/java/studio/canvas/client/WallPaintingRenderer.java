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
 public static final class State extends ItemFrameRenderState {public int width=1,height=1;}
 public WallPaintingRenderer(EntityRendererProvider.Context context){super(context);}
 @Override public State createRenderState(){return new State();}
 @Override public void extractRenderState(WallPainting painting,ItemFrameRenderState state,float partialTick){super.extractRenderState(painting,state,partialTick);var size=painting.displaySize();((State)state).width=size.width();((State)state).height=size.height();}
 @Override public void submit(ItemFrameRenderState state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){var sized=(State)state;pose.pushPose();try{if(state.direction.getAxis()==Direction.Axis.X)pose.scale(1,sized.height,sized.width);else pose.scale(sized.width,sized.height,1);super.submit(state,pose,collector,camera);}finally{pose.popPose();}}
}
