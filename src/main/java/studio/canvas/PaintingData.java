package studio.canvas;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record PaintingData(InteractionHand hand,UUID request,UUID token,String title,byte[] pixels) implements CustomPacketPayload {
 public static final Type<PaintingData> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"painting_data"));
 public static final StreamCodec<RegistryFriendlyByteBuf,PaintingData> CODEC=new StreamCodec<>(){
  public PaintingData decode(RegistryFriendlyByteBuf b){var hand=b.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;UUID request=b.readUUID(),token=b.readUUID();String title=b.readUtf(64);byte[] pixels=new byte[SavePainting.PIXELS];b.readBytes(pixels);return new PaintingData(hand,request,token,title,pixels);}
  public void encode(RegistryFriendlyByteBuf b,PaintingData p){if(p.pixels.length!=SavePainting.PIXELS)throw new IllegalArgumentException("Pixel count");b.writeBoolean(p.hand==InteractionHand.OFF_HAND);b.writeUUID(p.request);b.writeUUID(p.token);b.writeUtf(p.title,64);b.writeBytes(p.pixels);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(PaintingData p,IPayloadContext context){studio.canvas.client.CanvasClient.painting(p);}
}
