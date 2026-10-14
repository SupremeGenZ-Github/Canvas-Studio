package studio.canvas;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
/** Change access on an existing painting through an owner-authorized edit capability. */
public record PaintingLock(UUID request,InteractionHand hand,UUID token,boolean wall,boolean locked) implements CustomPacketPayload {
 public static final Type<PaintingLock> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"painting_lock"));
 public static final StreamCodec<RegistryFriendlyByteBuf,PaintingLock> CODEC=new StreamCodec<>(){
  public PaintingLock decode(RegistryFriendlyByteBuf b){return new PaintingLock(b.readUUID(),b.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,b.readUUID(),b.readBoolean(),b.readBoolean());}
  public void encode(RegistryFriendlyByteBuf b,PaintingLock p){b.writeUUID(p.request);b.writeBoolean(p.hand==InteractionHand.OFF_HAND);b.writeUUID(p.token);b.writeBoolean(p.wall);b.writeBoolean(p.locked);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(PaintingLock request,IPayloadContext context){StudioService.changeLock(request,context);}
}
