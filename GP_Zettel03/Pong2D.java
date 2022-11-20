import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;


public class Pong2D {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        MyGui myGUI = new MyGui();
                        myGUI.createGUI();
                    }
                });
    }
}

class MyGui extends JFrame implements GLEventListener {
    private Game game;
    private final GLU glu = new GLU();

    public void createGUI() {
        setTitle("Pong2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        final FPSAnimator ani = new FPSAnimator(canvas, 120, true);
        canvas.addGLEventListener(this);
        game = new Game();
        canvas.addKeyListener(game);
        ani.start();

        getContentPane().setPreferredSize(new Dimension(1000, 600));
        getContentPane().add(canvas);
        pack();
        setVisible(true);
        canvas.requestFocus();
    }

    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2(); // get the OpenGL 2 graphics context
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0, 16.0f / 9.0f, 1.5f, 3.5f);
        game.init(d);
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int width, int height) {
        GL2 gl = d.getGL().getGL2(); // get the OpenGL 2 graphics context
        gl.glViewport(0, 0, width, height);

    }

    @Override
    public void display(GLAutoDrawable d) {
        game.update();
        game.display(d);
    }

    @Override
    public void dispose(GLAutoDrawable d) {
    }
}

class Game extends KeyAdapter {
    boolean pauseGame = true;
    Player playerOne;
    Score scoreOne;
    Player playerTwo;
    Score scoreTwo;
    Ball ball;
    GameView view;

    ArrayList<GameObject> gameObjects;

    public Game() {
        gameObjects = new ArrayList<>();

        // instantiate game elements
        ball = new Ball();
        playerOne = new Player(-1.7f, 0f);
        scoreOne = new Score(-0.2f, 1.0f);
        playerTwo = new Player(1.7f, 0f);
        scoreTwo = new Score(0.2f, 1.0f);
        view = new GameView();

        // populate gameobject list
        gameObjects.add(view);
        gameObjects.add(ball);
        gameObjects.add(playerOne);
        gameObjects.add(playerTwo);
        gameObjects.add(scoreOne);
        gameObjects.add(scoreTwo);
    }

    public void init(GLAutoDrawable d) {
    }

    public void display(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2(); // get the OpenGL 2 graphics context

        // clear the screen
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        for (GameObject gameObject : gameObjects) {
            gameObject.display(gl);
        }
        gl.glFlush();
    }

    public void update() {
        for (GameObject gameObject : gameObjects) {
            gameObject.update();
        }
        checkBallCollisionPlayer();
        checkBallCollisionBorder();
    }

    public void startGame() {
        if (scoreOne.getScore() > 2 || scoreTwo.getScore() > 2) {
            scoreOne.setScore(0);
            scoreTwo.setScore(0);
        }
        ball.velocityX = 0.05f;
        ball.velocityY = 0.05f;
        pauseGame = false;
    }

    public void score(Score score) {
        score.setScore(score.getScore() + 1);
        ball.reset();
        pauseGame = true;
    }

    public void checkBallCollisionPlayer() {
        // collision player one
        if (ball.borderLeft < playerOne.borderRight) {
            if (ball.borderDown < playerOne.borderUp && ball.borderUp > playerOne.borderDown) {
                ball.posX = playerOne.borderRight + ball.coordinateX;

                // rotate ball
                ball.rotation = playerOne.velocity * 350;
                // reflect ball
                ball.velocityX = -(ball.velocityX + (ball.rotation * 0.00005f));
                ball.velocityY += (ball.rotation * 0.00015f);
            }
        }

        // collision player two
        if (ball.borderRight > playerTwo.borderLeft) {
            if (ball.borderDown < playerTwo.borderUp && ball.borderUp > playerTwo.borderDown) {
                ball.posX = playerTwo.borderLeft - ball.coordinateX;

                // rotate ball
                ball.rotation = playerTwo.velocity * 350;
                // reflect ball
                ball.velocityX = -ball.velocityX + (ball.rotation * 0.00005f);
                ball.velocityY += (ball.rotation * 0.00015f);
            }
        }
    }

    public void checkBallCollisionBorder() {
        // let and right border
        if (ball.posX > 1.8f) {
            score(scoreOne);
        }
        if (ball.posX < -1.8f) {
            score(scoreTwo);
        }

        // ceiling and ground
        if (ball.posY > 1f) {
            ball.velocityY = -ball.velocityY;
        }
        if (ball.posY < -1f) {
            ball.velocityY = -ball.velocityY;
        }
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                playerOne.moveUp = true;
                break;
            case KeyEvent.VK_S:
                playerOne.moveDown = true;
                break;
            case KeyEvent.VK_P:
                playerTwo.moveUp = true;
                break;
            case KeyEvent.VK_L:
                playerTwo.moveDown = true;
                break;
            case KeyEvent.VK_SPACE:
                if (pauseGame) {
                    startGame();
                }
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                playerOne.moveUp = false;
                break;
            case KeyEvent.VK_S:
                playerOne.moveDown = false;
                break;
            case KeyEvent.VK_P:
                playerTwo.moveUp = false;
                break;
            case KeyEvent.VK_L:
                playerTwo.moveDown = false;
                break;
        }
    }
}


abstract class GameObject {
    float[] vertices = Cube.cube;
    float posX, posY;
    float coordinateX, coordinateY, coordinateZ;
    float rotation;
    float angle;

    public void display(GL2 gl) {
        gl.glLoadIdentity();
        gl.glTranslatef(posX, posY, -2f);
        gl.glScalef(this.coordinateX, this.coordinateY, this.coordinateZ);

        angle = this.angle+rotation;
        gl.glRotatef(angle, 0, 0, 1);

        gl.glBegin(GL2GL3.GL_QUADS);
        for (int i = 0; i < vertices.length; i += 3) {
            // check if side changed
            if (i % 12 == 0) {
                int side = i / 12;
                setColor(side, gl);
            }
            gl.glVertex3f(vertices[i], vertices[i + 1], vertices[i + 2]);
        }
        gl.glEnd();
    }

    private void setColor(int side, GL2 gl) {
        switch (side) {
            case 0:
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                break;
            case 1:
                gl.glColor3f(0.0f, 0.0f, 0.0f);
                break;
            case 2:
            case 3:
                gl.glColor3f(0.5f, 0.5f, 0.5f);
                break;
            case 4:
            case 5:
                gl.glColor3f(0.8f, 0.8f, 0.8f);
                break;
        }
    }

    public void update() {
    }
}
class GameView extends GameObject{
    public void display(GL2 gl) {
        gl.glLoadIdentity();
        gl.glTranslatef(posX, posY, -2.1f);
        gl.glScalef(16f / 9f, 1.0f, 1.0f);

        for (int i = 0; i < vertices.length; i += 3) {
            if (i % 12 == 0) {
                gl.glBegin(GL.GL_LINE_LOOP);
            }
            gl.glVertex3f(vertices[i], vertices[i + 1], vertices[i + 2]);
            if (i % 12 == 9) {
                gl.glEnd();
            }
        }
    }
}

class Player extends GameObject {
    float acceleration;
    float velocity;
    boolean moveUp, moveDown = false;
    final float ACCELERATION_VALUE = 0.012f;
    float borderLeft, borderRight, borderUp, borderDown;

    public Player(float posX, float posY) {
        this.coordinateX=0.05f;
        this.coordinateY=0.35f;
        this.coordinateZ=0.025f;

        this.posX = posX;
        this.posY = posY;
    }

    public void update() {
        this.acceleration = 0.0f;
        if (moveUp) {
            acceleration += ACCELERATION_VALUE;
        }

        if (moveDown) {
            acceleration -= ACCELERATION_VALUE;
        }
        velocity += acceleration;
        velocity *=0.8;
        this.posY += velocity;

        if (this.posY >= 0.85f) {
            this.posY = 0.85f;
        }
        if (this.posY <= -0.85f) {
            this.posY = -0.85f;
        }

        // update collision border
        this.borderLeft = this.posX - this.coordinateX;
        this.borderRight = this.posX + this.coordinateX;
        this.borderUp = this.posY + this.coordinateY;
        this.borderDown = this.posY - this.coordinateY;
    }
}

class Ball extends GameObject {
    float velocityX, velocityY;
    float borderLeft, borderRight, borderUp, borderDown;

    public Ball() {
        this.coordinateX=0.075f;
        this.coordinateY=0.075f;
        this.coordinateZ=0.075f;

    }

    public void update() {
        this.posX += velocityX;
        this.posY += velocityY;

        // update collision border
        this.borderLeft = this.posX - this.coordinateX;
        this.borderRight = this.posX + this.coordinateX;
        this.borderUp = this.posY + this.coordinateY;
        this.borderDown = this.posY - this.coordinateY;
    }

    public void reset() {
        this.velocityX = 0;
        this.velocityY = 0;
        this.posX = 0;
        this.posY = 0;
        this.angle=0;
        this.rotation=0;
    }
}

class Score extends GameObject {
    private int score = 0;

    float[] score0Data = {
            0.06f, 0.1f, 0.04f, 0.1f, 0.04f, -0.1f, 0.06f, -0.1f, -0.04f, 0.1f, -0.06f, 0.1f, -0.06f, -0.1f,
            -0.04f, -0.1f, 0.05f, 0.1f, 0.05f, 0.08f, -0.05f, 0.08f, -0.05f, 0.1f, 0.05f, -0.08f, 0.05f,
            -0.1f, -0.05f, -0.1f, -0.05f, -0.08f
    };

    float[] score1Data = {0.01f, 0.1f, -0.01f, 0.1f, -0.01f, -0.1f, 0.01f, -0.1f};

    float[] score2Data = {
            0.06f, 0.1f, 0.04f, 0.1f, 0.04f, 0.0f, 0.06f, 0.0f, -0.04f, 0.0f, -0.06f, 0.0f, -0.06f, -0.1f,
            -0.04f, -0.1f, 0.05f, 0.1f, 0.05f, 0.08f, -0.05f, 0.08f, -0.05f, 0.1f, 0.05f, -0.08f, 0.05f,
            -0.1f, -0.05f, -0.1f, -0.05f, -0.08f, 0.05f, 0.01f, 0.05f, -0.01f, -0.05f, -0.01f, -0.05f, 0.01f
    };

    float[] score3Data = {
            0.06f, 0.1f, 0.04f, 0.1f, 0.04f, -0.1f, 0.06f, -0.1f, 0.05f, 0.1f, 0.05f, 0.08f, -0.05f, 0.08f,
            -0.05f, 0.1f, 0.05f, -0.08f, 0.05f, -0.1f, -0.05f, -0.1f, -0.05f, -0.08f, 0.05f, 0.01f, 0.05f,
            -0.01f, -0.05f, -0.01f, -0.05f, 0.01f
    };

    public Score(float posX, float posY) {
        this.setScore(this.score);
        this.posX = posX;
        this.posY = posY;
    }

    public void setScore(int score) {
        this.score = score;
        switch (score) {
            case 0:
                this.vertices = score0Data;
                break;
            case 1:
                this.vertices = score1Data;
                break;
            case 2:
                this.vertices = score2Data;
                break;
            case 3:
                this.vertices = score3Data;
                break;
        }
    }

    public int getScore() {
        return this.score;
    }
    @Override
    public void display(GL2 gl) {
        gl.glLoadIdentity();
        gl.glTranslatef(posX, posY, -2f);
        gl.glBegin(GL2.GL_QUADS);
        for (int i = 0; i < vertices.length; i += 2) {
            gl.glVertex2f(vertices[i], vertices[i + 1]);
        }
        gl.glEnd();
    }
}
