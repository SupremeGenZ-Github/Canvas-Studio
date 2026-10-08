import studio.canvas.SavePainting;
import studio.canvas.OpenCanvas;
import studio.canvas.CanvasReply;
import java.util.UUID;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.material.MapColor;
import io.netty.buffer.Unpooled;
import java.util.Arrays;

public class CanvasChecks {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception {
  ImageImportTest.main(args);
  byte[] pixels=new byte[SavePainting.PIXELS];
  for(int i=0;i<pixels.length;i++)pixels[i]=(byte)(4+i%244);
  var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
  try {
   UUID request=UUID.randomUUID(), token=UUID.randomUUID();
   for(InteractionHand hand:InteractionHand.values()){
    OpenCanvas.CODEC.encode(b,new OpenCanvas(hand,request));var open=OpenCanvas.CODEC.decode(b);
    check(open.hand()==hand&&open.request().equals(request)&&b.readableBytes()==0,"Session request roundtrip");
   }
   for(int code=0;code<4;code++){
    var reply=new CanvasReply(request,token,code,"Server response");CanvasReply.CODEC.encode(b,reply);
    check(reply.equals(CanvasReply.CODEC.decode(b))&&b.readableBytes()==0,"Session reply roundtrip");
   }
   var p=new SavePainting(InteractionHand.OFF_HAND,"My Painting",pixels);
   SavePainting.CODEC.encode(b,p);var decoded=SavePainting.CODEC.decode(b);
   check(decoded.hand()==p.hand()&&decoded.title().equals(p.title())&&Arrays.equals(decoded.pixels(),pixels),"Packet roundtrip");
   check(b.readableBytes()==0,"Packet consumed exactly");
   b.clear();b.writeBoolean(false);b.writeUtf("Truncated");b.writeByte(0);
   try{SavePainting.CODEC.decode(b);throw new AssertionError("Accepted truncated packet");}catch(IndexOutOfBoundsException expected){}
   b.clear();
   try{SavePainting.CODEC.encode(b,new SavePainting(InteractionHand.MAIN_HAND,"bad",new byte[2]));throw new AssertionError("Accepted wrong pixel size");}catch(IllegalArgumentException expected){}
  }finally{b.release();}
  for(int i=4;i<248;i++)check((MapColor.getColorFromPackedId(i)>>>24)==255,"Palette alpha");
  int red=MapColor.COLOR_RED.calculateARGBColor(MapColor.Brightness.NORMAL);
  check(((red>>16)&255)>(red&255),"ARGB red channel order");
  System.out.println("PASS: payload roundtrip, truncated/wrong-length payload rejection, actual Minecraft palette ARGB.");
 }
}
