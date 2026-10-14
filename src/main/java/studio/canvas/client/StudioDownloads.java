package studio.canvas.client;
import studio.canvas.*;
import studio.canvas.client.art.Artwork;
import net.minecraft.client.Minecraft;
import java.util.*;
/** Assemble bounded server replies before changing the active editor. */
public final class StudioDownloads {
 private record Incoming(ArtDownload header,ArtAssembly data){}
 private static final Map<UUID,Incoming> INCOMING=new HashMap<>();
 private StudioDownloads(){}
 private static boolean expected(ArtDownload p){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return false;if(p.action()==ArtUpload.WALL)return true;if(p.action()==ArtUpload.HELD)return p.request().equals(CanvasClient.paintingRequest);return mc.gui.screen() instanceof ServerGalleryScreen s&&s.expects(p.request());}
 public static void receive(ArtDownload p){
  var mc=Minecraft.getInstance();if(!expected(p)||p.count()<1||p.count()>16)return;
  try{
   if(p.index()==0){INCOMING.clear();INCOMING.put(p.request(),new Incoming(p,new ArtAssembly(p.request(),p.count(),mc.level.getGameTime())));}
   var a=INCOMING.get(p.request());if(a==null)return;var h=a.header;if(h.action()!=p.action()||h.hand()!=p.hand()||!h.token().equals(p.token())||!h.title().equals(p.title())||!h.size().equals(p.size())||!h.options().equals(p.options()))throw new IllegalArgumentException("Changed download");
   a.data.add(p.request(),p.index(),p.count(),mc.level.getGameTime(),p.pixels());if(!a.data.complete())return;INCOMING.remove(p.request());
   var art=new ArtBundle(p.title(),p.size(),p.options(),a.data.frames());CanvasScreen screen;
   if(p.action()==ArtUpload.PUBLISH){if(!(mc.gui.screen() instanceof ServerGalleryScreen gallery))return;screen=gallery.parent;}
   else{screen=new CanvasScreen(p.hand());screen.editingPainting=true;screen.editingWall=p.action()==ArtUpload.WALL;screen.paintingToken=p.token();CanvasClient.paintingRequest=null;}
   if(p.action()!=ArtUpload.PUBLISH)screen.savedFrameStyle=art.options().frame();
   load(screen,art);screen.status=p.action()==ArtUpload.PUBLISH?"Gallery artwork imported. Save it using your canvas or edit session.":"Painting loaded with its original animation and display settings.";mc.setScreenAndShow(screen);
  }catch(IllegalArgumentException e){INCOMING.remove(p.request());if(CanvasClient.activeCanvas!=null)CanvasClient.activeCanvas.status="Artwork download rejected: "+e.getMessage();}
 }
 public static void load(CanvasScreen screen,ArtBundle art){
  var d=new Artwork(art.frames().getFirst());d.title=art.title();d.wallWidth=art.size().width();d.wallHeight=art.size().height();d.frameStyle=art.options().frame();d.fitMode=art.options().fit();d.frameDelay=art.options().delay();d.locked=art.options().locked();
  for(byte[] pixels:art.frames()){var f=new Artwork.Frame();f.layers.add(new Artwork.Layer("Background",pixels));d.timeline.add(f);}d.animationIndex=0;d.loadFrame();screen.loadProject(UUID.randomUUID(),d);
 }
}
