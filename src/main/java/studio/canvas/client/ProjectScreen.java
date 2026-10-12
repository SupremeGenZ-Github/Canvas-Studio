package studio.canvas.client;
import studio.canvas.client.art.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public final class ProjectScreen extends Screen {
 private final CanvasScreen parent;private java.util.List<ProjectStore.Entry> entries=java.util.List.of();private int page,selected=-1,rows;private boolean loading;private UUID deleteConfirm;private EditBox field;private String input="",status="Loading projects...";
 ProjectScreen(CanvasScreen parent){super(Component.literal("Painting projects"));this.parent=parent;}
 private interface Work{void run(ProjectStore store)throws Exception;}
 private void work(Work action,String success){if(loading)return;loading=true;Path root=minecraft.gameDirectory.toPath();status="Working...";CompletableFuture.supplyAsync(()->{try{var store=new ProjectStore(root);action.run(store);return store.list();}catch(Exception e){throw new CompletionException(e);}},ProjectStore.IO).whenComplete((result,error)->minecraft.execute(()->{loading=false;if(error!=null)status="Project error: "+error.getCause().getMessage();else{entries=result;status=success;}selected=-1;refresh();}));}
 private UUID chosen(){return selected>=0&&selected<entries.size()?entries.get(selected).id():null;}
 private void action(int i,String label,Runnable run){int w=Math.min(95,(width-24)/3);addRenderableWidget(Button.builder(Component.literal(label),b->{input=field.getValue();if(!loading)run.run();}).bounds(width/2-(w*3+8)/2+i%3*(w+4),height-76+i/3*23,w,20).build());}
 @Override protected void init(){rows=Math.max(1,(height-150)/38);page=Math.max(0,Math.min(page,Math.max(0,(entries.size()-1)/rows)));for(int i=0;i<rows;i++){int index=page*rows+i;if(index>=entries.size())break;var entry=entries.get(index);final int n=index;addRenderableWidget(Button.builder(Component.literal((selected==index?"> ":"")+entry.artwork().title),b->{selected=n;deleteConfirm=null;input=entry.artwork().title;refresh();}).bounds(width/2-100,40+i*38,235,32).build());}
  field=new EditBox(font,width/2-145,height-101,290,20,Component.literal("New name or full path to .csp file"));field.setMaxLength(1024);field.setValue(input);addRenderableWidget(field);
  action(0,"Open",()->{if(chosen()!=null){var e=entries.get(selected);parent.loadProject(e.id(),e.artwork());onClose();}});
  action(1,"Save current",()->{parent.saveProject(false);status="Saving current artwork...";work(s->{},"Saved; select a project.");});
  action(2,"Import .csp",()->work(s->s.importFile(Path.of(input)),"Imported shared project."));
  action(3,"Rename",()->{UUID id=chosen();if(id!=null)work(s->s.rename(id,input),"Renamed.");});action(4,"Duplicate",()->{UUID id=chosen();if(id!=null)work(s->s.duplicate(id),"Duplicated.");});
  action(5,"Export .csp",()->{UUID id=chosen();if(id!=null)work(s->s.export(id),"Shared file saved in canvasstudio/exports.");});
  addRenderableWidget(Button.builder(Component.literal("<"),b->{page--;refresh();}).bounds(6,18,22,18).build());addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;refresh();}).bounds(width-28,18,22,18).build());
  addRenderableWidget(Button.builder(Component.literal(deleteConfirm==null?"Delete":"Confirm delete"),b->{UUID id=chosen();if(id!=null){if(id.equals(deleteConfirm))work(s->s.delete(id),"Deleted selected project.");else{deleteConfirm=id;status="Click Confirm delete to remove this project.";refresh();}}}).bounds(width/2-145,height-29,115,20).build());addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(width/2+55,height-29,90,20).build());
  if(entries.isEmpty()&&!loading&&status.equals("Loading projects..."))work(s->{},"Select a project. Name/path field below.");
 }
 private void refresh(){clearWidgets();init();}
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float t){g.fill(0,0,width,height,0xff181c26);g.centeredText(font,"PROJECTS — page "+(page+1),width/2,9,0xfff5ddaa);g.centeredText(font,font.plainSubstrByWidth(status,width-65),width/2,25,0xffbec8d8);for(int i=0;i<rows;i++){int index=page*rows+i;if(index<entries.size())ArtRender.draw(g,entries.get(index).artwork().flatten(),parent.palette,width/2-138,40+i*38,32);}super.extractRenderState(g,x,y,t);}
}
