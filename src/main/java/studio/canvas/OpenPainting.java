package studio.canvas;
import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.network.handling.IPayloadContext;
/** Editing a held locked map creates a new map; other copies retain their original art. */
public record OpenPainting(InteractionHand hand,UUID request) implements CustomPacketPayload {
 private record EditSession(ItemStack held,InteractionHand hand,UUID token,long expires){}
 private static final WeakHashMap<ServerPlayer,EditSession> SESSIONS=new WeakHashMap<>();
 public static final Type<OpenPainting> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"open_painting"));
 public static final StreamCodec<RegistryFriendlyByteBuf,OpenPainting> CODEC=new StreamCodec<>(){
  public OpenPainting decode(RegistryFriendlyByteBuf b){return new OpenPainting(b.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,b.readUUID());}
  public void encode(RegistryFriendlyByteBuf b,OpenPainting p){b.writeBoolean(p.hand==InteractionHand.OFF_HAND);b.writeUUID(p.request);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(OpenPainting p,IPayloadContext context){if(context.player() instanceof ServerPlayer player)StudioService.openHeld(player,p.hand,p.request);}
 public static boolean claim(ServerPlayer p,InteractionHand hand,ItemStack held,UUID token){var s=StudioSessions.validate(p,hand,token,false);if(s==null)return false;StudioSessions.close(p);return true;}
}
