package studio.canvas;

import java.io.IOException;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;

/** Stable frame IDs: append new materials so existing paintings keep their appearance. */
public final class FrameMaterials {
 public static final String[] NAMES={"None","Wood","Gold","Iron","Diamond","Stone","Copper","Amethyst","Lapis","Emerald","Quartz","Netherite"};
 public static final int[] ORDER={0,1,5,6,3,2,7,8,4,9,10,11};
 private static final int[] COLORS={0,0xff976239,0xffe8bc35,0xffbbc3ce,0xff3acac4,0xff858585,0xffc9794b,0xffa47bd6,0xff345cbb,0xff35ba6f,0xffe8e0d2,0xff51494b};
 private FrameMaterials(){}
 public static boolean valid(int frame){return frame>=0&&frame<NAMES.length;}
 private static void require(int frame){if(!valid(frame))throw new IllegalArgumentException("Unknown painting frame");}
 public static int color(int frame){require(frame);return COLORS[frame];}
 public static int next(int frame){require(frame);for(int i=0;i<ORDER.length;i++)if(ORDER[i]==frame)return ORDER[(i+1)%ORDER.length];throw new IllegalStateException();}
 public static String material(int frame){require(frame);return switch(frame){
  case 0->"nothing";case 1->"any wooden plank";case 2->"Gold Ingot";case 3->"Iron Ingot";case 4->"Diamond";
  case 5->"Stone";case 6->"Copper Ingot";case 7->"Amethyst Shard";case 8->"Lapis Lazuli";case 9->"Emerald";case 10->"Nether Quartz";case 11->"Netherite Ingot";default->throw new IllegalStateException();};}
 public static boolean matches(int frame,ItemStack stack){require(frame);if(stack.isEmpty())return false;return switch(frame){
  case 1->stack.is(ItemTags.PLANKS);case 2->stack.is(Items.GOLD_INGOT);case 3->stack.is(Items.IRON_INGOT);case 4->stack.is(Items.DIAMOND);
  case 5->stack.is(Items.STONE);case 6->stack.is(Items.COPPER_INGOT);case 7->stack.is(Items.AMETHYST_SHARD);case 8->stack.is(Items.LAPIS_LAZULI);
  case 9->stack.is(Items.EMERALD);case 10->stack.is(Items.QUARTZ);case 11->stack.is(Items.NETHERITE_INGOT);default->false;};}
 public static boolean costsMaterial(int existing,int requested){require(existing);require(requested);return requested!=0&&requested!=existing;}
 public static int slot(Container inventory,int frame){require(frame);for(int i=0;i<inventory.getContainerSize();i++)if(matches(frame,inventory.getItem(i)))return i;return -1;}
 public static boolean has(Container inventory,ItemStack offHand,int frame){return slot(inventory,frame)>=0||matches(frame,offHand);}
 @FunctionalInterface public interface SaveAction {void run()throws IOException;}
 /** Called on the main server thread; a rejected or failed save cannot spend material. */
 public static void save(int existing,int requested,Container inventory,SaveAction action)throws IOException{
  save(existing,requested,inventory,ItemStack.EMPTY,action);
 }
 public static void save(int existing,int requested,Container inventory,ItemStack offHand,SaveAction action)throws IOException{
  if(!costsMaterial(existing,requested)){action.run();return;}
  int slot=slot(inventory,requested);if(slot<0&&!matches(requested,offHand))throw new IOException("A "+NAMES[requested]+" frame requires 1 "+material(requested)+" in your inventory.");
  ItemStack payment=slot<0?offHand:inventory.getItem(slot);
  action.run();
  payment.shrink(1);inventory.setChanged();
 }
}
