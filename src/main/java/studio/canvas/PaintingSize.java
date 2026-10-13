package studio.canvas;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import java.util.*;
/** Wall display dimensions; painting pixels remain 128 x 128. */
public record PaintingSize(int width,int height) {
 public static final int MAX=8;
 public static final PaintingSize ONE=new PaintingSize(1,1);
 public PaintingSize{if(!valid(width,height))throw new IllegalArgumentException("Painting size must be 1 to 8 blocks per side");}
 public static boolean valid(int width,int height){return width>=1&&width<=MAX&&height>=1&&height<=MAX;}
 public static PaintingSize fromSaved(int width,int height){return valid(width,height)?new PaintingSize(width,height):ONE;}
 public static final Codec<PaintingSize> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(1,MAX).fieldOf("width").forGetter(PaintingSize::width),Codec.intRange(1,MAX).fieldOf("height").forGetter(PaintingSize::height)).apply(i,PaintingSize::new));
 public static final StreamCodec<RegistryFriendlyByteBuf,PaintingSize> STREAM=new StreamCodec<>(){
  public PaintingSize decode(RegistryFriendlyByteBuf b){return new PaintingSize(b.readUnsignedByte(),b.readUnsignedByte());}
  public void encode(RegistryFriendlyByteBuf b,PaintingSize p){b.writeByte(p.width);b.writeByte(p.height);}
 };
 public static final DataComponentType<PaintingSize> TYPE=DataComponentType.<PaintingSize>builder().persistent(CODEC).networkSynchronized(STREAM).build();
 /** Front cells starting at the lower-left tile as seen by the placer. */
 public List<BlockPos> cells(BlockPos anchor,Direction face){
  if(!face.getAxis().isHorizontal())throw new IllegalArgumentException("Vertical wall required");
  var right=face.getCounterClockWise();List<BlockPos> result=new ArrayList<>(width*height);
  for(int y=0;y<height;y++)for(int x=0;x<width;x++)result.add(anchor.relative(right,x).above(y));
  return List.copyOf(result);
 }
 public AABB bounds(BlockPos anchor,Direction face){
  if(!face.getAxis().isHorizontal())return ONE.bounds(anchor,Direction.SOUTH);
  var right=face.getCounterClockWise();double cx=anchor.getX()+.5-face.getStepX()*.46875+right.getStepX()*(width-1)/2.0,cy=anchor.getY()+.5+(height-1)/2.0,cz=anchor.getZ()+.5-face.getStepZ()*.46875+right.getStepZ()*(width-1)/2.0;
  double hx=face.getAxis()==Direction.Axis.X?1.0/32:width/2.0,hz=face.getAxis()==Direction.Axis.Z?1.0/32:width/2.0;
  return new AABB(cx-hx,cy-height/2.0,cz-hz,cx+hx,cy+height/2.0,cz+hz);
 }
}
