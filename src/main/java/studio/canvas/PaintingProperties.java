package studio.canvas;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
public record PaintingProperties(UUID owner, PaintOptions options, int revision) {
 public static final UUID NONE=new UUID(0,0);
 public static final PaintingProperties DEFAULT=new PaintingProperties(NONE,PaintOptions.DEFAULT,0);
 public PaintingProperties {if(owner==null||options==null||revision<0)throw new IllegalArgumentException("Invalid painting identity");}
 public static final Codec<PaintingProperties> CODEC=RecordCodecBuilder.create(i->i.group(
  Codec.STRING.comapFlatMap(text->{try{return com.mojang.serialization.DataResult.success(UUID.fromString(text));}catch(IllegalArgumentException e){return com.mojang.serialization.DataResult.error(()->"Invalid owner UUID");}},UUID::toString).fieldOf("owner").forGetter(PaintingProperties::owner),
  PaintOptions.CODEC.fieldOf("options").forGetter(PaintingProperties::options),
  Codec.intRange(0,Integer.MAX_VALUE).fieldOf("revision").forGetter(PaintingProperties::revision)).apply(i,PaintingProperties::new));
 public static final StreamCodec<RegistryFriendlyByteBuf,PaintingProperties> STREAM=new StreamCodec<>(){
  public PaintingProperties decode(RegistryFriendlyByteBuf b){return new PaintingProperties(b.readUUID(),PaintOptions.read(b),b.readVarInt());}
  public void encode(RegistryFriendlyByteBuf b,PaintingProperties p){b.writeUUID(p.owner);p.options.write(b);b.writeVarInt(p.revision);}
 };
 public static final DataComponentType<PaintingProperties> TYPE=DataComponentType.<PaintingProperties>builder().persistent(CODEC).networkSynchronized(STREAM).build();
 public boolean mayEdit(UUID player,boolean admin){return !options.locked()||owner.equals(NONE)||owner.equals(player)||admin;}
 public boolean mayChangeLock(UUID player,boolean admin){return owner.equals(NONE)||owner.equals(player)||admin;}
}
