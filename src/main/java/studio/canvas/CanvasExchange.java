package studio.canvas;
import java.util.UUID;

/** Actual authorization boundary used by the server handler and headless regression tests. */
public final class CanvasExchange {
 private CanvasExchange(){}
 public interface RewardTarget {
  void replaceHeld(net.minecraft.world.item.ItemStack reward);
  boolean add(net.minecraft.world.item.ItemStack reward);
  boolean drop(net.minecraft.world.item.ItemStack reward);
 }
 public static net.minecraft.world.item.ItemStack createReward(String item){
  return new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(item)),1);
 }
 public static boolean deliver(CanvasTier tier,net.minecraft.world.item.ItemStack reward,RewardTarget target){
  if(tier==CanvasTier.MOLTEN){target.replaceHeld(reward);return true;}
  if(tier==CanvasTier.INFINITY)return target.add(reward)||target.drop(reward);
  return false;
 }
 public static <T> boolean authorize(ExchangeSession<T> session,T held,boolean off,CanvasTier tier,int count,
                                    UUID token,byte[] pixels,boolean registeredVanillaItem,long now){
  return tier!=null&&tier.canExchange()&&count==1&&DrawItem.validDrawing(pixels)&&registeredVanillaItem&&session!=null
    &&session.claim(held,off,tier,token,now);
 }
}
