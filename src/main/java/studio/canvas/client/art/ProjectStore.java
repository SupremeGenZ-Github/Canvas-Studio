package studio.canvas.client.art;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
/** Local file operations run through one executor, preserving save order. */
public final class ProjectStore {
 public static final ExecutorService IO=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Canvas Studio files");t.setDaemon(true);return t;});
 public record Entry(UUID id,Artwork artwork){}
 private final Path directory;
 public ProjectStore(Path game)throws IOException{Path root=game.toAbsolutePath().normalize();Path studio=root.resolve("canvasstudio");directory=studio.resolve("projects");safeDirectory(studio);safeDirectory(directory);}
 private static void safeDirectory(Path p)throws IOException{if(Files.isSymbolicLink(p))throw new IOException("Symbolic project directories are not supported");Files.createDirectories(p);}
 private Path file(UUID id)throws IOException{if(Files.isSymbolicLink(directory)||Files.isSymbolicLink(directory.getParent()))throw new IOException("Unsafe project directory");Path p=directory.resolve(id+".csp");if(Files.isSymbolicLink(p))throw new IOException("Symbolic project files are not supported");return p;}
 public void save(UUID id,Artwork document)throws IOException{byte[] data=ProjectCodec.encode(document);Path target=file(id),tmp=Files.createTempFile(directory,"saving-",".tmp");try{Files.write(tmp,data);try{Files.move(tmp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(tmp);}}
 public Artwork load(UUID id)throws IOException{return read(file(id));}
 public static Artwork read(Path path)throws IOException{if(Files.isSymbolicLink(path))throw new IOException("Symbolic project files are not supported");try(var input=Files.newInputStream(path)){return ProjectCodec.decode(input.readNBytes(ProjectCodec.MAX_BYTES+1));}}
 public List<Entry> list()throws IOException{List<Entry> result=new ArrayList<>();try(var paths=Files.list(directory)){for(Path p:paths.filter(p->p.getFileName().toString().endsWith(".csp")).sorted().limit(200).toList())try{String n=p.getFileName().toString();UUID id=UUID.fromString(n.substring(0,n.length()-4));result.add(new Entry(id,load(id)));}catch(IOException|IllegalArgumentException ignored){/* One corrupt file must not hide the remaining projects. */}}return result;}
 public UUID importFile(Path source)throws IOException{Artwork d=read(source);UUID id=UUID.randomUUID();save(id,d);return id;}
 public UUID duplicate(UUID id)throws IOException{Artwork d=load(id);d.title=(d.title.length()>59?d.title.substring(0,59):d.title)+" copy";UUID copy=UUID.randomUUID();save(copy,d);return copy;}
 public void rename(UUID id,String title)throws IOException{Artwork d=load(id);d.title=title;save(id,d);}
 public void delete(UUID id)throws IOException{Files.deleteIfExists(file(id));}
 public Path export(UUID id)throws IOException{Artwork d=load(id);Path exports=directory.getParent().resolve("exports");safeDirectory(exports);Path target=exports.resolve(UUID.randomUUID()+".csp");Files.write(target,ProjectCodec.encode(d),StandardOpenOption.CREATE_NEW);return target;}
}
