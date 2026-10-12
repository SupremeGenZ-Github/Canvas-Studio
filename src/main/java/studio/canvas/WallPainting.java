package studio.canvas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
/** Saved-map hanging entity using vanilla map synchronization/rendering, without a frame model or frame item. */
public final class WallPainting extends ItemFrame {
 public WallPainting(EntityType<? extends ItemFrame> type,Level level){super(type,level);setInvisible(true);}
 public WallPainting(Level level,BlockPos position,Direction face){super(CanvasStudio.WALL_PAINTING.get(),level,position,face);setInvisible(true);}
 @Override public void tick(){super.tick();if(!isInvisible())setInvisible(true);}
 @Override protected ItemStack getFrameItemStack(){return new ItemStack(Items.AIR);}
 @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount){
  if(source.getEntity() instanceof Player player&&!player.mayBuild())return false;
  boolean accepted=super.hurtServer(level,source,amount);
  if(accepted&&getItem().isEmpty()&&!isRemoved())discard();
  return accepted;
 }
}
