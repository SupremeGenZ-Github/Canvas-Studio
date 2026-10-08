package studio.canvas;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
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
    public static final DeferredItem<Item> CANVAS = ITEMS.registerSimpleItem("canvas", p -> p.stacksTo(1).component(DataComponents.LORE, lore("Draw, import and save paintings", CanvasTier.BLANK.availability)));
    public static final DeferredItem<Item> QUILL = ITEMS.registerSimpleItem("quill", p -> p.component(DataComponents.LORE, lore("Crafts a Blank Canvas")));
    public static final DeferredItem<Item> MOLDER_CANVAS = ITEMS.registerSimpleItem("molder_canvas", p -> p.stacksTo(1).component(DataComponents.LORE, lore("Draw, import and save paintings", CanvasTier.MOLDER.availability)));
    public static final DeferredItem<Item> INFINITY_CANVAS = ITEMS.registerSimpleItem("infinity_canvas", p -> p.stacksTo(1).component(DataComponents.LORE, lore("Draw, import and save paintings", CanvasTier.INFINITY.availability)));
    public static final DeferredItem<Item> MOLDER_QUILL = ITEMS.registerSimpleItem("molder_quill", p -> p.component(DataComponents.LORE, lore("Crafts a single-use Molder Canvas")));
    public static final DeferredItem<Item> INFINITY_QUILL = ITEMS.registerSimpleItem("infinity_quill", p -> p.component(DataComponents.LORE, lore("Crafts a reusable Infinity Canvas")));
    private static ItemLore lore(String... lines){return new ItemLore(java.util.Arrays.stream(lines).map(Component::literal).map(c -> (Component)c).toList());}
    public static CanvasTier tier(ItemStack stack){
        if(stack.is(CANVAS.get()))return CanvasTier.BLANK;
        if(stack.is(MOLDER_CANVAS.get()))return CanvasTier.MOLDER;
        if(stack.is(INFINITY_CANVAS.get()))return CanvasTier.INFINITY;
        return null;
    }
    public static boolean isCanvas(ItemStack stack){return tier(stack)!=null;}
    public CanvasStudio(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(CanvasStudio::creative);
        bus.addListener(CanvasStudio::network);
    }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CANVAS); event.accept(MOLDER_CANVAS); event.accept(INFINITY_CANVAS);
            event.accept(QUILL); event.accept(MOLDER_QUILL); event.accept(INFINITY_QUILL);
        }
    }
    private static void network(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("3").executesOn(net.neoforged.neoforge.network.registration.HandlerThread.MAIN);
        registrar.playToServer(SavePainting.TYPE, SavePainting.CODEC, SavePainting::handle);
        registrar.playToServer(DrawItem.TYPE, DrawItem.CODEC, DrawItem::handle);
        registrar.playToServer(OpenCanvas.TYPE, OpenCanvas.CODEC, OpenCanvas::handle);
        registrar.playToClient(CanvasReply.TYPE, CanvasReply.CODEC, CanvasReply::handle);
    }
}
