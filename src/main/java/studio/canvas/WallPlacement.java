package studio.canvas;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
public final class WallPlacement {
 private WallPlacement(){}
 public static boolean eligible(Direction face,boolean locked,boolean permission,boolean survives){return face!=null&&face.getAxis().isHorizontal()&&locked&&permission&&survives;}
 public static void place(PlayerInteractEvent.RightClickBlock event){
  if(event.isCanceled()||event.getEntity().isShiftKeyDown()||!event.getItemStack().is(Items.FILLED_MAP))return;
  Direction face=event.getFace();if(face==null||!face.getAxis().isHorizontal())return;
  var data=MapItem.getSavedData(event.getItemStack(),event.getLevel());if(data==null||!data.locked)return;
  event.setCanceled(true);
  if(!(event.getLevel() instanceof ServerLevel level)||!(event.getEntity() instanceof ServerPlayer player)){event.setCancellationResult(InteractionResult.SUCCESS);return;}
  var anchor=event.getPos().relative(face);var held=player.getItemInHand(event.getHand());
  boolean permission=player.mayBuild()&&level.mayInteract(player,event.getPos())&&player.mayUseItemAt(anchor,face,held);
  if(!permission){event.setCancellationResult(InteractionResult.FAIL);return;}
  var size=held.getOrDefault(PaintingSize.TYPE,PaintingSize.ONE);
  for(var front:size.cells(anchor,face)){var back=front.relative(face.getOpposite());if(!level.hasChunkAt(back)||!level.hasChunkAt(front)||!level.mayInteract(player,back)||!player.mayUseItemAt(front,face,held)){event.setCancellationResult(InteractionResult.FAIL);return;}}
  var painting=new WallPainting(level,anchor,face);painting.setDisplaySize(size);
  if(!eligible(face,true,permission,painting.survives())){event.setCancellationResult(InteractionResult.FAIL);player.sendSystemMessage(net.minecraft.network.chat.Component.literal("The whole painting needs a supported wall and an empty space."),false);return;}
  var display=held.copyWithCount(1);var properties=StudioSessions.properties(display);
  if(properties.owner().equals(PaintingProperties.NONE))display.set(PaintingProperties.TYPE,new PaintingProperties(player.getUUID(),properties.options(),properties.revision()));
  painting.setItem(display,false);
  if(!level.addFreshEntity(painting)){event.setCancellationResult(InteractionResult.FAIL);return;}
  painting.playPlacementSound();if(!player.hasInfiniteMaterials())held.shrink(1);
  player.inventoryMenu.broadcastChanges();event.setCancellationResult(InteractionResult.SUCCESS);
 }
}
