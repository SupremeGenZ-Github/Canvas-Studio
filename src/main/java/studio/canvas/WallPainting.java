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
 private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DISPLAY_WIDTH=net.minecraft.network.syncher.SynchedEntityData.defineId(WallPainting.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
 private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DISPLAY_HEIGHT=net.minecraft.network.syncher.SynchedEntityData.defineId(WallPainting.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(DISPLAY_WIDTH,1);builder.define(DISPLAY_HEIGHT,1);}
 public PaintingSize displaySize(){return PaintingSize.fromSaved(getEntityData().get(DISPLAY_WIDTH),getEntityData().get(DISPLAY_HEIGHT));}
 public void setDisplaySize(PaintingSize size){getEntityData().set(DISPLAY_WIDTH,size.width());getEntityData().set(DISPLAY_HEIGHT,size.height());recalculateBoundingBox();}
 @Override protected net.minecraft.world.phys.AABB calculateBoundingBox(BlockPos anchor,Direction face){return displaySize().bounds(anchor,face);}
 @Override protected net.minecraft.world.phys.AABB getPopBox(){return getBoundingBox();}
 @Override public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key.equals(DISPLAY_WIDTH)||key.equals(DISPLAY_HEIGHT))recalculateBoundingBox();}
 @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output){super.addAdditionalSaveData(output);output.putInt("CanvasWidth",displaySize().width());output.putInt("CanvasHeight",displaySize().height());}
 @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input){super.readAdditionalSaveData(input);var fallback=getItem().getOrDefault(PaintingSize.TYPE,PaintingSize.ONE);setDisplaySize(PaintingSize.fromSaved(input.getIntOr("CanvasWidth",fallback.width()),input.getIntOr("CanvasHeight",fallback.height())));}
 @Override public boolean survives(){
  var face=getDirection();if(!face.getAxis().isHorizontal())return false;
  // Existing paintings must not drop simply because an adjacent chunk unloads.
  for(BlockPos front:displaySize().cells(getPos(),face))if(!level().hasChunkAt(front)||!level().hasChunkAt(front.relative(face.getOpposite())))return true;
  if(!super.survives())return false;
  for(BlockPos front:displaySize().cells(getPos(),face)){var back=front.relative(face.getOpposite());if(!level().hasChunkAt(back)||!level().hasChunkAt(front)||!level().getBlockState(back).isFaceSturdy(level(),back,face))return false;}
  return true;
 }
 public WallPainting(EntityType<? extends ItemFrame> type,Level level){super(type,level);setInvisible(true);}
 public WallPainting(Level level,BlockPos position,Direction face){super(CanvasStudio.WALL_PAINTING.get(),level,position,face);setInvisible(true);}
 public boolean preview,previewValid;
 @Override public boolean isPickable(){return !preview&&super.isPickable();}
 @Override public void tick(){
  super.tick();if(!isInvisible())setInvisible(true);
  if(level() instanceof ServerLevel){var sequence=getItem().get(PaintingAnimation.TYPE);if(sequence!=null&&sequence.mapIds().size()>1){var options=StudioSessions.properties(getItem()).options();int id=sequence.mapIds().get(sequence.index(level().getGameTime(),options.delay()));var current=getItem().get(net.minecraft.core.component.DataComponents.MAP_ID);if(current==null||current.id()!=id){var frame=getItem().copy();frame.set(net.minecraft.core.component.DataComponents.MAP_ID,new net.minecraft.world.level.saveddata.maps.MapId(id));setItem(frame,false);}}}
 }
 @Override public net.minecraft.world.InteractionResult interact(Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.Vec3 hit){
  if(preview)return net.minecraft.world.InteractionResult.PASS;
  if(player.isShiftKeyDown()){if(player instanceof net.minecraft.server.level.ServerPlayer p)StudioService.openWall(p,this,hand);return net.minecraft.world.InteractionResult.SUCCESS;}
  if(player instanceof net.minecraft.server.level.ServerPlayer p&&!StudioSessions.wallPermission(p,this))return net.minecraft.world.InteractionResult.FAIL;
  return super.interact(player,hand,hit);
 }
 public void previewAt(BlockPos anchor,Direction face){this.pos=anchor;setDirection(face);}

 @Override protected ItemStack getFrameItemStack(){return new ItemStack(Items.AIR);}
 @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount){
  if(preview)return false;
  if(source.getEntity() instanceof Player player&&!player.mayBuild())return false;
  if(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer p&&!StudioSessions.wallPermission(p,this))return false;
  boolean accepted=super.hurtServer(level,source,amount);
  if(accepted&&getItem().isEmpty()&&!isRemoved())discard();
  return accepted;
 }
}
