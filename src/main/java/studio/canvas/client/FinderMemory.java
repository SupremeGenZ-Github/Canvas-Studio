package studio.canvas.client;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import studio.canvas.client.art.ProjectStore;
final class FinderMemory {
 final LinkedHashSet<String> favorites=new LinkedHashSet<>();final ArrayList<String> recent=new ArrayList<>();
 static FinderMemory load(Path root){var data=new FinderMemory();try{Path file=root.resolve("canvasstudio/finder.json");if(!Files.exists(file)||Files.isSymbolicLink(file))return data;byte[] bytes;try(var in=Files.newInputStream(file)){bytes=in.readNBytes(65537);}if(bytes.length>65536)return data;var json=com.google.gson.JsonParser.parseString(new String(bytes,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();for(var x:json.getAsJsonArray("favorites"))if(data.favorites.size()<256&&valid(x.getAsString()))data.favorites.add(x.getAsString());for(var x:json.getAsJsonArray("recent"))if(data.recent.size()<50&&valid(x.getAsString()))data.recent.add(x.getAsString());}catch(Exception ignored){}return data;}
 private static boolean valid(String id){return id.length()<=256&&net.minecraft.resources.Identifier.tryParse(id)!=null;}
 void toggle(String id){if(!favorites.remove(id)&&favorites.size()<256)favorites.add(id);}
 void record(java.util.List<ItemMatcher.Match> results){for(int i=results.size()-1;i>=0;i--){String id=results.get(i).id();recent.remove(id);recent.addFirst(id);}while(recent.size()>50)recent.removeLast();}
 void save(Path root){var json=new com.google.gson.JsonObject();var f=new com.google.gson.JsonArray();favorites.forEach(f::add);var r=new com.google.gson.JsonArray();recent.forEach(r::add);json.add("favorites",f);json.add("recent",r);String text=json.toString();java.util.concurrent.CompletableFuture.runAsync(()->{try{Path dir=root.resolve("canvasstudio");if(Files.isSymbolicLink(dir))return;Files.createDirectories(dir);Path file=dir.resolve("finder.json");if(Files.isSymbolicLink(file))return;Path tmp=Files.createTempFile(dir,"finder-",".tmp");try{Files.writeString(tmp,text);Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(tmp);}}catch(IOException ignored){}},ProjectStore.IO);}
}
