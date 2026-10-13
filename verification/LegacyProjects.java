import studio.canvas.client.art.*;
import java.io.*;
/** Fixed pre-animation wire fixtures, independent of the current project encoder. */
public final class LegacyProjects {
 public static byte[] encode(Artwork d,int version)throws IOException{
  var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes);out.writeInt(ProjectCodec.MAGIC);out.writeInt(version);out.writeInt(128);out.writeInt(128);out.writeUTF(d.title);
  if(version==1)out.write(d.flatten());else{
   out.writeInt(d.active);out.writeInt(d.brush);out.writeInt(d.snap);out.writeBoolean(d.grid);out.writeBoolean(d.mirrorX);out.writeBoolean(d.mirrorY);out.writeUTF(d.tool);out.writeByte(d.selected);out.writeInt(d.layers.size());for(var l:d.layers){out.writeUTF(l.name);out.writeBoolean(l.visible);out.write(l.pixels);}out.writeBoolean(d.original!=null);if(d.original!=null)for(int c:d.original)out.writeInt(c);
   if(version>=3){out.writeByte(d.wallWidth);out.writeByte(d.wallHeight);}
  }return bytes.toByteArray();
 }
}
