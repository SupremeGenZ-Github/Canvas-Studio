package studio.canvas.client;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Decodes only a local PNG/JPEG after checking compressed and decoded sizes. */
public final class ImageImport {
    private ImageImport() {}
    public static BufferedImage load(Path path) throws IOException {
        if (!Files.isRegularFile(path) || Files.size(path) > 16 * 1024 * 1024)
            throw new IOException("Choose a PNG/JPEG file smaller than 16 MB.");
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            if (input == null) throw new IOException("Cannot open image.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Unsupported image. Use PNG or JPEG.");
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg") && !format.equalsIgnoreCase("jpg"))
                    throw new IOException("Only PNG and JPEG are supported.");
                reader.setInput(input, true, true);
                int w = reader.getWidth(0), h = reader.getHeight(0);
                if (w <= 0 || h <= 0 || w > 8192 || h > 8192 || (long)w * h > 16_000_000)
                    throw new IOException("Image too large: maximum 16 megapixels / 8192 px per side.");
                return reader.read(0);
            } finally { reader.dispose(); }
        }
    }
    public static BufferedImage resize(BufferedImage source, boolean crop) {
        BufferedImage result = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setColor(java.awt.Color.WHITE); g.fillRect(0, 0, 128, 128);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            double scale = crop ? Math.max(128.0/source.getWidth(),128.0/source.getHeight())
                                : Math.min(128.0/source.getWidth(),128.0/source.getHeight());
            int w = Math.max(1, (int)Math.round(source.getWidth()*scale));
            int h = Math.max(1, (int)Math.round(source.getHeight()*scale));
            g.drawImage(source, (128-w)/2, (128-h)/2, w, h, null);
        } finally { g.dispose(); }
        return result;
    }
    public static byte[] quantize(BufferedImage image, int[] palette) {
        byte[] pixels = new byte[128*128];
        // The small cache avoids repeatedly searching the palette for flat-color artwork.
        java.util.Map<Integer,Byte> cache = new java.util.HashMap<>();
        for (int y=0;y<128;y++) for (int x=0;x<128;x++) {
            int rgb = image.getRGB(x,y) & 0xffffff;
            pixels[y*128+x] = cache.computeIfAbsent(rgb, c -> nearest(c,palette));
        }
        return pixels;
    }
    public static byte nearest(int rgb, int[] palette) {
        int best=4; long distance=Long.MAX_VALUE;
        for (int i=4;i<palette.length;i++) {
            int c=palette[i];
            int dr=((rgb>>16)&255)-((c>>16)&255), dg=((rgb>>8)&255)-((c>>8)&255), db=(rgb&255)-(c&255);
            long d=2L*dr*dr+4L*dg*dg+3L*db*db;
            if (d<distance) { distance=d; best=i; }
        }
        return (byte)best;
    }
}
