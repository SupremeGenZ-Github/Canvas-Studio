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
 public static void handle(OpenPainting p,IPayloadContext context){
  if(!(context.player() instanceof ServerPlayer player))return;var held=player.getItemInHand(p.hand);
  if(!held.is(Items.FILLED_MAP)||held.getCount()!=1)return;var data=MapItem.getSavedData(held,player.level());if(data==null||!data.locked){player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Hold one saved, locked painting map to edit."),false);return;}
  UUID token=UUID.randomUUID();SESSIONS.put(player,new EditSession(held,p.hand,token,player.level().getGameTime()+24000));String title=held.getHoverName().getString();if(title.length()>64)title=title.substring(0,64);
  var size=held.getOrDefault(PaintingSize.TYPE,PaintingSize.ONE);context.reply(new PaintingData(p.hand,p.request,token,title,data.colors.clone(),size.width(),size.height()));
 }
 public static boolean claim(ServerPlayer player,InteractionHand hand,ItemStack held,UUID token){var s=SESSIONS.get(player);if(s==null||s.held!=held||s.hand!=hand||!s.token.equals(token)||player.level().getGameTime()>s.expires||!held.is(Items.FILLED_MAP)||held.getCount()!=1)return false;SESSIONS.remove(player);return true;}
}
