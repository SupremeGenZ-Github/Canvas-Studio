package studio.canvas.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import studio.canvas.CanvasStudio;

/** Loaded only on the physical client; server code never resolves GUI/AWT classes. */
@Mod(value=CanvasStudio.ID, dist=Dist.CLIENT)
public final class CanvasClient {
    static CanvasScreen activeCanvas;
    public CanvasClient(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(CanvasClient::rightClick);
        NeoForge.EVENT_BUS.addListener(CanvasClient::copyExisting);
        NeoForge.EVENT_BUS.addListener(PlacementPreview::tick);
        NeoForge.EVENT_BUS.addListener(PlacementPreview::hud);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event)->PlacementPreview.clear());
        bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event)->event.registerEntityRenderer(CanvasStudio.WALL_PAINTING.get(),context->new WallPaintingRenderer(context)));
        bus.addListener((net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event)->event.addListener(net.minecraft.resources.Identifier.fromNamespaceAndPath(CanvasStudio.ID,"catalogue_reload"),(net.minecraft.server.packs.resources.ResourceManagerReloadListener)resources->ItemMatchCatalog.RESOURCE_EPOCH.incrementAndGet()));
    }
    public static void reply(studio.canvas.CanvasReply reply){
        var screen=Minecraft.getInstance().gui.screen();
        if(screen instanceof ServerGalleryScreen gallery)gallery.reply(reply);
        else if(screen instanceof ItemMatchScreen matches)matches.reply(reply);
        else if(activeCanvas!=null)activeCanvas.reply(reply);
    }
    static java.util.UUID paintingRequest;
    public static void painting(studio.canvas.PaintingData data){
        Minecraft mc=Minecraft.getInstance();
        if(!data.request().equals(paintingRequest)||mc.player==null||!mc.player.getItemInHand(data.hand()).is(net.minecraft.world.item.Items.FILLED_MAP))return;
        paintingRequest=null;var screen=new CanvasScreen(data.hand());
        var document=new studio.canvas.client.art.Artwork(data.pixels());document.title=data.title();document.wallWidth=data.width();document.wallHeight=data.height();
        screen.loadProject(java.util.UUID.randomUUID(),document);screen.editingPainting=true;screen.paintingToken=data.token();
        mc.setScreenAndShow(screen);
    }
    private static void copyExisting(PlayerInteractEvent.EntityInteract event){
        var mc=Minecraft.getInstance();if(event.getEntity()!=mc.player||!mc.player.isShiftKeyDown()||!CanvasStudio.isCanvas(event.getItemStack())||!(event.getTarget() instanceof net.minecraft.world.entity.decoration.painting.Painting painting))return;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);var editor=new CanvasScreen(event.getHand(),CanvasStudio.tier(event.getItemStack()));var importer=new MinecraftPaintingsScreen(editor);mc.setScreenAndShow(importer);importer.importExisting(painting.getVariant().value().assetId());
    }
    private static void rightClick(PlayerInteractEvent.RightClickItem event) {
        Minecraft mc = Minecraft.getInstance();
        if(event.getEntity()==mc.player&&mc.player.isShiftKeyDown()&&event.getItemStack().is(net.minecraft.world.item.Items.FILLED_MAP)){
            event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);paintingRequest=java.util.UUID.randomUUID();
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new studio.canvas.OpenPainting(event.getHand(),paintingRequest));return;
        }
        if (event.getEntity() == mc.player && CanvasStudio.isCanvas(event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            mc.setScreenAndShow(new CanvasScreen(event.getHand(),CanvasStudio.tier(event.getItemStack())));
        }
    }
}
