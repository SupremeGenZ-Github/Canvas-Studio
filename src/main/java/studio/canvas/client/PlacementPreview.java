package studio.canvas.client;
import studio.canvas.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
/** A local-only, non-pickable ghost uses the same native map renderer and geometry. */
public final class PlacementPreview {
 private static WallPainting ghost;
 private PlacementPreview(){}
 static void clear(){if(ghost!=null){ghost.discard();ghost=null;}}
 public static void tick(PlayerTickEvent.Post event){
  var mc=Minecraft.getInstance();if(event.getEntity()!=mc.player)return;
  if(mc.player==null||mc.level==null||mc.gui.screen()!=null||mc.player.isShiftKeyDown()||!(mc.hitResult instanceof BlockHitResult hit)||!hit.getDirection().getAxis().isHorizontal()){clear();return;}
  var held=mc.player.getMainHandItem();if(!held.is(Items.FILLED_MAP))held=mc.player.getOffhandItem();if(!held.is(Items.FILLED_MAP)){clear();return;}
  var data=MapItem.getSavedData(held,mc.level);if(data==null||!data.locked){clear();return;}
  var face=hit.getDirection();var anchor=hit.getBlockPos().relative(face);
  if(ghost==null||ghost.level()!=mc.level||ghost.isRemoved()){
   clear();ghost=new WallPainting(mc.level,anchor,face);ghost.preview=true;int id=Integer.MIN_VALUE+320;while(mc.level.getEntity(id)!=null)id++;ghost.setId(id);mc.level.addEntity(ghost);
  }
  ghost.previewAt(anchor,face);ghost.setDisplaySize(held.getOrDefault(PaintingSize.TYPE,PaintingSize.ONE));ghost.setItem(held.copyWithCount(1),false);
  boolean loaded=true;for(var front:ghost.displaySize().cells(anchor,face))if(!mc.level.hasChunkAt(front)||!mc.level.hasChunkAt(front.relative(face.getOpposite())))loaded=false;
  ghost.previewValid=loaded&&mc.player.mayBuild()&&ghost.survives();
 }
 public static void hud(RenderGuiEvent.Post event){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null){clear();return;}if(ghost==null||ghost.isRemoved())return;var g=event.getGuiGraphics();String message=ghost.displaySize().width()+" x "+ghost.displaySize().height()+" painting: "+(ghost.previewValid?"space clear - right-click to place":"blocked or unsupported");g.centeredText(mc.font,message,g.guiWidth()/2,g.guiHeight()-60,ghost.previewValid?0xff4bdf78:0xffef5555);
 }
}
