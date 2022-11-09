// This code example is created for educational purpose
// by Thorsten Thormaehlen (contact: www.thormae.de).
// It is distributed without any warranty.

import java.awt.*;
import java.awt.event.*;
import java.net.StandardSocketOptions;
import java.util.Scanner;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2.*;

class Renderer {
    int mode = VertexZusammenbauen.GL_POINTS.ordinal();
    int red=0;
    int blue=0;
    int green=0;
    float[] array = {
            0.29761907f,0.45421583f,
            0.20238096f,0.29368397f,
            0.74999997f,0.29368397f,
            0.65476197f,0.45421583f,
            -0.02701211f,0.7499916f,
            -0.02701211f,-0.75000379f,
            0.10714287f,-0.74977316f,
            0.10714287f,0.74852419f,
            0.34523807f,0.29368397f,
            0.34523807f,-0.1647749f,
            0.47714287f,-0.1647749f,
            0.47714287f,0.29368397f,
            0.47714287f,0.29368397f,
            0.47714287f,-0.1647749f,
            0.60714287f,-0.1647749f,
            0.60714287f,0.29368397f,
            0.65476197f,0.29368397f,
            0.65476197f,-0.10764567f,
            0.74999997f,-0.10764567f,
            0.74999997f,0.29368397f,
            0.20238096f,0.29368397f,
            0.20238096f,-0.10764567f,
            0.29761907f,-0.10764567f,
            0.29761907f,0.29368397f,
            0.41666667f,0.74852424f,
            0.36904767f,0.641503f,
            0.58333337f,0.641503f,
            0.53571427f,0.74852424f,
            0.41666667f,0.53448177f,
            0.36904767f,0.64150297f,
            0.58333337f,0.64150297f,
            0.53571427f,0.53448177f,
            -0.60714285f,0.45421583f,
            -0.70238095f,0.29368397f,
            -0.1547619f,0.29368397f,
            -0.24999999f,0.45421583f,
            -0.5595238f,-0.29493284f,
            -0.5595238f,-0.7497731f,
            -0.46428571f,-0.7497731f,
            -0.46428571f,-0.29493284f,
            -0.39285714f,-0.29493284f,
            -0.39285714f,-0.7497731f,
            -0.29761904f,-0.7497731f,
            -0.29761904f,-0.29493284f,
            -0.24999999f,0.29368397f,
            -0.20238094f,-0.05413505f,
            -0.10714285f,-0.02737975f,
            -0.1547619f,0.29368397f,
            -0.48809523f,0.74852424f,
            -0.53571428f,0.641503f,
            -0.32142856f,0.641503f,
            -0.36904761f,0.74852424f,
            -0.48809523f,0.53448177f,
            -0.53571428f,0.64150297f,
            -0.32142856f,0.64150297f,
            -0.36904761f,0.53448177f,
            -0.5595238f,0.29368397f,
            -0.67857142f,-0.29493284f,
            -0.17857142f,-0.29493284f,
            -0.32142856f,0.29368397f,
            -0.60714285f,0.29368397f,
            -0.6547619f,-0.05413505f,
            -0.75f,-0.02737975f,
            -0.70238095f,0.29368397f,
            0.51190477f,-0.1647749f,
            0.51190477f,-0.74977308f,
            0.60714287f,-0.74977308f,
            0.60714287f,-0.1647749f,
            0.34523807f,-0.1647749f,
            0.34523807f,-0.74977308f,
            0.44047617f,-0.74977308f,
            0.44047617f,-0.1647749f};
    public void init(GLAutoDrawable d) {}
    public void resize(GLAutoDrawable d, int width, int height) {
        GL2 gl = d.getGL().getGL2(); // get the OpenGL 2 graphics context
        gl.glViewport(0, 0, width, height);
    }
    public void display(GLAutoDrawable d) {


        GL2 gl = d.getGL().getGL2();  // get the OpenGL 2 graphics context
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);

        gl.glBegin(mode);

        for(int x=0; x<array.length; x++){
            for (int y=x; y<array.length; y++){
                int colRed = (red + (x+y) * 10) % 256, colGreen = (green + (x+y) * 10) % 256, colBlue = (blue + (x+y) * 10) % 256;
                gl.glColor3f((float) colRed / 255, (float) colGreen / 255, (float) colBlue / 255);
                gl.glVertex2f(array[x], array[y]);
            }
        }
        gl.glEnd();
    }
    public void dispose(GLAutoDrawable d) {}
}


class MyGui extends JFrame implements GLEventListener{

    private Renderer renderer;

    public void createGUI() {
        setTitle("JoglFirstTriangle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        setSize(320, 320);
        canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char key = e.getKeyChar();
                switch (key) {
                    case 'r':
                        renderer.red = (renderer.red+1) % 256;
                        break;
                    case 'g':
                        renderer.green = (renderer.green+1) % 256;
                        break;
                    case 'b':
                        renderer.blue = (renderer.blue+1) % 256;
                        break;
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                switch (key){
                    case KeyEvent.VK_1: {
                        renderer.mode = VertexZusammenbauen.GL_POINTS.ordinal();
                        break;
                    }
                    case KeyEvent.VK_2: {
                        renderer.mode = VertexZusammenbauen.GL_LINE_STRIP.ordinal();
                        break;
                    }
                    case KeyEvent.VK_3: {
                        renderer.mode = VertexZusammenbauen.GL_LINE_LOOP.ordinal();
                        break;
                    }
                    case KeyEvent.VK_4: {
                        renderer.mode = VertexZusammenbauen.GL_TRIANGLES.ordinal();
                        break;
                    }
                    case KeyEvent.VK_5: {
                        renderer.mode = VertexZusammenbauen.GL_TRIANGLE_STRIP.ordinal();
                        break;
                    }
                    case KeyEvent.VK_6: {
                        renderer.mode = VertexZusammenbauen.GL_TRIANGLE_FAN.ordinal();
                        break;
                    }
                    case KeyEvent.VK_7: {
                        renderer.mode = VertexZusammenbauen.GL_QUADS.ordinal();
                        break;
                    }
                    case KeyEvent.VK_8: {
                        renderer.mode = VertexZusammenbauen.GL_QUAD_STRIP.ordinal();
                        break;
                    }
                    case KeyEvent.VK_9: {
                        renderer.mode = VertexZusammenbauen.GL_POLYGON.ordinal();
                        break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        getContentPane().add(canvas);
        final FPSAnimator ani = new FPSAnimator(canvas, 60, true);
        canvas.addGLEventListener(this);
        setVisible(true);
        renderer = new Renderer();
        ani.start();
    }

    @Override
    public void init(GLAutoDrawable d) {
        renderer.init(d);
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int width, int height) {
        renderer.resize(d, width, height);
    }

    @Override
    public void display(GLAutoDrawable d) {
        renderer.display(d);
    }

    @Override
    public void dispose(GLAutoDrawable d) {
        renderer.dispose(d);
    }
}

public class FirstTriangle {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MyGui myGUI = new MyGui();
                myGUI.createGUI();
            }
        });
    }
}
