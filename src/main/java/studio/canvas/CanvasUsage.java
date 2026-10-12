package studio.canvas;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
/** Persistent, immutable lifetime counter. Zero is the legacy-item default. */
public final class CanvasUsage {
 public static final DataComponentType<Long> TYPE=DataComponentType.<Long>builder().persistent(Codec.longRange(0,Long.MAX_VALUE)).networkSynchronized(ByteBufCodecs.VAR_LONG).build();
 public static long count(ItemStack stack){return stack.getOrDefault(TYPE,0L);}
 public static boolean allowed(CanvasTier tier,long used,int maximum){return tier==CanvasTier.MOLTEN||tier==CanvasTier.INFINITY&&(maximum==0||used<maximum);}
 public static long afterDelivery(long used,boolean delivered){return delivered&&used<Long.MAX_VALUE?used+1:used;}
 public static void delivered(ItemStack stack){stack.set(TYPE,afterDelivery(count(stack),true));}
 private CanvasUsage(){}
}
