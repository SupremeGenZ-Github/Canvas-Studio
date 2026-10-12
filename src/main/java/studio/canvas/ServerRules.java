package studio.canvas;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Only the authoritative server consults these settings for rewards. */
public final class ServerRules {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.IntValue MAX_INFINITY;
 public static final ModConfigSpec.BooleanValue MODDED;
 public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED;
 static {
  var b=new ModConfigSpec.Builder();
  MAX_INFINITY=b.comment("Maximum lifetime successful rewards per Infinity Canvas. 0 = unlimited. Counts never reset. Does not affect Molten.").defineInRange("maxInfinityCanvasUses",0,0,Integer.MAX_VALUE);
  MODDED=b.comment("Explicit opt-in for registered modded item rewards. Vanilla remains the default.").define("allowModdedRewards",false);
  BLOCKED=b.comment("Individual item IDs that cannot be rewarded. Does not disable canvas painting or Molten mode.").defineListAllowEmpty("blockedRewardItems",List.<String>of(),()->"minecraft:barrier",o->o instanceof String s&&Identifier.tryParse(s)!=null);
  SPEC=b.build();
 }
 public static boolean permits(String id){return !BLOCKED.get().contains(id)&&(MODDED.get()||id.startsWith("minecraft:"));}
 private ServerRules(){}
}
