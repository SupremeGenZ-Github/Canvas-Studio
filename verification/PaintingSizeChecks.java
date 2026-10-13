import studio.canvas.*;
import studio.canvas.client.art.*;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import java.util.*;
public class PaintingSizeChecks {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 static void near(double a,double b,String why){check(Math.abs(a-b)<1e-8,why);}
 public static void main(String[] args)throws Exception{
  BlockPos anchor=new BlockPos(-17,72,31);
  for(int w=1;w<=8;w++)for(int h=1;h<=8;h++){
   var size=new PaintingSize(w,h);
   var json=PaintingSize.CODEC.encodeStart(JsonOps.INSTANCE,size).getOrThrow();
   check(size.equals(PaintingSize.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow()),"Persistent codec");
   var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
   try{PaintingSize.STREAM.encode(buffer,size);check(size.equals(PaintingSize.STREAM.decode(buffer))&&buffer.readableBytes()==0,"Wire codec");}finally{buffer.release();}
   for(Direction face:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
    var cells=size.cells(anchor,face);check(cells.size()==w*h&&new HashSet<>(cells).size()==w*h,"Unique occupied cells");
    check(cells.getFirst().equals(anchor),"Lower-left anchor");
    check(cells.getLast().equals(anchor.relative(face.getCounterClockWise(),w-1).above(h-1)),"Top-right endpoint");
    var box=size.bounds(anchor,face);near(box.getYsize(),h,"Actual hit-box height");
    near(face.getAxis()==Direction.Axis.X?box.getZsize():box.getXsize(),w,"Actual hit-box width");
    near(face.getAxis()==Direction.Axis.X?box.getXsize():box.getZsize(),1.0/16,"No enlarged depth");
    for(BlockPos cell:cells){
     var center=cell.getCenter().add(-face.getStepX()*.46875,0,-face.getStepZ()*.46875);
     check(box.contains(center),"Every occupied tile lies in hit box");
     check(!box.contains(center.add(face.getStepX(),0,face.getStepZ())),"Box stays on wall face");
    }
   }
   var art=new Artwork();art.wallWidth=w;art.wallHeight=h;
   byte[] bytes=ProjectCodec.encode(art);var restored=ProjectCodec.decode(bytes);
   check(restored.wallWidth==w&&restored.wallHeight==h,"Project dimensions");
   check(art.copy().wallWidth==w&&art.copy().wallHeight==h,"Copied dimensions");
   byte[] v2=Arrays.copyOf(bytes,bytes.length-2);v2[7]=2;var old=ProjectCodec.decode(v2);
   check(old.wallWidth==1&&old.wallHeight==1,"Version-2 migration");
  }
  for(int bad:new int[]{-1,0,9,255}){
   check(!PaintingSize.valid(bad,1)&&!PaintingSize.valid(1,bad),"Invalid dimensions");
   try{new PaintingSize(bad,1);throw new AssertionError("Accepted bad width");}catch(IllegalArgumentException expected){}
   check(PaintingSize.fromSaved(bad,8).equals(PaintingSize.ONE),"Invalid saved data safely defaults");
   check(PaintingSize.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{\"width\":"+bad+",\"height\":1}")).error().isPresent(),"Invalid persistent codec");
  }
  var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
  try{
   b.writeByte(9);b.writeByte(1);try{PaintingSize.STREAM.decode(b);throw new AssertionError("Accepted forged size");}catch(IllegalArgumentException expected){}
   b.clear();b.writeByte(8);try{PaintingSize.STREAM.decode(b);throw new AssertionError("Accepted truncated size");}catch(IndexOutOfBoundsException expected){}
   b.clear();byte[] pixels=Artwork.white();UUID token=UUID.randomUUID();
   var save=new SavePainting(net.minecraft.world.InteractionHand.OFF_HAND,"Large",pixels,token,8,5);
   SavePainting.CODEC.encode(b,save);var copied=SavePainting.CODEC.decode(b);check(copied.width()==8&&copied.height()==5&&copied.editToken().equals(token),"Save packet keeps size");
   var data=new PaintingData(net.minecraft.world.InteractionHand.MAIN_HAND,UUID.randomUUID(),token,"Large",pixels,3,8);
   PaintingData.CODEC.encode(b,data);var read=PaintingData.CODEC.decode(b);check(read.width()==3&&read.height()==8&&Arrays.equals(read.pixels(),pixels),"Editing packet keeps size");
  }finally{b.release();}
  var invalid=new Artwork();invalid.wallWidth=9;try{ProjectCodec.encode(invalid);throw new AssertionError("Accepted oversized project");}catch(java.io.IOException expected){}
  byte[] corrupt=ProjectCodec.encode(new Artwork());corrupt[corrupt.length-1]=0;try{ProjectCodec.decode(corrupt);throw new AssertionError("Accepted forged project size");}catch(java.io.IOException expected){}
  System.out.println("PASS all 64 wall sizes/four faces: exact cells, physical bounds, persistent/wire codecs, invalid/truncated dimensions, project v2 migration and save/edit packet dimensions.");
 }
}
