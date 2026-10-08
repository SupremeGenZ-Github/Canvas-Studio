package studio.canvas;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 0 = session ready; 1 = Molder success; 2 = Infinity success; 3 = rejected. */
public record CanvasReply(UUID request,UUID token,int code,String message) implements CustomPacketPayload {
 public static final UUID NONE=new UUID(0,0);
 public static final Type<CanvasReply> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"canvas_reply"));
 public static final StreamCodec<RegistryFriendlyByteBuf,CanvasReply> CODEC=new StreamCodec<>(){
  public CanvasReply decode(RegistryFriendlyByteBuf b){return new CanvasReply(b.readUUID(),b.readUUID(),b.readUnsignedByte(),b.readUtf(256));}
  public void encode(RegistryFriendlyByteBuf b,CanvasReply p){b.writeUUID(p.request);b.writeUUID(p.token);b.writeByte(p.code);b.writeUtf(p.message,256);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(CanvasReply p,IPayloadContext context){studio.canvas.client.CanvasClient.reply(p);}
}
