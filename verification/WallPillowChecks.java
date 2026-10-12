import studio.canvas.*;
import net.minecraft.core.Direction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
public class WallPillowChecks {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception {
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  for(Direction direction:Direction.values()){
   check(WallPlacement.eligible(direction,true,true,true)==direction.getAxis().isHorizontal(),"Horizontal supported placement");
   check(!WallPlacement.eligible(direction,false,true,true),"Unlocked map rejected");
   check(!WallPlacement.eligible(direction,true,false,true),"Permission rejection");
   check(!WallPlacement.eligible(direction,true,true,false),"Obstructed placement rejection");
  }
  check(KurloPillow.completed(false,false,100,true),"Completed sleep grants effects");
  check(!KurloPillow.completed(true,true,100,true),"Leaving bed early rejected");
  check(!KurloPillow.completed(false,true,100,true),"Interrupted sleep rejected");
  check(!KurloPillow.completed(false,false,99,true),"Incomplete sleep rejected");
  check(!KurloPillow.completed(false,false,100,false),"Missing/swapped pillow rejected");
  var effects=KurloPillow.statusEffects();check(!effects.isEmpty(),"Vanilla enchanted apple effects available");
  var ids=new java.util.HashSet<String>();for(var group:effects){check(group.probability()==1,"Guaranteed vanilla status effects");for(var effect:group.effects())ids.add(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString());}
  check(ids.equals(java.util.Set.of("minecraft:regeneration","minecraft:absorption","minecraft:resistance","minecraft:fire_resistance")),"Exact enchanted-apple status effect catalogue");
  var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
  var painting=(WallPainting)unsafe.allocateInstance(WallPainting.class);var method=WallPainting.class.getDeclaredMethod("getFrameItemStack");method.setAccessible(true);
  check(((net.minecraft.world.item.ItemStack)method.invoke(painting)).isEmpty(),"Wall paintings have no frame item drop");
  System.out.println("PASS wall-placement eligibility, no-frame item, completed/early/interrupted sleep rules and actual vanilla enchanted-apple effects");
 }
}
