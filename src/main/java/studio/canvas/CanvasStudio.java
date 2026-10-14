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
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
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
    public static final DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,ID);
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,net.minecraft.core.component.DataComponentType<Long>> USES=COMPONENTS.register("infinity_uses",()->CanvasUsage.TYPE);
    public static final DeferredItem<Item> KURLO_PILLOW=ITEMS.registerSimpleItem("kurlo_pillow",p->p.stacksTo(1).component(DataComponents.LORE,lore("Hold in either hand through a full night's sleep","Wake with enchanted golden apple status effects")));
    public static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,ID);
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>,net.minecraft.world.entity.EntityType<WallPainting>> WALL_PAINTING=ENTITIES.register("wall_painting",()->net.minecraft.world.entity.EntityType.Builder.<WallPainting>of(WallPainting::new,net.minecraft.world.entity.MobCategory.MISC).sized(0.5f,0.5f).clientTrackingRange(10).updateInterval(1).build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE,net.minecraft.resources.Identifier.fromNamespaceAndPath(ID,"wall_painting"))));
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,net.minecraft.core.component.DataComponentType<PaintingSize>> PAINTING_SIZE=COMPONENTS.register("painting_size",()->PaintingSize.TYPE);
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,net.minecraft.core.component.DataComponentType<PaintingProperties>> PAINTING_PROPERTIES=COMPONENTS.register("painting_properties",()->PaintingProperties.TYPE);
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,net.minecraft.core.component.DataComponentType<PaintingAnimation>> PAINTING_ANIMATION=COMPONENTS.register("painting_animation",()->PaintingAnimation.TYPE);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,ID);
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> STUDIO_TAB=TABS.register("studio",()->CreativeModeTab.builder()
      .title(Component.literal("Canvas Studio+")) .icon(()->new ItemStack(INFINITY_CANVAS.get()))
      .displayItems((parameters,output)->{output.accept(CANVAS.get());output.accept(MOLTEN_CANVAS.get());output.accept(INFINITY_CANVAS.get());output.accept(QUILL.get());output.accept(MOLTEN_QUILL.get());output.accept(INFINITY_QUILL.get());output.accept(KURLO_PILLOW.get());}).build());
    private static ItemLore lore(String... lines){return new ItemLore(java.util.Arrays.stream(lines).map(Component::literal).map(c -> (Component)c).toList());}
    public static CanvasTier tier(ItemStack stack){
        if(stack.is(CANVAS.get()))return CanvasTier.BLANK;
        if(stack.is(MOLTEN_CANVAS.get()))return CanvasTier.MOLTEN;
        if(stack.is(INFINITY_CANVAS.get()))return CanvasTier.INFINITY;
        return null;
    }
    public static boolean isCanvas(ItemStack stack){return tier(stack)!=null;}
    public CanvasStudio(IEventBus bus, ModContainer container) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,WallPlacement::place);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KurloPillow::tick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KurloPillow::wake);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KurloPillow::logout);
        COMPONENTS.register(bus);
        TABS.register(bus);
        container.registerConfig(ServerRules.configType(),ServerRules.SPEC,"canvasstudio-server.toml");
        bus.addListener(CanvasStudio::network);
    }
    private static void network(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("8").executesOn(net.neoforged.neoforge.network.registration.HandlerThread.MAIN);
        registrar.playToServer(PaintingLock.TYPE,PaintingLock.CODEC,PaintingLock::handle);
        registrar.playToServer(ArtUpload.TYPE,ArtUpload.CODEC,ArtUpload::handle);
        registrar.playToClient(ArtDownload.TYPE,ArtDownload.CODEC,ArtDownload::handle);
        registrar.playToServer(GalleryRequest.TYPE,GalleryRequest.CODEC,GalleryRequest::handle);
        registrar.playToClient(GalleryPage.TYPE,GalleryPage.CODEC,GalleryPage::handle);
        registrar.playToServer(SavePainting.TYPE, SavePainting.CODEC, SavePainting::handle);
        registrar.playToServer(DrawItem.TYPE, DrawItem.CODEC, DrawItem::handle);
        registrar.playToServer(OpenPainting.TYPE,OpenPainting.CODEC,OpenPainting::handle);
        registrar.playToClient(PaintingData.TYPE,PaintingData.CODEC,PaintingData::handle);
        registrar.playToServer(OpenCanvas.TYPE, OpenCanvas.CODEC, OpenCanvas::handle);
        registrar.playToClient(CanvasReply.TYPE, CanvasReply.CODEC, CanvasReply::handle);
    }
}
