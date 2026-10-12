package studio.canvas;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
/** Uses the installed game's actual enchanted-apple status effects; no hunger or item consumption. */
public final class KurloPillow {
 private record Sleep(ItemStack held,InteractionHand hand){}
 private static final WeakHashMap<ServerPlayer,Sleep> SLEEP=new WeakHashMap<>();
 private KurloPillow(){}
 public static java.util.List<ApplyStatusEffectsConsumeEffect> statusEffects(){return net.minecraft.world.item.component.Consumables.ENCHANTED_GOLDEN_APPLE.onConsumeEffects().stream().filter(effect->effect instanceof ApplyStatusEffectsConsumeEffect).map(effect->(ApplyStatusEffectsConsumeEffect)effect).toList();}
 public static boolean completed(boolean immediately,boolean updateLevel,int sleepTicks,boolean samePillow){return !immediately&&!updateLevel&&sleepTicks>=100&&samePillow;}
 public static void tick(PlayerTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer player))return;
  if(!player.isSleeping()){SLEEP.remove(player);return;}
  if(player.getSleepTimer()>2||SLEEP.containsKey(player))return;
  for(InteractionHand hand:InteractionHand.values()){var held=player.getItemInHand(hand);if(held.is(CanvasStudio.KURLO_PILLOW.get())){SLEEP.put(player,new Sleep(held,hand));break;}}
 }
 public static void wake(PlayerWakeUpEvent event){
  if(!(event.getEntity() instanceof ServerPlayer player))return;
  var sleep=SLEEP.remove(player);boolean same=sleep!=null&&player.getItemInHand(sleep.hand)==sleep.held&&sleep.held.is(CanvasStudio.KURLO_PILLOW.get());
  if(!completed(event.wakeImmediately(),event.updateLevel(),player.getSleepTimer(),same))return;
  var apple=new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
  for(var status:statusEffects())status.apply(player.level(),apple,player);
  player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Kurlo Pillow: enchanted golden apple effects granted."),false);
 }
 public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer player)SLEEP.remove(player);}
}
