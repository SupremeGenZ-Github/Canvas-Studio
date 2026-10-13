package studio.canvas.client.art;
import java.io.*;
/** Bounded, uncompressed CSP binary format. It contains no paths or executable data. */
public final class ProjectCodec {
 public static final int MAX_BYTES=8*1024*1024,MAGIC=0x43535031,VERSION=4;
 private ProjectCodec(){}
 public static byte[] encode(Artwork d)throws IOException{
  var bytes=new ByteArrayOutputStream();try(var out=new DataOutputStream(bytes)){
   out.writeInt(MAGIC);out.writeInt(VERSION);out.writeInt(128);out.writeInt(128);out.writeUTF(text(d.title));
   out.writeInt(d.active);out.writeInt(d.brush);out.writeInt(d.snap);out.writeBoolean(d.grid);out.writeBoolean(d.mirrorX);out.writeBoolean(d.mirrorY);out.writeUTF(d.tool);out.writeByte(d.selected);
   out.writeInt(d.layers.size());for(var l:d.layers){out.writeUTF(text(l.name));out.writeBoolean(l.visible);checkPixels(l.pixels);out.write(l.pixels);}
   out.writeBoolean(d.original!=null);if(d.original!=null){if(d.original.length!=Artwork.PIXELS)throw new IOException("Invalid source image");for(int c:d.original)out.writeInt(c);}
   d.syncFrame();
   if(!studio.canvas.PaintingSize.valid(d.wallWidth,d.wallHeight))throw new IOException("Invalid wall size");out.writeByte(d.wallWidth);out.writeByte(d.wallHeight);
   var settings=new studio.canvas.PaintOptions(d.frameStyle,d.fitMode,d.frameDelay,d.locked);out.writeByte(settings.frame());out.writeByte(settings.fit());out.writeByte(settings.delay());out.writeBoolean(settings.locked());out.writeByte(d.timeline.size());out.writeByte(d.animationIndex);
   if(d.timeline.isEmpty()||d.timeline.size()>Artwork.MAX_FRAMES)throw new IOException("Invalid animation count");
   for(var frame:d.timeline){out.writeByte(frame.layers.size());out.writeByte(frame.active);for(var layer:frame.layers){out.writeUTF(text(layer.name));out.writeBoolean(layer.visible);checkPixels(layer.pixels);out.write(layer.pixels);}out.writeBoolean(frame.original!=null);if(frame.original!=null){if(frame.original.length!=Artwork.PIXELS)throw new IOException("Invalid source image");for(int c:frame.original)out.writeInt(c);}}
  }
  byte[] encoded=bytes.toByteArray();decode(encoded);return encoded;
 }
 public static Artwork decode(byte[] bytes)throws IOException{
  if(bytes.length>MAX_BYTES)throw new IOException("Project exceeds 8 MiB");
  try(var in=new DataInputStream(new ByteArrayInputStream(bytes))){
   if(in.readInt()!=MAGIC)throw new IOException("Not a Canvas Studio project");int version=in.readInt();
   if(version<1||version>VERSION)throw new IOException("Unsupported project version: "+version);
   if(in.readInt()!=128||in.readInt()!=128)throw new IOException("Only 128 x 128 projects are supported");
   var d=new Artwork();d.title=text(in.readUTF());
   if(version==1){byte[] pixels=new byte[Artwork.PIXELS];in.readFully(pixels);checkPixels(pixels);d.layers.clear();d.layers.add(new Artwork.Layer("Background",pixels));}
   else{
    d.active=in.readInt();d.brush=in.readInt();d.snap=in.readInt();d.grid=in.readBoolean();d.mirrorX=in.readBoolean();d.mirrorY=in.readBoolean();d.tool=in.readUTF();d.selected=in.readByte();
    try{PixelTools.Tool.valueOf(d.tool);}catch(IllegalArgumentException e){throw new IOException("Unknown drawing tool");}
    if(d.brush<1||d.brush>16||d.snap<1||d.snap>32||Byte.toUnsignedInt(d.selected)<4||Byte.toUnsignedInt(d.selected)>247)throw new IOException("Invalid editor settings");
    int n=in.readInt();if(n<1||n>Artwork.MAX_LAYERS||d.active<0||d.active>=n)throw new IOException("Invalid layer count or selection");
    d.layers.clear();for(int i=0;i<n;i++){String name=text(in.readUTF());boolean visible=in.readBoolean();byte[] pixels=new byte[Artwork.PIXELS];in.readFully(pixels);checkPixels(pixels);var l=new Artwork.Layer(name,pixels);l.visible=visible;d.layers.add(l);}
    if(in.readBoolean()){d.original=new int[Artwork.PIXELS];for(int i=0;i<d.original.length;i++)d.original[i]=in.readInt();}
   }
   if(version>=3){d.wallWidth=in.readUnsignedByte();d.wallHeight=in.readUnsignedByte();if(!studio.canvas.PaintingSize.valid(d.wallWidth,d.wallHeight))throw new IOException("Invalid wall size");}
   if(version>=4){
    try{var settings=new studio.canvas.PaintOptions(in.readUnsignedByte(),in.readUnsignedByte(),in.readUnsignedByte(),in.readBoolean());d.frameStyle=settings.frame();d.fitMode=settings.fit();d.frameDelay=settings.delay();d.locked=settings.locked();}catch(IllegalArgumentException e){throw new IOException("Invalid display settings",e);}
    int count=in.readUnsignedByte();d.animationIndex=in.readUnsignedByte();if(count<1||count>Artwork.MAX_FRAMES||d.animationIndex>=count)throw new IOException("Invalid animation selection");
    for(int f=0;f<count;f++){var frame=new Artwork.Frame();int layers=in.readUnsignedByte();frame.active=in.readUnsignedByte();if(layers<1||layers>Artwork.MAX_LAYERS||frame.active>=layers)throw new IOException("Invalid frame layers");for(int l=0;l<layers;l++){String name=text(in.readUTF());boolean visible=in.readBoolean();byte[] pixels=new byte[Artwork.PIXELS];in.readFully(pixels);checkPixels(pixels);var layer=new Artwork.Layer(name,pixels);layer.visible=visible;frame.layers.add(layer);}if(in.readBoolean()){frame.original=new int[Artwork.PIXELS];for(int i=0;i<frame.original.length;i++)frame.original[i]=in.readInt();}d.timeline.add(frame);}d.loadFrame();
   }
   if(in.available()!=0)throw new IOException("Unexpected project data");return d;
  }catch(EOFException e){throw new IOException("Incomplete project file",e);}
 }
 private static String text(String text)throws IOException{if(text==null||text.length()>64||text.codePoints().anyMatch(c->Character.isISOControl(c)||c=='§'))throw new IOException("Invalid project or layer name (maximum 64 characters)");return text;}
 private static void checkPixels(byte[] pixels)throws IOException{if(pixels.length!=Artwork.PIXELS)throw new IOException("Invalid pixel count");for(byte p:pixels){int c=Byte.toUnsignedInt(p);if(c!=0&&(c<4||c>247))throw new IOException("Invalid map palette index");}}
}
