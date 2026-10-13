package studio.canvas;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
/** Capability tokens are bound to hand/stack or to a nearby wall entity and revision. */
public final class StudioSessions {
 public record Session(UUID token,InteractionHand hand,ItemStack held,WallPainting wall,int base,int revision,long expires){}
 private static final WeakHashMap<ServerPlayer,Session> SESSIONS=new WeakHashMap<>();
 private StudioSessions(){}
 public static boolean admin(ServerPlayer p){return p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);}
 public static PaintingProperties properties(ItemStack item){return item.getOrDefault(PaintingProperties.TYPE,PaintingProperties.DEFAULT);}
 public static int base(ItemStack item){var a=item.get(PaintingAnimation.TYPE);if(a!=null)return a.mapIds().getFirst();var id=item.get(net.minecraft.core.component.DataComponents.MAP_ID);return id==null?-1:id.id();}
 public static boolean nearby(ServerPlayer p,WallPainting wall){AABB b=wall.getBoundingBox();var v=p.getEyePosition();double x=Math.max(b.minX,Math.min(b.maxX,v.x)),y=Math.max(b.minY,Math.min(b.maxY,v.y)),z=Math.max(b.minZ,Math.min(b.maxZ,v.z));return v.distanceToSqr(x,y,z)<=64&&wall.level()==p.level()&&!wall.isRemoved();}
 public static boolean wallPermission(ServerPlayer p,WallPainting w){return p.mayBuild()&&nearby(p,w)&&p.level().mayInteract(p,w.getPos())&&properties(w.getItem()).mayEdit(p.getUUID(),admin(p));}
 public static Session begin(ServerPlayer p,InteractionHand hand,ItemStack item,WallPainting wall){var s=new Session(UUID.randomUUID(),hand,wall==null?item:null,wall,base(item),properties(item).revision(),p.level().getGameTime()+24000);SESSIONS.put(p,s);return s;}
 public static Session validate(ServerPlayer p,InteractionHand hand,UUID token,boolean wall){var s=SESSIONS.get(p);if(s==null||s.hand!=hand||!s.token.equals(token)||p.level().getGameTime()>s.expires||!p.mayBuild())return null;
  ItemStack current;if(wall){if(s.wall==null||!wallPermission(p,s.wall))return null;current=s.wall.getItem();}else{if(s.wall!=null||p.getItemInHand(hand)!=s.held||s.held.getCount()!=1||!s.held.is(Items.FILLED_MAP))return null;current=s.held;if(!properties(current).mayEdit(p.getUUID(),admin(p)))return null;}
  return base(current)==s.base&&properties(current).revision()==s.revision?s:null;
 }
 public static void close(ServerPlayer p){SESSIONS.remove(p);}
}
