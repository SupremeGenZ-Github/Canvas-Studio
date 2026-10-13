package studio.canvas.client;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
/** Resource-pack painting decoder with the same image-size protections as file imports. */
public final class PaintingImport {
 private PaintingImport(){}
 public static BufferedImage read(InputStream source)throws IOException{
  byte[] bytes=source.readNBytes(16*1024*1024+1);if(bytes.length>16*1024*1024)throw new IOException("Painting file exceeds 16 MiB");
  try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IOException("Unsupported painting image");var reader=readers.next();try{reader.setInput(input,true,true);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||w>8192||h>8192||(long)w*h>16_000_000)throw new IOException("Painting texture too large");return reader.read(0);}finally{reader.dispose();}}
 }
 public static BufferedImage square(BufferedImage image){var square=new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);var g=square.createGraphics();try{g.setColor(Color.WHITE);g.fillRect(0,0,128,128);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(image,0,0,128,128,null);}finally{g.dispose();}return square;}
}
