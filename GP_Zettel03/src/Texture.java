import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class Texture {
    static Random random = new Random();

    public static int loadTexture(GLAutoDrawable d, String filename) {
        GL2 gl = d.getGL().getGL2(); // get the OpenGL 2 graphics context

        int width;
        int height;
        int level = 0;
        int border = 0;

        try {
            // open file
            FileInputStream fileInputStream = new FileInputStream(new File(filename));
            // read image
            BufferedImage bufferedImage = ImageIO.read(fileInputStream);
            fileInputStream.close();

            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
            int[] pixelIntData = new int[width * height];
            // convert image to ByteBuffer
            bufferedImage.getRGB(0, 0, width, height, pixelIntData, 0, width);
            ByteBuffer buffer = ByteBuffer.allocateDirect(pixelIntData.length * 4);
            buffer.order(ByteOrder.nativeOrder());

            // Unpack the data, each integer into 4 bytes of the ByteBuffer.
            // Also we need to vertically flip the image because the image origin
            // in OpenGL is the lower-left corner.
            for (int y = 0; y < height; y++) {
                int k = (height - 1 - y) * width;
                for (int x = 0; x < width; x++) {
                    buffer.put((byte) (pixelIntData[k] >>> 16));
                    buffer.put((byte) (pixelIntData[k] >>> 8));
                    buffer.put((byte) (pixelIntData[k]));
                    buffer.put((byte) (pixelIntData[k] >>> 24));
                    k++;
                }
            }
            buffer.rewind();

            // data is aligned in byte order
            gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);

            // request textureID
            final int[] textureID = new int[1];
            gl.glGenTextures(1, textureID, 0);

            // bind texture
            gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID[0]);

            // define how to filter the texture
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);

            // texture colors should replace the original color values
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE); // GL_MODULATE

            // specify the 2D texture map
            gl.glTexImage2D(GL2.GL_TEXTURE_2D, level, GL2.GL_RGB, width, height, border, GL2.GL_RGBA,
                    GL2.GL_UNSIGNED_BYTE, buffer);

            return textureID[0];
        } catch (FileNotFoundException e) {
            System.out.println("Can not find texture data file " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
