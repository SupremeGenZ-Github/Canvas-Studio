package studio.canvas;
import java.util.*;
import java.io.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
/** All mutations happen on the authoritative server thread after complete validation. */
public final class StudioService {
 private record Incoming(ArtUpload header,ArtAssembly assembly){}
 private static final WeakHashMap<ServerPlayer,Incoming> INCOMING=new WeakHashMap<>();
 private static final WeakHashMap<ServerPlayer,Long> LAST=new WeakHashMap<>();
 private static final WeakHashMap<ServerPlayer,ArrayDeque<UUID>> CLOSED=new WeakHashMap<>();
 private static final WeakHashMap<MinecraftServer,StudioStore> STORES=new WeakHashMap<>();
 private StudioService(){}
 private static StudioStore store(ServerPlayer p){var server=p.level().getServer();return STORES.computeIfAbsent(server,s->new StudioStore(s.getWorldPath(LevelResource.ROOT)));}
 private static void remember(ServerPlayer p,UUID id){var recent=CLOSED.computeIfAbsent(p,x->new ArrayDeque<>());recent.addLast(id);while(recent.size()>64)recent.removeFirst();}
 private static boolean allowed(ServerPlayer p,ArtUpload u){
  if(!p.mayBuild())return false;
  if(u.action()==ArtUpload.WALL)return StudioSessions.validate(p,u.hand(),u.token(),true)!=null;
  if(u.action()==ArtUpload.PUBLISH){if(!ServerRules.GALLERY.get())return false;var wall=StudioSessions.validate(p,u.hand(),u.token(),true);if(wall!=null)return StudioSessions.properties(wall.wall().getItem()).mayChangeLock(p.getUUID(),StudioSessions.admin(p));}
  if(u.action()!=ArtUpload.HELD&&u.action()!=ArtUpload.PUBLISH)return false;
  var held=p.getItemInHand(u.hand());if(held.getCount()!=1)return false;if(CanvasStudio.isCanvas(held))return true;
  if(StudioSessions.validate(p,u.hand(),u.token(),false)==null)return false;
  return u.action()!=ArtUpload.PUBLISH||StudioSessions.properties(held).mayChangeLock(p.getUUID(),StudioSessions.admin(p));
 }
 public static void upload(ArtUpload u,IPayloadContext c){
  if(!(c.player() instanceof ServerPlayer p))return;long now=p.level().getGameTime();
  try{
   if(!allowed(p,u)||u.count()<1||u.count()>ArtBundle.MAX_FRAMES||u.index()<0||u.index()>=u.count())throw new IllegalArgumentException("Painting permission/session expired. Reopen the canvas or painting.");
   var incoming=INCOMING.get(p);
   if(u.index()==0){if(CLOSED.getOrDefault(p,new ArrayDeque<>()).contains(u.transfer()))throw new IllegalArgumentException("Duplicate artwork request");if(now-LAST.getOrDefault(p,-1000L)<20)throw new IllegalArgumentException("Please wait a moment before another upload");LAST.put(p,now);incoming=new Incoming(u,new ArtAssembly(u.transfer(),u.count(),now));INCOMING.put(p,incoming);}
   if(incoming==null||!incoming.header.sameHeader(u))throw new IllegalArgumentException("Artwork transfer changed or expired");
   incoming.assembly.add(u.transfer(),u.index(),u.count(),now,u.pixels());if(!incoming.assembly.complete())return;
   INCOMING.remove(p);remember(p,u.transfer());var art=new ArtBundle(u.title(),u.size(),u.options(),incoming.assembly.frames());
   if(u.action()==ArtUpload.PUBLISH){store(p).publish(p.getUUID(),p.getName().getString(),art);c.reply(new CanvasReply(u.transfer(),CanvasReply.NONE,5,"Painting published to the server gallery."));return;}
   save(p,u.hand(),u.token(),u.action()==ArtUpload.WALL,art);c.reply(new CanvasReply(u.transfer(),CanvasReply.NONE,4,"Painting saved."));
  }catch(IllegalArgumentException|IOException e){INCOMING.remove(p);remember(p,u.transfer());c.reply(new CanvasReply(u.transfer(),CanvasReply.NONE,3,e.getMessage()==null?"Artwork request rejected":e.getMessage()));}
 }
 private static ItemStack makeMap(ServerPlayer p,ArtBundle art,PaintingProperties previous)throws IOException{
  List<Integer> ids=new ArrayList<>();ItemStack first=null;
  for(byte[] raw:art.frames()){
   ItemStack map=MapItem.create(p.level(),0,0,(byte)0,false,false);var data=MapItem.getSavedData(map,p.level());if(data==null)throw new IOException("Could not create painting map");
   byte[] pixels=PaintLayout.apply(raw,art.size(),art.options().fit());System.arraycopy(pixels,0,data.colors,0,pixels.length);data=data.locked();p.level().setMapData(map.get(DataComponents.MAP_ID),data);data.setDirty();ids.add(map.get(DataComponents.MAP_ID).id());if(first==null)first=map;
  }
  UUID owner=previous.owner().equals(PaintingProperties.NONE)?p.getUUID():previous.owner();if(previous.revision()==Integer.MAX_VALUE)throw new IOException("Painting revision limit reached");
  first.set(PaintingProperties.TYPE,new PaintingProperties(owner,art.options(),previous.revision()+1));first.set(PaintingAnimation.TYPE,new PaintingAnimation(ids));first.set(PaintingSize.TYPE,art.size());
  first.set(DataComponents.CUSTOM_NAME,Component.literal(art.title().isBlank()?"Custom Painting":art.title()));
  first.set(DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(List.of(Component.literal("Wall: "+art.size().width()+" x "+art.size().height()+" | "+PaintOptions.FRAMES[art.options().frame()]),Component.literal(art.frames().size()+" animation frame(s) | "+(art.options().locked()?"Owner locked":"Public editing")))));
  store(p).saveMap(ids.getFirst(),art);return first;
 }
 public static void save(ServerPlayer p,InteractionHand hand,UUID token,boolean onWall,ArtBundle art)throws IOException{
  StudioSessions.Session session=null;WallPainting wall=null;ItemStack old=p.getItemInHand(hand);PaintingSize before=null;
  if(onWall){session=StudioSessions.validate(p,hand,token,true);if(session==null)throw new IOException("Wall painting session expired or changed");wall=session.wall();old=wall.getItem();before=wall.displaySize();}
  else if(!CanvasStudio.isCanvas(old)){session=StudioSessions.validate(p,hand,token,false);if(session==null)throw new IOException("Held painting session expired or changed");}
  if(!p.mayBuild()||(!onWall&&old.getCount()!=1))throw new IOException("Hold one canvas or saved painting");
  var previous=StudioSessions.properties(old);if(!previous.mayEdit(p.getUUID(),StudioSessions.admin(p)))throw new IOException("Painting is locked by its owner");
  if(previous.options().locked()!=art.options().locked()&&!previous.mayChangeLock(p.getUUID(),StudioSessions.admin(p)))throw new IOException("Only the owner or an administrator can change the lock");
  if(wall!=null){
   for(var front:art.size().cells(wall.getPos(),wall.getDirection())){var back=front.relative(wall.getDirection().getOpposite());if(!p.level().hasChunkAt(front)||!p.level().hasChunkAt(back)||!p.level().mayInteract(p,back)||!p.mayUseItemAt(front,wall.getDirection(),old))throw new IOException("The whole resized area must be loaded and allow building");}
   wall.setDisplaySize(art.size());boolean valid=wall.survives();wall.setDisplaySize(before);if(!valid)throw new IOException("The resized painting needs supported wall space without obstructions");
  }
  final WallPainting target=wall;
  int existingFrame=CanvasStudio.isCanvas(old)?0:previous.options().frame();
  FrameMaterials.save(existingFrame,art.options().frame(),p.getInventory(),p.getOffhandItem(),()->{
   ItemStack saved=makeMap(p,art,previous);
   if(target!=null){target.setDisplaySize(art.size());target.setItem(saved,false);}else p.setItemInHand(hand,saved);
  });
  p.inventoryMenu.broadcastChanges();
  StudioSessions.close(p);OpenCanvas.SESSIONS.remove(p);
 }
 private static ArtBundle source(ServerPlayer p,ItemStack item)throws IOException{
  var existing=store(p).readMap(StudioSessions.base(item));if(existing.isPresent())return existing.get();
  var data=MapItem.getSavedData(item,p.level());if(data==null||!data.locked)throw new IOException("Only locked painting maps can be edited");String name=item.getHoverName().getString();name=name.replaceAll("[\\p{Cntrl}§]","");if(name.length()>64)name=name.substring(0,64);
  return new ArtBundle(name,item.getOrDefault(PaintingSize.TYPE,PaintingSize.ONE),StudioSessions.properties(item).options(),List.of(data.colors.clone()));
 }
 public static void openHeld(ServerPlayer p,InteractionHand hand,UUID request){
  var item=p.getItemInHand(hand);if(!item.is(Items.FILLED_MAP)||item.getCount()!=1)return;
  try{if(!p.mayBuild()||!StudioSessions.properties(item).mayEdit(p.getUUID(),StudioSessions.admin(p)))throw new IOException("Painting is locked by its owner");var art=source(p,item);var session=StudioSessions.begin(p,hand,item,null);send(p,request,ArtUpload.HELD,hand,session.token(),art);}catch(IOException e){p.sendSystemMessage(Component.literal(e.getMessage()),false);}
 }
 public static void openWall(ServerPlayer p,WallPainting wall,InteractionHand hand){
  try{if(!StudioSessions.wallPermission(p,wall))throw new IOException("This painting is locked or outside your build permission");var art=source(p,wall.getItem());var session=StudioSessions.begin(p,hand,wall.getItem(),wall);send(p,UUID.randomUUID(),ArtUpload.WALL,hand,session.token(),art);}catch(IOException e){p.sendSystemMessage(Component.literal(e.getMessage()),false);}
 }
 private static void send(ServerPlayer p,UUID request,int action,InteractionHand hand,UUID token,ArtBundle art){for(int i=0;i<art.frames().size();i++)PacketDistributor.sendToPlayer(p,new ArtDownload(request,action,hand,token,art.title(),art.size(),art.options(),i,art.frames().size(),art.frames().get(i)));}
 public static void gallery(GalleryRequest request,IPayloadContext c){
  if(!(c.player() instanceof ServerPlayer p))return;
  try{if(!ServerRules.GALLERY.get())throw new IOException("The server gallery is disabled");if(request.page()<0||request.page()>166)throw new IOException("Invalid gallery page");
   if(request.action()==GalleryRequest.LOAD){send(p,request.request(),ArtUpload.PUBLISH,InteractionHand.MAIN_HAND,CanvasReply.NONE,store(p).read(request.entry()));return;}
   if(request.action()==GalleryRequest.DELETE&&!store(p).delete(request.entry(),p.getUUID(),StudioSessions.admin(p)))throw new IOException("Only the artist or an administrator can delete this entry");
   if(request.action()!=GalleryRequest.LIST&&request.action()!=GalleryRequest.DELETE)throw new IOException("Invalid gallery request");
   var all=store(p).list();int from=Math.min(all.size(),request.page()*GalleryPage.PAGE_SIZE),to=Math.min(all.size(),from+GalleryPage.PAGE_SIZE);c.reply(new GalleryPage(request.request(),request.page(),all.size(),all.subList(from,to),"Shared artwork from this world"));
  }catch(IOException|IllegalArgumentException e){c.reply(new GalleryPage(request.request(),0,0,List.of(),e.getMessage()==null?"Gallery request failed":e.getMessage()));}
 }
 public static void saveLegacy(SavePainting request,IPayloadContext context){if(!(context.player() instanceof ServerPlayer p))return;try{var old=StudioSessions.properties(p.getItemInHand(request.hand()));var art=new ArtBundle(request.title(),new PaintingSize(request.width(),request.height()),old.options(),List.of(request.pixels()));save(p,request.hand(),request.editToken(),false,art);context.reply(new CanvasReply(request.editToken(),CanvasReply.NONE,4,"Painting saved."));}catch(IOException|IllegalArgumentException e){context.reply(new CanvasReply(request.editToken(),CanvasReply.NONE,3,"Painting save rejected: "+e.getMessage()));}}
}
