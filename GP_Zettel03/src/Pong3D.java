import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.*;
import java.util.Timer;
import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

public class Pong3D {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
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
        setTitle("PongTex");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        final FPSAnimator ani = new FPSAnimator(canvas, 120, true);
        canvas.addGLEventListener(this);
        game = new Game();
        canvas.addKeyListener(game);
        ani.start();

        getContentPane().setPreferredSize(new Dimension(800, 450));
        getContentPane().add(canvas);
        pack();
        setVisible(true);
        canvas.requestFocus();
    }

    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2(); // get the OpenGL 2 graphics context
        // enable depth test
        gl.glEnable(gl.GL_DEPTH_TEST);

        // setup camera
        float aspect = 16.0f / 9.0f;
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0, aspect, 1.5f, 5f);

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

    // gameobjects
    Player playerOne;
    Score scoreOne;
    Player playerTwo;
    Score scoreTwo;
    Ball ball;
    PowerUp powerUp;
    Court court;

    ArrayList<GameObject> gameObjects = new ArrayList<>();

    public Game() {
        // Instantiate game elements
        ball = new Ball();
        playerOne = new Player(-1.8f, 0f);
        scoreOne = new Score(-0.15f, 0.85f);
        playerTwo = new Player(1.8f, 0f);
        scoreTwo = new Score(0.15f, 0.85f);
        court = new Court();

        // populate gameobject list
        gameObjects.add(court);
        gameObjects.add(ball);
        gameObjects.add(playerOne);
        gameObjects.add(playerTwo);
        gameObjects.add(scoreOne);
        gameObjects.add(scoreTwo);
    }

    public void init(GLAutoDrawable d) {
        // setup textures
        court.texID = Texture.loadTexture(d, "./interstellar.png");
        PowerUp.texIDs[0] = Texture.loadTexture(d, "./powerup_icons_grow.png");
        PowerUp.texIDs[1] = Texture.loadTexture(d, "./powerup_icons_shrink.png");
        PowerUp.texIDs[2] = Texture.loadTexture(d, "./powerup_icons_star.png");
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
        checkCollisionBallPlayer();
        checkCollisionBallBorder();
        checkCollisionBallPowerUp();

        // spawn power up
        if (Texture.random.nextInt(10000) > 9975 && (ball.posY > 0.2f || ball.posY < -0.02f)) {
            spawnPowerUp();
        }
    }

    public void startGame() {
        if (scoreOne.getScore() > 2 || scoreTwo.getScore() > 2) {
            scoreOne.setScore(0);
            scoreTwo.setScore(0);
        }
        ball.velocityX = 0.03f;
        ball.velocityY = 0.015f;
        pauseGame = false;
    }

    public void score(Score score) {
        removePowerUp();

        score.setScore(score.getScore() + 1);
        ball.reset();
        pauseGame = true;
    }

    public void spawnPowerUp() {
        if (!PowerUp.spawned && !PowerUp.taken) {
            powerUp = new PowerUp();
            gameObjects.add(powerUp);
            PowerUp.spawned = true;
        }
    }

    public void removePowerUp() {
        for (int i = 0; i < gameObjects.size(); i++) {
            if (gameObjects.get(i) instanceof PowerUp) {
                gameObjects.remove(i);
                break;
            }
        }
        PowerUp.spawned = false;
    }

    public void checkCollisionBallPlayer() {
        // collision player one
        if (ball.borderLeft < playerOne.borderRight) {
            if (ball.borderDown < playerOne.borderUp && ball.borderUp > playerOne.borderDown) {
                ball.posX = playerOne.borderRight + ball.sizeX;

                // rotate ball
                ball.rotation = playerOne.velocity * 273;
                // reflect ball
                ball.velocityX = -(ball.velocityX + (ball.rotation * .0005f));
                ball.velocityY += (ball.rotation * .0015f);
            }
        }

        // collision player two
        if (ball.borderRight > playerTwo.borderLeft) {
            if (ball.borderDown < playerTwo.borderUp && ball.borderUp > playerTwo.borderDown) {
                ball.posX = playerTwo.borderLeft - ball.sizeX;

                // rotate ball
                ball.rotation = playerTwo.velocity * 273;
                // reflect ball
                ball.velocityX = -ball.velocityX + (ball.rotation * .0005f);
                ball.velocityY += (ball.rotation * .0015f);
            }
        }
    }

    public void checkCollisionBallBorder() {
        // let and right border
        if (ball.posX > 1.9f) {
            score(scoreOne);
        }
        if (ball.posX < -1.9f) {
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

    public void checkCollisionBallPowerUp() {
        if (PowerUp.spawned) {
            if (Math.abs(powerUp.posX - ball.posX) < powerUp.sizeX + ball.sizeX
                    && Math.abs(powerUp.posY - ball.posY) < powerUp.sizeY + ball.sizeY) {
                if (ball.velocityX < 0) {
                    powerUp.applyPowerUp(playerTwo, playerOne);
                } else {
                    powerUp.applyPowerUp(playerOne, playerTwo);
                }
                removePowerUp();
                PowerUp.taken = true;
            }
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
    float[] vertices = Cube.geom;
    float angle;
    float rotation;
    float posX, posY;
    float sizeX, sizeY, sizeZ;

    public void display(GL2 gl) {
        gl.glLoadIdentity();
        gl.glTranslatef(posX, posY, -2f);
        gl.glScalef(this.sizeX, this.sizeY, this.sizeZ);

        // rotate the object
        angle += rotation;
        gl.glRotatef(angle, 0, 0, 1);

        gl.glBegin(GL2.GL_QUADS);
        for (int i = 0; i < vertices.length; i += 3) {
            // check if side changed
            if (i % 12 == 0) {
                int side = i / 12;
                setColor(side, gl);
            }
            gl.glVertex3f(vertices[i], vertices[i + 1], vertices[i + 2]);
        }
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glEnd();
    }

    public void update() {
    }

    public void setColor(int side, GL2 gl) {
        switch (side) {
            case 0:
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                break;
            case 1:
                gl.glColor3f(0.0f, 0.0f, 0.0f);
                break;
            case 2:
            case 3:
                gl.glColor3f(0.3f, 0.3f, 0.3f);
                break;
            case 4:
            case 5:
                gl.glColor3f(0.65f, 0.65f, 0.65f);
                break;
        }
    }
}

class Player extends GameObject {
    boolean moveUp, moveDown = false;
    float ACCELERATION_VALUE = 0.012f;
    float acceleration;
    float velocity;
    float borderLeft, borderRight, borderUp, borderDown;

    public Player(float posX, float posY) {
        this.sizeX = 0.05f;
        this.sizeY = 0.35f;
        this.sizeZ = 0.025f;
        this.posX = posX;
        this.posY = posY;
    }

    public void update() {
        acceleration = 0.0f;
        if (moveUp) {
            acceleration += ACCELERATION_VALUE;
        }
        if (moveDown) {
            acceleration += -ACCELERATION_VALUE;
        }

        velocity += acceleration;
        velocity *= 0.75;
        this.posY += velocity;

        if (this.posY >= 0.8f) {
            this.posY = 0.8f;
        }
        if (this.posY <= -0.8f) {
            this.posY = -0.8f;
        }

        // update collision border
        this.borderLeft = this.posX - this.sizeX;
        this.borderRight = this.posX + this.sizeX;
        this.borderUp = this.posY + this.sizeY;
        this.borderDown = this.posY - this.sizeY;
    }
}

class Ball extends GameObject {
    float velocityX, velocityY;
    float borderLeft, borderRight, borderUp, borderDown;

    public Ball() {
        this.sizeX = 0.05f;
        this.sizeY = 0.05f;
        this.sizeZ = 0.05f;
    }

    public void update() {
        this.posX += velocityX;
        this.posY += velocityY;

        // update collision border
        this.borderLeft = this.posX - this.sizeX;
        this.borderRight = this.posX + this.sizeX;
        this.borderUp = this.posY + this.sizeY;
        this.borderDown = this.posY - this.sizeY;
    }

    public void reset() {
        this.velocityX = 0;
        this.velocityY = 0;
        this.posX = 0;
        this.posY = 0;
        this.angle = 0;
        this.rotation = 0;
    }
}

class PowerUp extends GameObject {
    Timer timer;
    static boolean taken = false;
    static boolean spawned = false;
    float velocity;
    int type;
    static int[] texIDs = new int[3];

    public PowerUp() {
        this.angle = -90f;

        sizeX = 0.1f;
        sizeY = 0.1f;
        sizeZ = 0.1f;

        // set random velocity
        velocity = Texture.random.nextInt(1000) / 1000f * 0.05f;
        // set random type
        type = Texture.random.nextInt(2);
    }

    public void update() {
        if (posY > 1f) {
            posY = 1f;
            velocity = -velocity;
        }
        if (posY < -1f) {
            posY = -1f;
            velocity = -velocity;
        }
        posY += velocity;
    }

    public void applyPowerUp(Player consumer, Player other) {
        switch (type) {
            case 0:
                consumer.sizeY *= 2;
                break;
            case 1:
                other.sizeY /= 2;
                break;
            case 2:
                consumer.ACCELERATION_VALUE *= 2;
                break;
        }
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                removePowerUp(consumer, other);
                PowerUp.taken = false;
                timer.cancel();
            }
        }, 4000);
    }

    public void removePowerUp(Player taker, Player other) {
        switch (type) {
            case 0:
                taker.sizeY /= 2;
                break;
            case 1:
                other.sizeY *= 2;
                break;
            case 2:
                taker.ACCELERATION_VALUE /= 2;
                break;
        }
    }

    public void display(GL2 gl) {
        gl.glLoadIdentity();
        gl.glTranslatef(posX, posY, -2f);
        gl.glScalef(this.sizeX, this.sizeY, this.sizeZ);
        gl.glRotatef(angle, 0, 0, 1);

        // bind texture
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, PowerUp.texIDs[type]);

        gl.glBegin(GL2GL3.GL_QUADS);
        for (int i = 0; i < vertices.length; i += 3) {
            gl.glTexCoord2f(Cube.textureCoordsPowerUp[i], Cube.textureCoordsPowerUp[i + 1]);
            gl.glVertex3f(vertices[i], vertices[i + 1], vertices[i + 2]);
        }
        gl.glEnd();
        gl.glDisable(GL2.GL_TEXTURE_2D);
    }
}

class Court extends GameObject {
    int texID;

    public Court() {
        this.rotation = -0.005f;
    }

    public void update() {
        this.angle += rotation;
    }

    public void display(GL2 gl) {
        gl.glLoadIdentity();
        gl.glTranslatef(posX, posY, -2f);
        gl.glScalef(2f, 2f, 2f);
        gl.glRotatef(angle, 0, 1, 0);

        // bind texture
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texID);

        for (int i = 0; i < vertices.length; i += 3) {
            if (i % 12 == 0) {
                gl.glBegin(GL2GL3.GL_QUADS);
            }
            gl.glTexCoord2f(Cube.textureCoordinate[i / 3 * 2], Cube.textureCoordinate[i / 3 * 2 + 1]);
            gl.glVertex3f(vertices[i], vertices[i + 1], vertices[i + 2]);
            if (i % 12 == 9) {
                gl.glEnd();
            }
        }
        gl.glDisable(GL2.GL_TEXTURE_2D);
    }
}

class Score extends GameObject {
    private int score = 0;

    float[] score0Data = { 0.06f, 0.1f, 0.04f, 0.1f, 0.04f, -0.1f, 0.06f, -0.1f, -0.04f, 0.1f, -0.06f, 0.1f, -0.06f,
            -0.1f, -0.04f, -0.1f, 0.05f, 0.1f, 0.05f, 0.08f, -0.05f, 0.08f, -0.05f, 0.1f, 0.05f, -0.08f, 0.05f, -0.1f,
            -0.05f, -0.1f, -0.05f, -0.08f };

    float[] score1Data = { 0.01f, 0.1f, -0.01f, 0.1f, -0.01f, -0.1f, 0.01f, -0.1f };

    float[] score2Data = { 0.06f, 0.1f, 0.04f, 0.1f, 0.04f, 0.0f, 0.06f, 0.0f, -0.04f, 0.0f, -0.06f, 0.0f, -0.06f,
            -0.1f, -0.04f, -0.1f, 0.05f, 0.1f, 0.05f, 0.08f, -0.05f, 0.08f, -0.05f, 0.1f, 0.05f, -0.08f, 0.05f, -0.1f,
            -0.05f, -0.1f, -0.05f, -0.08f, 0.05f, 0.01f, 0.05f, -0.01f, -0.05f, -0.01f, -0.05f, 0.01f };

    float[] score3Data = { 0.06f, 0.1f, 0.04f, 0.1f, 0.04f, -0.1f, 0.06f, -0.1f, 0.05f, 0.1f, 0.05f, 0.08f, -0.05f,
            0.08f, -0.05f, 0.1f, 0.05f, -0.08f, 0.05f, -0.1f, -0.05f, -0.1f, -0.05f, -0.08f, 0.05f, 0.01f, 0.05f,
            -0.01f, -0.05f, -0.01f, -0.05f, 0.01f };

    public Score(float posX, float posY) {
        this.setScore(this.score);
        this.posX = posX;
        this.posY = posY;
    }

    public void setScore(int score) {
        if (score > 3) {
            return;
        }
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

class Cube {
    static float[] geom = {
            // front
            1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f,

            // back
            1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,

            // top
            1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f,

            // bottom
            1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,

            // left
            -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,

            // right
            1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f };

    static float[] textureCoordinate = {

            // front
            0.75f, 0.5f, 1f, 0.5f, 1f, 0.25f, 0.75f, 0.25f,

            // back
            0.5f, 0.5f, 0.25f, 0.5f, 0.25f, 0.25f, 0.5f, 0.25f,

            // top
            0.5f, 0.75f, 0.25f, 0.75f, 0.25f, 0.5f, 0.5f, 0.5f,

            // bottom
            0.5f, 0f, 0.25f, 0f, 0.25f, 0.25f, 0.5f, 0.25f,

            // left
            0f, 0.5f, 0f, 0.25f, 0.25f, 0.25f, 0.25f, 0.5f,

            // right
            0.75f, 0.5f, 0.75f, 0.25f, 0.5f, 0.25f, 0.5f, 0.5f };

    static float[] textureCoordsPowerUp = { 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f,

            0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f,

            0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f,

            0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f,

            0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f,

            0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f };
}
