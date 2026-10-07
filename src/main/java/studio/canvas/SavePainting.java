package studio.canvas;

import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** One fixed-size map image, no paths or original file data leave the client. */
public record SavePainting(InteractionHand hand, String title, byte[] pixels) implements CustomPacketPayload {
    public static final int SIZE = 128;
    public static final int PIXELS = SIZE * SIZE;
    public static final Type<SavePainting> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID, "save"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SavePainting> CODEC = new StreamCodec<>() {
        public SavePainting decode(RegistryFriendlyByteBuf b) {
            var hand = b.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            String title = b.readUtf(64);
            byte[] pixels = new byte[PIXELS];
            b.readBytes(pixels);
            return new SavePainting(hand, title, pixels);
        }
        public void encode(RegistryFriendlyByteBuf b, SavePainting p) {
            if (p.pixels.length != PIXELS) throw new IllegalArgumentException("Invalid canvas size");
            b.writeBoolean(p.hand == InteractionHand.OFF_HAND);
            b.writeUtf(p.title, 64); b.writeBytes(p.pixels);
        }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(SavePainting p, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        ItemStack held = player.getItemInHand(p.hand);
        if (!held.is(CanvasStudio.CANVAS.get()) || held.getCount() != 1 || p.pixels.length != PIXELS) return;
        // Color ids 0..3 are transparent; reject invalid palette entries before allocating map data.
        for (byte color : p.pixels) if (Byte.toUnsignedInt(color) > 247) return;
        ItemStack painting = MapItem.create(player.level(), 0, 0, (byte)0, false, false);
        MapItemSavedData data = MapItem.getSavedData(painting, player.level());
        if (data == null) return;
        System.arraycopy(p.pixels, 0, data.colors, 0, PIXELS);
        data = data.locked();
        player.level().setMapData(painting.get(DataComponents.MAP_ID), data);
        data.setDirty();
        String name = p.title.replaceAll("[\\p{Cntrl}§]", "").strip();
        painting.set(DataComponents.CUSTOM_NAME, Component.literal(name.isEmpty() ? "Custom Painting" : name));
        // Replacing the held canvas is atomic on the server thread and prevents packet duplication.
        player.setItemInHand(p.hand, painting);
        player.inventoryMenu.broadcastChanges();
        player.sendSystemMessage(Component.literal("Painting saved! Place it in an item frame."), false);
    }
}
