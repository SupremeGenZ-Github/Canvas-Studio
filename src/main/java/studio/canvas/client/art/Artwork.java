package studio.canvas.client.art;
import java.util.*;
/** All tools edit one layer-ready document; rewards and maps receive only flattened pixels. */
public final class Artwork {
 public static final int SIZE=128,PIXELS=SIZE*SIZE,MAX_LAYERS=16;
 public static final class Layer {
  public String name; public boolean visible=true; public final byte[] pixels;
  public Layer(String name,byte[] pixels){this.name=name;this.pixels=pixels.clone();}
  public Layer copy(){var l=new Layer(name,pixels);l.visible=visible;return l;}
 }
 public final List<Layer> layers=new ArrayList<>();
 public String title="Custom Painting";
 public int active,brush=2,snap=1,wallWidth=1,wallHeight=1;public boolean grid,mirrorX,mirrorY;
 public String tool="BRUSH"; public byte selected=116;
 public int[] original; // Optional fitted source pixels, never sent to the server.
 public int frameStyle,fitMode,frameDelay=20,animationIndex;public boolean locked;
 public static final int MAX_FRAMES=16;
 public static final class Frame {
  public final List<Layer> layers=new ArrayList<>();public int active;public int[] original;
  public Frame copy(){var f=new Frame();for(var l:layers)f.layers.add(l.copy());f.active=active;f.original=original==null?null:original.clone();return f;}
 }
 public final List<Frame> timeline=new ArrayList<>();
 public void syncFrame(){var f=new Frame();for(var l:layers)f.layers.add(l.copy());f.active=active;f.original=original==null?null:original.clone();if(timeline.isEmpty())timeline.add(f);else timeline.set(animationIndex,f);}
 public void selectFrame(int index){syncFrame();if(index<0||index>=timeline.size())return;animationIndex=index;loadFrame();}
 public void loadFrame(){var f=timeline.get(animationIndex);layers.clear();for(var l:f.layers)layers.add(l.copy());active=f.active;original=f.original==null?null:f.original.clone();}
 public void addFrame(boolean blank){if(timeline.size()>=MAX_FRAMES)return;begin();syncFrame();var f=timeline.get(animationIndex).copy();if(blank){f.layers.clear();f.layers.add(new Layer("Background",white()));f.active=0;f.original=null;}timeline.add(animationIndex+1,f);animationIndex++;loadFrame();commit();}
 public void deleteFrame(){syncFrame();if(timeline.size()==1)return;begin();timeline.remove(animationIndex);animationIndex=Math.min(animationIndex,timeline.size()-1);loadFrame();commit();}
 public List<byte[]> imageSequence(){syncFrame();List<byte[]> frames=new ArrayList<>();for(var f:timeline){byte[] p=white();for(var l:f.layers)if(l.visible)for(int i=0;i<PIXELS;i++)if(Byte.toUnsignedInt(l.pixels[i])>=4)p[i]=l.pixels[i];frames.add(p);}return List.copyOf(frames);}
 private final ArrayDeque<Artwork> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
 private Artwork pending;
 public Artwork(byte[] base){if(base.length!=PIXELS)throw new IllegalArgumentException("Canvas dimensions");layers.add(new Layer("Background",base));}
 public Artwork(){this(white());}
 public static byte[] white(){byte[] p=new byte[PIXELS];Arrays.fill(p,(byte)34);return p;}
 public byte[] pixels(){return layers.get(active).pixels;}
 public byte[] flatten(){byte[] p=white();for(Layer l:layers)if(l.visible)for(int i=0;i<PIXELS;i++)if(Byte.toUnsignedInt(l.pixels[i])>=4)p[i]=l.pixels[i];return p;}
 public Artwork copy(){syncFrame();var d=new Artwork();d.layers.clear();for(var l:layers)d.layers.add(l.copy());d.title=title;d.active=active;d.wallWidth=wallWidth;d.wallHeight=wallHeight;d.brush=brush;d.snap=snap;d.grid=grid;d.mirrorX=mirrorX;d.mirrorY=mirrorY;d.tool=tool;d.selected=selected;d.original=original==null?null:original.clone();d.frameStyle=frameStyle;d.fitMode=fitMode;d.frameDelay=frameDelay;d.locked=locked;d.animationIndex=animationIndex;d.timeline.clear();for(var f:timeline)d.timeline.add(f.copy());return d;}
 private void restore(Artwork d){animationIndex=d.animationIndex;timeline.clear();for(var f:d.timeline)timeline.add(f.copy());layers.clear();for(var l:d.layers)layers.add(l.copy());title=d.title;active=d.active;original=d.original==null?null:d.original.clone();}
 public void begin(){if(pending==null)pending=copy();}
 public void commit(){if(pending!=null){if(!same(pending)){push(undo,pending);redo.clear();}pending=null;}}
 public void cancel(){if(pending!=null){restore(pending);pending=null;}}
 public void previewReset(){if(pending!=null)System.arraycopy(pending.layers.get(active).pixels,0,pixels(),0,PIXELS);}
 private boolean same(Artwork d){syncFrame();if(animationIndex!=d.animationIndex||timeline.size()!=d.timeline.size())return false;for(int n=0;n<timeline.size();n++){var a=timeline.get(n);var b=d.timeline.get(n);if(a.active!=b.active||a.layers.size()!=b.layers.size()||!Arrays.equals(a.original,b.original))return false;for(int i=0;i<a.layers.size();i++){var x=a.layers.get(i);var y=b.layers.get(i);if(x.visible!=y.visible||!x.name.equals(y.name)||!Arrays.equals(x.pixels,y.pixels))return false;}}if(layers.size()!=d.layers.size()||active!=d.active||!title.equals(d.title)||!Arrays.equals(original,d.original))return false;for(int i=0;i<layers.size();i++){var a=layers.get(i);var b=d.layers.get(i);if(!a.name.equals(b.name)||a.visible!=b.visible||!Arrays.equals(a.pixels,b.pixels))return false;}return true;}
 private static void push(ArrayDeque<Artwork> q,Artwork d){if(q.size()==32)q.removeFirst();q.addLast(d);}
 public boolean undo(){commit();if(undo.isEmpty())return false;push(redo,copy());restore(undo.removeLast());return true;}
 public boolean redo(){if(redo.isEmpty())return false;push(undo,copy());restore(redo.removeLast());return true;}
 public void addLayer(){if(layers.size()>=MAX_LAYERS)return;begin();layers.add(new Layer("Layer "+(layers.size()+1),new byte[PIXELS]));active=layers.size()-1;commit();}
 public void duplicateLayer(){if(layers.size()>=MAX_LAYERS)return;begin();var l=layers.get(active).copy();l.name+=" copy";layers.add(active+1,l);active++;commit();}
 public void moveLayer(int direction){int n=active+direction;if(n<0||n>=layers.size())return;begin();Collections.swap(layers,active,n);active=n;commit();}
 public void mergeDown(){if(active==0)return;begin();var upper=layers.get(active);var lower=layers.get(active-1);if(!lower.visible)Arrays.fill(lower.pixels,(byte)0);if(upper.visible)for(int i=0;i<PIXELS;i++)if(Byte.toUnsignedInt(upper.pixels[i])>=4)lower.pixels[i]=upper.pixels[i];lower.visible=lower.visible||upper.visible;layers.remove(active);active--;commit();}
}
