package studio.canvas;

import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCanvas(InteractionHand hand,UUID request) implements CustomPacketPayload {
 static final WeakHashMap<ServerPlayer,ExchangeSession<ItemStack>> SESSIONS=new WeakHashMap<>();
 public static final Type<OpenCanvas> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"open_canvas"));
 public static final StreamCodec<RegistryFriendlyByteBuf,OpenCanvas> CODEC=new StreamCodec<>(){
  public OpenCanvas decode(RegistryFriendlyByteBuf b){return new OpenCanvas(b.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,b.readUUID());}
  public void encode(RegistryFriendlyByteBuf b,OpenCanvas p){b.writeBoolean(p.hand==InteractionHand.OFF_HAND);b.writeUUID(p.request);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void handle(OpenCanvas p,IPayloadContext context){
  if(!(context.player() instanceof ServerPlayer player))return;
  ItemStack held=player.getItemInHand(p.hand);CanvasTier tier=CanvasStudio.tier(held);
  if(tier==null||!tier.canExchange()||held.getCount()!=1){context.reply(new CanvasReply(p.request,CanvasReply.NONE,3,"Molten or Infinity Canvas required."));return;}
  long now=player.level().getGameTime();boolean off=p.hand==InteractionHand.OFF_HAND;
  var session=SESSIONS.get(player);
  if(session==null||!session.matches(held,off,tier,now)||session.token()==null){session=new ExchangeSession<>(held,off,tier,now);SESSIONS.put(player,session);}
  context.reply(new CanvasReply(p.request,session.token(),0,"Get Item ready."));
 }
}
