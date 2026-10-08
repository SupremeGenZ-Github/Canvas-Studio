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
    public CanvasClient(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(CanvasClient::rightClick);
    }
    public static void reply(studio.canvas.CanvasReply reply){
        var screen=Minecraft.getInstance().gui.screen();
        if(screen instanceof CanvasScreen canvas)canvas.reply(reply);
        else if(screen instanceof ItemMatchScreen matches)matches.reply(reply);
    }
    private static void rightClick(PlayerInteractEvent.RightClickItem event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() == mc.player && CanvasStudio.isCanvas(event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            mc.setScreenAndShow(new CanvasScreen(event.getHand(),CanvasStudio.tier(event.getItemStack())));
        }
    }
}
