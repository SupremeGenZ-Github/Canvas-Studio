package studio.canvas;
import java.io.*;
import java.util.*;
/** Bounded common-side artwork; never contains local paths or executable data. */
public record ArtBundle(String title,PaintingSize size,PaintOptions options,List<byte[]> frames) {
 public static final int PIXELS=128*128,MAX_FRAMES=16,MAX_FILE=MAX_FRAMES*PIXELS+1024;
 public ArtBundle {
  if(title==null||title.length()>64||title.codePoints().anyMatch(c->Character.isISOControl(c)||c=='§')||size==null||options==null||frames==null||frames.isEmpty()||frames.size()>MAX_FRAMES)throw new IllegalArgumentException("Invalid artwork");
  List<byte[]> copy=new ArrayList<>();for(byte[] p:frames){validatePixels(p);copy.add(p.clone());}frames=List.copyOf(copy);
 }
 public static void validatePixels(byte[] p){if(p==null||p.length!=PIXELS)throw new IllegalArgumentException("Invalid pixel count");for(byte b:p){int c=Byte.toUnsignedInt(b);if(c>247)throw new IllegalArgumentException("Invalid map color");}}
 public byte[] encode()throws IOException {
  var bytes=new ByteArrayOutputStream();try(var out=new DataOutputStream(bytes)){out.writeInt(0x43534131);out.writeUTF(title);out.writeByte(size.width());out.writeByte(size.height());out.writeByte(options.frame());out.writeByte(options.fit());out.writeByte(options.delay());out.writeBoolean(options.locked());out.writeByte(frames.size());for(byte[] p:frames)out.write(p);}return bytes.toByteArray();
 }
 public static ArtBundle decode(byte[] bytes)throws IOException {
  if(bytes.length>MAX_FILE)throw new IOException("Artwork too large");try(var in=new DataInputStream(new ByteArrayInputStream(bytes))){if(in.readInt()!=0x43534131)throw new IOException("Invalid artwork file");String title=in.readUTF();var size=new PaintingSize(in.readUnsignedByte(),in.readUnsignedByte());var options=new PaintOptions(in.readUnsignedByte(),in.readUnsignedByte(),in.readUnsignedByte(),in.readBoolean());int n=in.readUnsignedByte();if(n<1||n>MAX_FRAMES)throw new IOException("Invalid frame count");List<byte[]> frames=new ArrayList<>();for(int i=0;i<n;i++){byte[] p=new byte[PIXELS];in.readFully(p);frames.add(p);}if(in.available()!=0)throw new IOException("Unexpected artwork data");return new ArtBundle(title,size,options,frames);}catch(IllegalArgumentException|EOFException e){throw new IOException("Invalid or truncated artwork",e);}
 }
}
