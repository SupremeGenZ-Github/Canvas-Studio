import studio.canvas.client.ImageImport;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

public class ImageImportTest {
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    public static void main(String[] args) throws Exception {
        Path folder=Files.createTempDirectory("canvas-tests");
        try {
            BufferedImage source=new BufferedImage(256,128,BufferedImage.TYPE_INT_RGB);
            for(int y=0;y<128;y++)for(int x=0;x<256;x++)source.setRGB(x,y,0xff0000);
            Path png=folder.resolve("source.png");ImageIO.write(source,"png",png.toFile());
            check(ImageImport.load(png).getWidth()==256,"PNG decode");
            Path jpeg=folder.resolve("source.jpg");ImageIO.write(source,"jpeg",jpeg.toFile());
            check(ImageImport.load(jpeg).getHeight()==128,"JPEG decode");
            BufferedImage fit=ImageImport.resize(source,false),crop=ImageImport.resize(source,true);
            check((fit.getRGB(0,0)&0xffffff)==0xffffff,"Fit letterbox");
            check((fit.getRGB(64,64)&0xffffff)==0xff0000,"Fit center");
            check((crop.getRGB(0,0)&0xffffff)==0xff0000,"Crop fills image");
            BufferedImage transparent=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
            check((ImageImport.resize(transparent,false).getRGB(64,64)&0xffffff)==0xffffff,"Alpha onto white");
            Path wide=folder.resolve("wide.png");ImageIO.write(new BufferedImage(8193,1,BufferedImage.TYPE_INT_RGB),"png",wide.toFile());expectRejected(wide);
            Path gif=folder.resolve("bad.gif");ImageIO.write(source,"gif",gif.toFile());
            expectRejected(gif);Path bad=folder.resolve("corrupt.png");Files.writeString(bad,"not an image");expectRejected(bad);
            Path huge=folder.resolve("huge.png");try(var f=new java.io.RandomAccessFile(huge.toFile(),"rw")){f.setLength(16*1024*1024+1);}expectRejected(huge);
            int[] palette={0,0,0,0,0xffffff,0xff0000,0x0000ff};
            byte[] converted=ImageImport.quantize(crop,palette);
            check(converted.length==16384,"Fixed payload length");
            for(byte b:converted)check(b==5,"Red palette match");
            System.out.println("PASS: PNG/JPEG decode, fit/crop, transparency, GIF/corrupt/oversize rejection, map quantization.");
        }finally{try(var paths=Files.list(folder)){for(Path p:paths.toList())Files.delete(p);}Files.delete(folder);}
    }
    static void expectRejected(Path p)throws Exception{try{ImageImport.load(p);throw new AssertionError("Accepted "+p);}catch(java.io.IOException expected){}}
}
