import studio.canvas.client.ItemMatcher;
import studio.canvas.client.ItemMatchCatalog;
import studio.canvas.DrawItem;
import studio.canvas.SavePainting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import io.netty.buffer.Unpooled;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ItemMatchChecks {
 static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 static BufferedImage sprite(boolean sword){
  BufferedImage image=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();g.setColor(sword?Color.CYAN:Color.RED);
  if(sword){g.fillRect(28,8,8,34);g.setColor(Color.ORANGE);g.fillRect(18,42,28,5);g.fillRect(28,47,8,12);}else{g.fillOval(8,18,48,40);g.setColor(Color.GREEN);g.fillRect(31,8,4,14);}
  g.dispose();return image;
 }
 static BufferedImage sketch(BufferedImage source,int x,int y,int w,int h){BufferedImage out=new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);Graphics2D g=out.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,128,128);g.drawImage(source,x,y,w,h,null);g.dispose();return out;}
 public static void main(String[] args)throws Exception{
  BufferedImage sword=sprite(true),apple=sprite(false);
  var refs=List.of(new ItemMatcher.Reference("minecraft:diamond_sword",ItemMatcher.describe(sword,false)),new ItemMatcher.Reference("minecraft:apple",ItemMatcher.describe(apple,false)));
  for(int size:new int[]{32,64,96}){
   var result=ItemMatcher.match(sketch(sword,7,5,size,size),refs,3);check(result.getFirst().id().equals("minecraft:diamond_sword"),"Translated/scaled sword");
   result=ItemMatcher.match(sketch(apple,128-size,128-size,size,size),refs,3);check(result.getFirst().id().equals("minecraft:apple"),"Translated/scaled apple");
  }
  var outline=new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);Graphics2D pen=outline.createGraphics();pen.setColor(Color.WHITE);pen.fillRect(0,0,128,128);pen.setColor(Color.BLACK);pen.setStroke(new BasicStroke(2));
  pen.drawPolygon(new int[]{28,36,36,46,46,36,36,28,28,18,18,28},new int[]{8,8,42,42,47,47,59,59,47,47,42,42},12);pen.dispose();
  check(ItemMatcher.match(outline,refs,3).getFirst().id().equals("minecraft:diamond_sword"),"Closed black outline is recognized by shape");
  check(ItemMatcher.match(ItemMatcher.rotate(sketch(sword,0,0,128,128),Math.PI/4),refs,3).getFirst().id().equals("minecraft:diamond_sword"),"Rotated sword is recognized");
  var blank=sketch(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB),0,0,1,1);
  check(ItemMatcher.match(blank,refs,3).isEmpty(),"Blank image not matched");
  var duplicate=new ArrayList<>(refs);duplicate.add(refs.getFirst());
  check(ItemMatcher.match(sketch(sword,0,0,128,128),duplicate,10).size()==2,"One result per item ID");
  for(var m:ItemMatcher.match(sketch(sword,0,0,128,128),refs,3))check(Double.isFinite(m.similarity())&&m.similarity()>=0&&m.similarity()<=1,"Bounded similarity");
  BufferedImage texture=new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);Graphics2D g=texture.createGraphics();g.setColor(Color.ORANGE);g.fillRect(0,0,16,16);g.dispose();
  check(ItemMatcher.describe(ItemMatchCatalog.cube(texture),false)!=null,"Cube texture reference");
  byte[] pixels=new byte[SavePainting.PIXELS];Arrays.fill(pixels,(byte)34);check(!DrawItem.validDrawing(pixels),"Blank reward rejected");
  Arrays.fill(pixels,0,8,(byte)172);check(DrawItem.validDrawing(pixels),"Drawing accepted");
  pixels[99]=(byte)248;check(!DrawItem.validDrawing(pixels),"Invalid palette rejected");pixels[99]=34;
  check(!DrawItem.validDrawing(new byte[3]),"Incorrect size rejected");
  var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
  try{
   var p=new DrawItem(InteractionHand.OFF_HAND,"minecraft:diamond_sword",pixels);DrawItem.CODEC.encode(b,p);var d=DrawItem.CODEC.decode(b);
   check(d.hand()==p.hand()&&d.item().equals(p.item())&&Arrays.equals(d.pixels(),pixels)&&b.readableBytes()==0,"Reward packet roundtrip");
   b.clear();b.writeBoolean(false);b.writeUtf("minecraft:stone");b.writeByte(4);
   try{DrawItem.CODEC.decode(b);throw new AssertionError("Truncated payload accepted");}catch(IndexOutOfBoundsException expected){}
   try{DrawItem.CODEC.encode(b,new DrawItem(InteractionHand.MAIN_HAND,"minecraft:stone",new byte[1]));throw new AssertionError("Wrong length encoded");}catch(IllegalArgumentException expected){}
  }finally{b.release();}
  System.out.println("PASS matcher: shape/color, scale/translation, blank rejection, distinct/bounded results, cube reference, exchange drawing validation and packet codec");
 }
}
