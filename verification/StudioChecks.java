import studio.canvas.client.art.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
public class StudioChecks {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 static void reject(byte[] bytes)throws Exception{try{ProjectCodec.decode(bytes);throw new AssertionError("Accepted invalid project");}catch(IOException expected){}}
 public static void main(String[] args)throws Exception{
  var d=new Artwork();d.brush=1;d.begin();PixelTools.line(d,0,0,127,127,(byte)116,PixelTools.Tool.BRUSH,0);d.commit();for(int i=0;i<128;i++)check(d.pixels()[i*128+i]==116,"Unbroken line");check(d.undo(),"Undo stroke");check(Arrays.equals(d.flatten(),Artwork.white()),"Undo restores document");check(d.redo(),"Redo stroke");
  d.undo();d.begin();PixelTools.shape(d,PixelTools.Tool.FILLED_RECTANGLE,20,20,30,30,(byte)116);d.commit();check(!d.redo(),"New edit invalidates redo");check(d.pixels()[25*128+25]==116,"Filled rectangle interior");check(d.pixels()[19*128+25]==34,"Rectangle bounds");
  d.begin();PixelTools.shape(d,PixelTools.Tool.FILLED_ELLIPSE,40,40,60,60,(byte)116);d.commit();check(d.pixels()[50*128+50]==116,"Ellipse center");check(d.pixels()[40*128+40]==34,"Ellipse excludes corners");
  d.addLayer();d.mirrorX=true;d.mirrorY=true;d.begin();PixelTools.stamp(d,10,20,(byte)172);d.commit();for(int x:new int[]{10,117})for(int y:new int[]{20,107})check(d.flatten()[y*128+x]==(byte)172,"Both mirror axes");
  d.layers.get(1).visible=false;check(d.flatten()[20*128+10]==34,"Hidden layer");d.layers.get(1).visible=true;d.original=new int[Artwork.PIXELS];Arrays.fill(d.original,0xff123456);d.title="Test project";
  byte[] encoded=ProjectCodec.encode(d);var loaded=ProjectCodec.decode(encoded);check(Arrays.equals(d.flatten(),loaded.flatten()),"Project pixels");check(loaded.layers.size()==2&&loaded.active==1&&loaded.mirrorX,"Editable layers/settings");check(Arrays.equals(d.original,loaded.original),"Source preview preserved");
  loaded.duplicateLayer();check(loaded.layers.size()==3,"Layer duplicate");loaded.moveLayer(-1);check(loaded.active==1,"Layer ordering");loaded.mergeDown();check(loaded.layers.size()==2,"Merge");check(loaded.undo(),"Undo layer operation");
  reject(Arrays.copyOf(encoded,encoded.length-1));reject(new byte[ProjectCodec.MAX_BYTES+1]);byte[] bad=encoded.clone();bad[7]=99;reject(bad);
  var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes);out.writeInt(ProjectCodec.MAGIC);out.writeInt(1);out.writeInt(128);out.writeInt(128);out.writeUTF("Legacy");out.write(Artwork.white());check(ProjectCodec.decode(bytes.toByteArray()).layers.size()==1,"Version 1 migration");
  Path root=Files.createTempDirectory("studio-checks");var store=new ProjectStore(root);UUID id=UUID.randomUUID();store.save(id,d);check(store.list().size()==1,"Gallery");store.rename(id,"Renamed");check(store.load(id).title.equals("Renamed"),"Rename");UUID copy=store.duplicate(id);check(store.list().size()==2,"Duplicate");Path shared=store.export(copy);UUID imported=store.importFile(shared);check(Arrays.equals(store.load(imported).flatten(),d.flatten()),"Share round trip");store.delete(copy);check(store.list().size()==2,"Delete only selected project");
  Path victim=root.resolve("victim");Files.writeString(victim,"untouched");UUID unsafe=UUID.randomUUID();try{Files.createSymbolicLink(root.resolve("canvasstudio/projects/"+unsafe+".csp"),victim);try{store.save(unsafe,d);throw new AssertionError("Symlink write allowed");}catch(IOException expected){}check(Files.readString(victim).equals("untouched"),"Symlink target preserved");}catch(UnsupportedOperationException expected){}
  System.out.println("PASS artwork tools, undo/redo, layers, mirrors, project roundtrip/migration/rejection, gallery operations, sharing and safe paths");
 }
}
