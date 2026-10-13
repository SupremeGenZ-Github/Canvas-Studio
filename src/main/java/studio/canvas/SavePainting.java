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
public record SavePainting(InteractionHand hand, String title, byte[] pixels, java.util.UUID editToken, int width, int height) implements CustomPacketPayload {
    public SavePainting(InteractionHand hand,String title,byte[] pixels){this(hand,title,pixels,CanvasReply.NONE,1,1);}
    public SavePainting(InteractionHand hand,String title,byte[] pixels,java.util.UUID token){this(hand,title,pixels,token,1,1);}
    public static final int SIZE = 128;
    public static final int PIXELS = SIZE * SIZE;
    public static final Type<SavePainting> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CanvasStudio.ID, "save"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SavePainting> CODEC = new StreamCodec<>() {
        public SavePainting decode(RegistryFriendlyByteBuf b) {
            var hand = b.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            String title = b.readUtf(64);
            byte[] pixels = new byte[PIXELS];
            b.readBytes(pixels);
            var token=b.readUUID();var size=PaintingSize.STREAM.decode(b);return new SavePainting(hand,title,pixels,token,size.width(),size.height());
        }
        public void encode(RegistryFriendlyByteBuf b, SavePainting p) {
            if (p.pixels == null || p.pixels.length != PIXELS) throw new IllegalArgumentException("Invalid canvas size");
            b.writeBoolean(p.hand == InteractionHand.OFF_HAND);
            b.writeUtf(p.title, 64); b.writeBytes(p.pixels);b.writeUUID(p.editToken);PaintingSize.STREAM.encode(b,new PaintingSize(p.width,p.height));
        }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(SavePainting p, IPayloadContext context) {StudioService.saveLegacy(p,context);}
}
