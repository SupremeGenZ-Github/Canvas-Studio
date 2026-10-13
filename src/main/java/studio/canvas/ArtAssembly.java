package studio.canvas;
import java.util.*;
/** Sequential, one-transfer-at-a-time assembly; duplicates never complete an extra save. */
public final class ArtAssembly {
 private final UUID id;private final int count;private final long expires;private final List<byte[]> frames=new ArrayList<>();
 public ArtAssembly(UUID id,int count,long now){if(count<1||count>ArtBundle.MAX_FRAMES)throw new IllegalArgumentException("Frame count");this.id=id;this.count=count;expires=now+200;}
 public boolean accepts(UUID transfer,int index,int total,long now){return id.equals(transfer)&&total==count&&index==frames.size()&&index<count&&now<=expires;}
 public void add(UUID transfer,int index,int total,long now,byte[] pixels){if(!accepts(transfer,index,total,now))throw new IllegalArgumentException("Out-of-order, duplicate or expired frame");ArtBundle.validatePixels(pixels);frames.add(pixels.clone());}
 public boolean complete(){return frames.size()==count;}
 public List<byte[]> frames(){if(!complete())throw new IllegalStateException("Incomplete artwork");return List.copyOf(frames);}
}
