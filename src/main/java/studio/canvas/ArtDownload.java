package studio.canvas;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record ArtDownload(UUID request,int action,InteractionHand hand,UUID token,String title,PaintingSize size,PaintOptions options,int index,int count,byte[] pixels) implements CustomPacketPayload {
 public static final Type<ArtDownload> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"art_download"));
 public static final StreamCodec<RegistryFriendlyByteBuf,ArtDownload> CODEC=new StreamCodec<>(){
  public ArtDownload decode(RegistryFriendlyByteBuf b){var p=ArtUpload.CODEC.decode(b);return new ArtDownload(p.transfer(),p.action(),p.hand(),p.token(),p.title(),p.size(),p.options(),p.index(),p.count(),p.pixels());}
  public void encode(RegistryFriendlyByteBuf b,ArtDownload p){ArtUpload.CODEC.encode(b,new ArtUpload(p.request,p.action,p.hand,p.token,p.title,p.size,p.options,p.index,p.count,p.pixels));}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(ArtDownload p,IPayloadContext c){studio.canvas.client.StudioDownloads.receive(p);}
}
