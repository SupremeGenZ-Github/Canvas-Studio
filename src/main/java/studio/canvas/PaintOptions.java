package studio.canvas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Player-selected display settings; identity and revision are server-owned. */
public record PaintOptions(int frame, int fit, int delay, boolean locked) {
 public static final String[] FRAMES=FrameMaterials.NAMES;
 public static final String[] FITS={"Stretch","Fit","Crop"};
 public static final PaintOptions DEFAULT=new PaintOptions(0,0,20,false);
 public static final Codec<PaintOptions> CODEC=RecordCodecBuilder.create(i->i.group(
  Codec.intRange(0,11).fieldOf("frame").forGetter(PaintOptions::frame),
  Codec.intRange(0,2).fieldOf("fit").forGetter(PaintOptions::fit),
  Codec.intRange(10,200).fieldOf("delay").forGetter(PaintOptions::delay),
  Codec.BOOL.fieldOf("locked").forGetter(PaintOptions::locked)).apply(i,PaintOptions::new));
 public PaintOptions {if(!FrameMaterials.valid(frame)||fit<0||fit>2||delay<10||delay>200)throw new IllegalArgumentException("Invalid painting settings");}
 public static PaintOptions read(RegistryFriendlyByteBuf b){return new PaintOptions(b.readUnsignedByte(),b.readUnsignedByte(),b.readUnsignedByte(),b.readBoolean());}
 public void write(RegistryFriendlyByteBuf b){b.writeByte(frame);b.writeByte(fit);b.writeByte(delay);b.writeBoolean(locked);}
}
