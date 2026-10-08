package studio.canvas;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server authorizes one plain item per one-use capability; no custom NBT is accepted. */
public record DrawItem(InteractionHand hand,String item,byte[] pixels,java.util.UUID token) implements CustomPacketPayload {
 public static final Type<DrawItem> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"draw_item"));
 public static final StreamCodec<RegistryFriendlyByteBuf,DrawItem> CODEC=new StreamCodec<>(){
  public DrawItem decode(RegistryFriendlyByteBuf b){var hand=b.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;var token=b.readUUID();String item=b.readUtf(256);byte[] pixels=new byte[SavePainting.PIXELS];b.readBytes(pixels);return new DrawItem(hand,item,pixels,token);}
  public void encode(RegistryFriendlyByteBuf b,DrawItem p){if(p.pixels.length!=SavePainting.PIXELS)throw new IllegalArgumentException("Invalid canvas size");b.writeBoolean(p.hand==InteractionHand.OFF_HAND);b.writeUUID(p.token);b.writeUtf(p.item,256);b.writeBytes(p.pixels);}
 };
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static boolean validDrawing(byte[] pixels){
  if(pixels==null||pixels.length!=SavePainting.PIXELS)return false;
  int ink=0;for(byte p:pixels){int color=Byte.toUnsignedInt(p);if(color<4||color>247)return false;if(color!=34)ink++;}
  return ink>=8;
 }
 public static boolean validItem(String item){if(item==null)return false;Identifier id=Identifier.tryParse(item);return id!=null && id.getNamespace().equals("minecraft") && BuiltInRegistries.ITEM.containsKey(id) && BuiltInRegistries.ITEM.getValue(id)!=Items.AIR;}
 public static void handle(DrawItem p,IPayloadContext context){
  if(!(context.player() instanceof ServerPlayer player))return;
  ItemStack held=player.getItemInHand(p.hand);
  CanvasTier tier=CanvasStudio.tier(held);
  var session=OpenCanvas.SESSIONS.get(player);
  if(!CanvasExchange.authorize(session,held,p.hand==InteractionHand.OFF_HAND,tier,held.getCount(),p.token,p.pixels,validItem(p.item),player.level().getGameTime())){
   context.reply(new CanvasReply(p.token,CanvasReply.NONE,3,"Request rejected. Reopen the held Molder or Infinity Canvas."));return;
  }
  // Preserve the existing vanilla-ID rules and local texture-based matching catalogue.
  ItemStack reward=CanvasExchange.createReward(p.item);
  Component rewardName=reward.getHoverName();
  boolean delivered=CanvasExchange.deliver(tier,reward,new CanvasExchange.RewardTarget(){
   public void replaceHeld(ItemStack item){player.setItemInHand(p.hand,item);}
   public boolean add(ItemStack item){return player.getInventory().add(item);}
   public boolean drop(ItemStack item){return player.drop(item,false,net.minecraft.util.Prediction.SERVER_ONLY)!=null;}
  });
  if(!delivered){context.reply(new CanvasReply(p.token,CanvasReply.NONE,3,"Reward could not be dropped. Canvas retained; reopen to retry."));return;}
  if(tier==CanvasTier.MOLDER)OpenCanvas.SESSIONS.remove(player);
  player.inventoryMenu.broadcastChanges();
  context.reply(new CanvasReply(p.token,session.token()==null?CanvasReply.NONE:session.token(),tier==CanvasTier.MOLDER?1:2,"Received 1 "+rewardName.getString()));
  player.sendSystemMessage(Component.literal("Canvas Studio: received ").append(rewardName),false);
 }
}
