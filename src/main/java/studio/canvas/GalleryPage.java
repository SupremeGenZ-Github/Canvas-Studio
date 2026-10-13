package studio.canvas;
import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record GalleryPage(UUID request,int page,int total,List<StudioStore.Entry> entries,String message) implements CustomPacketPayload {
 public static final int PAGE_SIZE=6;
 public static final Type<GalleryPage> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"gallery_page"));
 public static final StreamCodec<RegistryFriendlyByteBuf,GalleryPage> CODEC=new StreamCodec<>(){
  public GalleryPage decode(RegistryFriendlyByteBuf b){UUID request=b.readUUID();int page=b.readVarInt(),total=b.readVarInt(),n=b.readUnsignedByte();if(page<0||total<0||total>1000||n>PAGE_SIZE)throw new IllegalArgumentException("Gallery page");List<StudioStore.Entry> rows=new ArrayList<>();for(int i=0;i<n;i++)rows.add(new StudioStore.Entry(b.readUUID(),b.readUUID(),b.readUtf(64),b.readUtf(64),b.readUnsignedByte(),b.readUnsignedByte(),b.readUnsignedByte(),b.readLong()));return new GalleryPage(request,page,total,List.copyOf(rows),b.readUtf(256));}
  public void encode(RegistryFriendlyByteBuf b,GalleryPage p){if(p.entries.size()>PAGE_SIZE)throw new IllegalArgumentException("Gallery page");b.writeUUID(p.request);b.writeVarInt(p.page);b.writeVarInt(p.total);b.writeByte(p.entries.size());for(var e:p.entries){b.writeUUID(e.id());b.writeUUID(e.owner());b.writeUtf(e.author(),64);b.writeUtf(e.title(),64);b.writeByte(e.width());b.writeByte(e.height());b.writeByte(e.frames());b.writeLong(e.created());}b.writeUtf(p.message,256);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(GalleryPage p,IPayloadContext c){studio.canvas.client.ServerGalleryScreen.receive(p);}
}
