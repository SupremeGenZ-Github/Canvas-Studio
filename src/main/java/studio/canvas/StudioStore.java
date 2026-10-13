package studio.canvas;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
/** World-local immutable artwork and a small public-gallery index. */
public final class StudioStore {
 public record Entry(UUID id,UUID owner,String author,String title,int width,int height,int frames,long created){}
 private final Path root;private final List<Entry> entries=new ArrayList<>();private boolean loaded;
 public StudioStore(Path world){root=world.resolve("canvasstudio");}
 private Path folder(String name)throws IOException{if(Files.isSymbolicLink(root))throw new IOException("Unsafe studio folder");Files.createDirectories(root);Path p=root.resolve(name);if(Files.isSymbolicLink(p))throw new IOException("Unsafe gallery folder");Files.createDirectories(p);return p;}
 private static byte[] bounded(Path file,int max)throws IOException{if(Files.isSymbolicLink(file)||!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)||Files.size(file)>max)throw new IOException("Invalid artwork path or size");return Files.readAllBytes(file);}
 private static void atomic(Path file,byte[] bytes)throws IOException{if(Files.isSymbolicLink(file))throw new IOException("Unsafe file");Path temp=Files.createTempFile(file.getParent(),"studio-",".tmp");try{Files.write(temp,bytes);try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}}
 public void saveMap(int map,ArtBundle art)throws IOException{if(map<0)throw new IOException("Map ID");atomic(folder("paintings").resolve(map+".csa"),art.encode());}
 public Optional<ArtBundle> readMap(int map)throws IOException{if(map<0)return Optional.empty();Path p=folder("paintings").resolve(map+".csa");return Files.exists(p,LinkOption.NOFOLLOW_LINKS)?Optional.of(ArtBundle.decode(bounded(p,ArtBundle.MAX_FILE))):Optional.empty();}
 private void load()throws IOException{
  if(loaded)return;Path index=folder("gallery").resolve("index.json");if(Files.exists(index,LinkOption.NOFOLLOW_LINKS))try{
   JsonArray data=JsonParser.parseString(new String(bounded(index,512*1024),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonArray();if(data.size()>1000)throw new IOException("Gallery index too large");
   for(JsonElement element:data){var o=element.getAsJsonObject();var e=new Entry(UUID.fromString(o.get("id").getAsString()),UUID.fromString(o.get("owner").getAsString()),o.get("author").getAsString(),o.get("title").getAsString(),o.get("width").getAsInt(),o.get("height").getAsInt(),o.get("frames").getAsInt(),o.get("created").getAsLong());if(e.title.length()>64||e.author.length()>64||!PaintingSize.valid(e.width,e.height)||e.frames<1||e.frames>16)throw new IOException("Invalid gallery entry");entries.add(e);}
  }catch(RuntimeException e){entries.clear();throw new IOException("Invalid gallery index",e);}loaded=true;
 }
 private void index()throws IOException{JsonArray data=new JsonArray();for(var e:entries){var o=new JsonObject();o.addProperty("id",e.id.toString());o.addProperty("owner",e.owner.toString());o.addProperty("author",e.author);o.addProperty("title",e.title);o.addProperty("width",e.width);o.addProperty("height",e.height);o.addProperty("frames",e.frames);o.addProperty("created",e.created);data.add(o);}atomic(folder("gallery").resolve("index.json"),data.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));}
 public List<Entry> list()throws IOException{load();return entries.stream().sorted(Comparator.comparingLong(Entry::created).reversed()).toList();}
 public Entry publish(UUID owner,String author,ArtBundle art)throws IOException{
  load();if(entries.size()>=1000||entries.stream().filter(e->e.owner.equals(owner)).count()>=64)throw new IOException("Gallery limit: 64 paintings per artist, 1000 per world");
  var e=new Entry(UUID.randomUUID(),owner,author,art.title(),art.size().width(),art.size().height(),art.frames().size(),System.currentTimeMillis());Path p=folder("gallery").resolve(e.id+".csa");atomic(p,art.encode());entries.add(e);try{index();}catch(IOException failure){entries.remove(e);Files.deleteIfExists(p);throw failure;}return e;
 }
 public ArtBundle read(UUID id)throws IOException{load();if(entries.stream().noneMatch(e->e.id.equals(id)))throw new IOException("Painting is no longer in the gallery");return ArtBundle.decode(bounded(folder("gallery").resolve(id+".csa"),ArtBundle.MAX_FILE));}
 public boolean delete(UUID id,UUID actor,boolean admin)throws IOException{load();var entry=entries.stream().filter(e->e.id.equals(id)).findFirst();if(entry.isEmpty()||(!entry.get().owner.equals(actor)&&!admin))return false;entries.remove(entry.get());try{index();}catch(IOException failure){entries.add(entry.get());throw failure;}Files.deleteIfExists(folder("gallery").resolve(id+".csa"));return true;}
}
