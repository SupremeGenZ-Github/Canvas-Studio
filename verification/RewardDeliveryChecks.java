import studio.canvas.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.*;
import net.minecraft.world.item.*;
import java.util.*;

/** Exercises production reward creation/delivery with a headless inventory adapter. */
public class RewardDeliveryChecks {
 static void check(boolean v,String why){if(!v)throw new AssertionError(why);}
 static class Inventory implements CanvasExchange.RewardTarget {
  final ItemStack[] hands={new ItemStack(Items.PAPER),new ItemStack(Items.PAPER)};
  final List<ItemStack> items=new ArrayList<>(),drops=new ArrayList<>();int hand;boolean full,rejectDrop;
  public void replaceHeld(ItemStack reward){hands[hand]=reward;}
  public boolean add(ItemStack reward){if(full)return false;items.add(reward.copy());reward.setCount(0);return true;}
  public boolean drop(ItemStack reward){if(rejectDrop)return false;drops.add(reward.copy());return true;}
 }
 public static void main(String[] args){
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  var defaults=DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build();
  BuiltInRegistries.ITEM.listElements().forEach(h->h.bindComponents(defaults));
  for(int hand=0;hand<2;hand++){
   Inventory inventory=new Inventory();inventory.hand=hand;ItemStack canvas=inventory.hands[hand],other=inventory.hands[1-hand];
   check(inventory.items.isEmpty(),"Player initially owns no diamond");
   var first=CanvasExchange.createReward("minecraft:diamond");var second=CanvasExchange.createReward("minecraft:diamond");
   check(first!=second&&first.is(Items.DIAMOND)&&first.getCount()==1&&first.getComponentsPatch().isEmpty(),"New plain reward of count one");
   check(!CanvasExchange.deliver(CanvasTier.BLANK,first,inventory)&&inventory.hands[hand]==canvas&&inventory.items.isEmpty(),"Blank cannot receive reward");
   check(CanvasExchange.deliver(CanvasTier.MOLTEN,first,inventory)&&inventory.hands[hand]==first&&inventory.hands[1-hand]==other,"Molten exchanges only held hand");
   inventory.hands[hand]=canvas;
   for(int i=0;i<20;i++)check(CanvasExchange.deliver(CanvasTier.INFINITY,CanvasExchange.createReward("minecraft:stone"),inventory),"Infinity delivery succeeds");
   check(inventory.hands[hand]==canvas&&inventory.items.size()==20&&inventory.items.stream().allMatch(s->s.is(Items.STONE)&&s.getCount()==1),"Infinity retained, one block per delivery");
   inventory.full=true;
   check(CanvasExchange.deliver(CanvasTier.INFINITY,second,inventory)&&inventory.drops.size()==1&&inventory.drops.getFirst().getCount()==1&&inventory.hands[hand]==canvas,"Full inventory drops one reward and retains canvas");
   inventory.rejectDrop=true;
   check(!CanvasExchange.deliver(CanvasTier.INFINITY,CanvasExchange.createReward("minecraft:diamond"),inventory)&&inventory.hands[hand]==canvas&&inventory.drops.size()==1,"Rejected drop retains canvas and reports failure");
  }
  System.out.println("PASS production reward delivery with headless inventory: both hands, fresh plain items without ownership, Molten replacement, Infinity reuse, full inventory drop, rejected drop retains canvas.");
 }
}
