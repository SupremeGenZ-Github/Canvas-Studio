package studio.canvas;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
/** Persist access changes before replacing the held/wall item; keep the original on failure. */
public final class PaintingAccess {
 private PaintingAccess(){}
 @FunctionalInterface public interface Persist {void write(PaintingProperties properties)throws IOException;}
 public static ItemStack change(ItemStack current,UUID actor,boolean admin,boolean locked,Persist persist)throws IOException{
  var changed=current.getOrDefault(PaintingProperties.TYPE,PaintingProperties.DEFAULT).changeLock(actor,admin,locked);
  var replacement=current.copy();replacement.set(PaintingProperties.TYPE,changed);
  persist.write(changed);return replacement;
 }
}
