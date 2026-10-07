package studio.canvas;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(CanvasStudio.ID)
public final class CanvasStudio {
    public static final String ID = "canvasstudio";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredItem<Item> CANVAS = ITEMS.registerSimpleItem("canvas", p -> p.stacksTo(1));
    public static final DeferredItem<Item> QUILL = ITEMS.registerSimpleItem("quill");
    public CanvasStudio(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(CanvasStudio::creative);
        bus.addListener(CanvasStudio::network);
    }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CANVAS); event.accept(QUILL);
        }
    }
    private static void network(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");
        registrar.playToServer(SavePainting.TYPE, SavePainting.CODEC, SavePainting::handle);
        registrar.playToServer(DrawItem.TYPE, DrawItem.CODEC, DrawItem::handle);
    }
}
