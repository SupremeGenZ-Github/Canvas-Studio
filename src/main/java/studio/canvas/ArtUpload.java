package studio.canvas;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
/** One 16 KiB image per packet, below the serverbound custom-payload limit. */
public record ArtUpload(UUID transfer,int action,InteractionHand hand,UUID token,String title,PaintingSize size,PaintOptions options,int index,int count,byte[] pixels) implements CustomPacketPayload {
 public static final int HELD=0,WALL=1,PUBLISH=2;
 public static final Type<ArtUpload> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"art_upload"));
 public static final StreamCodec<RegistryFriendlyByteBuf,ArtUpload> CODEC=new StreamCodec<>(){
  public ArtUpload decode(RegistryFriendlyByteBuf b){UUID transfer=b.readUUID();int action=b.readUnsignedByte();var hand=b.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;UUID token=b.readUUID();String title=b.readUtf(64);var size=PaintingSize.STREAM.decode(b);var options=PaintOptions.read(b);int index=b.readUnsignedByte(),count=b.readUnsignedByte();byte[] pixels=new byte[ArtBundle.PIXELS];b.readBytes(pixels);return new ArtUpload(transfer,action,hand,token,title,size,options,index,count,pixels);}
  public void encode(RegistryFriendlyByteBuf b,ArtUpload p){ArtBundle.validatePixels(p.pixels);b.writeUUID(p.transfer);b.writeByte(p.action);b.writeBoolean(p.hand==InteractionHand.OFF_HAND);b.writeUUID(p.token);b.writeUtf(p.title,64);PaintingSize.STREAM.encode(b,p.size);p.options.write(b);b.writeByte(p.index);b.writeByte(p.count);b.writeBytes(p.pixels);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public boolean sameHeader(ArtUpload p){return transfer.equals(p.transfer)&&action==p.action&&hand==p.hand&&token.equals(p.token)&&title.equals(p.title)&&size.equals(p.size)&&options.equals(p.options)&&count==p.count;}
 public static void handle(ArtUpload p,IPayloadContext c){StudioService.upload(p,c);}
}
