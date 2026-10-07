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

/** One canvas is atomically exchanged for one plain item; no custom NBT is accepted. */
public record DrawItem(InteractionHand hand,String item,byte[] pixels) implements CustomPacketPayload {
 public static final Type<DrawItem> TYPE=new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID,"draw_item"));
 public static final StreamCodec<RegistryFriendlyByteBuf,DrawItem> CODEC=new StreamCodec<>(){
  public DrawItem decode(RegistryFriendlyByteBuf b){var hand=b.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;String item=b.readUtf(256);byte[] pixels=new byte[SavePainting.PIXELS];b.readBytes(pixels);return new DrawItem(hand,item,pixels);}
  public void encode(RegistryFriendlyByteBuf b,DrawItem p){if(p.pixels.length!=SavePainting.PIXELS)throw new IllegalArgumentException("Invalid canvas size");b.writeBoolean(p.hand==InteractionHand.OFF_HAND);b.writeUtf(p.item,256);b.writeBytes(p.pixels);}
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
  if(!held.is(CanvasStudio.CANVAS.get())||held.getCount()!=1||!validDrawing(p.pixels)||!validItem(p.item))return;
  // Matching is local visual similarity against client resource-pack textures.
  // Receiving vanilla items is the intentionally enabled gameplay feature.
  ItemStack reward=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(p.item)),1);
  player.setItemInHand(p.hand,reward);player.inventoryMenu.broadcastChanges();
  player.sendSystemMessage(Component.literal("Canvas Studio: received ").append(reward.getHoverName()),false);
 }
}
