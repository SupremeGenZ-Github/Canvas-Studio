package studio.canvas;
import java.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
public record PaintingAnimation(List<Integer> mapIds) {
 public PaintingAnimation {mapIds=List.copyOf(mapIds);if(mapIds.isEmpty()||mapIds.size()>ArtBundle.MAX_FRAMES||mapIds.stream().anyMatch(i->i<0))throw new IllegalArgumentException("Invalid animation maps");}
 public static final Codec<PaintingAnimation> CODEC=Codec.intRange(0,Integer.MAX_VALUE).listOf(1,ArtBundle.MAX_FRAMES).xmap(PaintingAnimation::new,PaintingAnimation::mapIds);
 public static final StreamCodec<RegistryFriendlyByteBuf,PaintingAnimation> STREAM=new StreamCodec<>(){
  public PaintingAnimation decode(RegistryFriendlyByteBuf b){int n=b.readUnsignedByte();if(n<1||n>ArtBundle.MAX_FRAMES)throw new IllegalArgumentException("Invalid animation count");List<Integer> ids=new ArrayList<>();for(int i=0;i<n;i++)ids.add(b.readVarInt());return new PaintingAnimation(ids);}
  public void encode(RegistryFriendlyByteBuf b,PaintingAnimation a){b.writeByte(a.mapIds.size());for(int id:a.mapIds)b.writeVarInt(id);}
 };
 public static final DataComponentType<PaintingAnimation> TYPE=DataComponentType.<PaintingAnimation>builder().persistent(CODEC).networkSynchronized(STREAM).build();
 public int index(long time,int delay){return (int)Math.floorMod(time/delay,mapIds.size());}
}
