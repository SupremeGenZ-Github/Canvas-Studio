package studio.canvas;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(CanvasStudio.ID)
public final class CanvasStudio {
    public static final String ID = "canvasstudio";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredItem<Item> CANVAS = ITEMS.registerSimpleItem("canvas", p -> p.stacksTo(1).component(DataComponents.LORE, lore("Draw, import and save paintings", CanvasTier.BLANK.availability)));
    public static final DeferredItem<Item> QUILL = ITEMS.registerSimpleItem("quill", p -> p.component(DataComponents.LORE, lore("Crafts a Blank Canvas")));
    // Preserve 2.1.0 registry IDs so saved Molder items become Molten without migration.
    public static final DeferredItem<Item> MOLTEN_CANVAS = ITEMS.registerSimpleItem("molder_canvas", p -> p.stacksTo(1).component(DataComponents.LORE, lore("Draw, import and save paintings", CanvasTier.MOLTEN.availability)));
    public static final DeferredItem<Item> INFINITY_CANVAS = ITEMS.registerSimpleItem("infinity_canvas", p -> p.stacksTo(1).component(DataComponents.LORE, lore("Draw, import and save paintings", CanvasTier.INFINITY.availability)));
    public static final DeferredItem<Item> MOLTEN_QUILL = ITEMS.registerSimpleItem("molder_quill", p -> p.component(DataComponents.LORE, lore("Crafts a single-use Molten Canvas")));
    public static final DeferredItem<Item> INFINITY_QUILL = ITEMS.registerSimpleItem("infinity_quill", p -> p.component(DataComponents.LORE, lore("Crafts a reusable Infinity Canvas")));
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STUDIO_TAB = TABS.register("studio", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.canvasstudio.studio"))
            .icon(() -> new ItemStack(INFINITY_CANVAS.get()))
            .displayItems((parameters, output) -> {
                output.accept(CANVAS.get()); output.accept(MOLTEN_CANVAS.get()); output.accept(INFINITY_CANVAS.get());
                output.accept(QUILL.get()); output.accept(MOLTEN_QUILL.get()); output.accept(INFINITY_QUILL.get());
            }).build());
    private static ItemLore lore(String... lines){return new ItemLore(java.util.Arrays.stream(lines).map(Component::literal).map(c -> (Component)c).toList());}
    public static CanvasTier tier(ItemStack stack){
        if(stack.is(CANVAS.get()))return CanvasTier.BLANK;
        if(stack.is(MOLTEN_CANVAS.get()))return CanvasTier.MOLTEN;
        if(stack.is(INFINITY_CANVAS.get()))return CanvasTier.INFINITY;
        return null;
    }
    public static boolean isCanvas(ItemStack stack){return tier(stack)!=null;}
    public CanvasStudio(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
        bus.addListener(CanvasStudio::network);
    }
    private static void network(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("3").executesOn(net.neoforged.neoforge.network.registration.HandlerThread.MAIN);
        registrar.playToServer(SavePainting.TYPE, SavePainting.CODEC, SavePainting::handle);
        registrar.playToServer(DrawItem.TYPE, DrawItem.CODEC, DrawItem::handle);
        registrar.playToServer(OpenCanvas.TYPE, OpenCanvas.CODEC, OpenCanvas::handle);
        registrar.playToClient(CanvasReply.TYPE, CanvasReply.CODEC, CanvasReply::handle);
    }
}
