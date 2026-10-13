package studio.canvas;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record GalleryRequest(UUID request,int action,int page,UUID entry) implements CustomPacketPayload {
 public static final int LIST=0,LOAD=1,DELETE=2;
 public static final Type<GalleryRequest> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"gallery_request"));
 public static final StreamCodec<RegistryFriendlyByteBuf,GalleryRequest> CODEC=new StreamCodec<>(){
  public GalleryRequest decode(RegistryFriendlyByteBuf b){return new GalleryRequest(b.readUUID(),b.readUnsignedByte(),b.readVarInt(),b.readUUID());}
  public void encode(RegistryFriendlyByteBuf b,GalleryRequest p){b.writeUUID(p.request);b.writeByte(p.action);b.writeVarInt(p.page);b.writeUUID(p.entry);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(GalleryRequest p,IPayloadContext c){StudioService.gallery(p,c);}
}
