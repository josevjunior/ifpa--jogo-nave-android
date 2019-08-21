package br.com.ifpa;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import br.com.ifpa.Nave.DirecaoDaNave;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ViewDoJogo extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "ViewDoJogo"; // for logging errors

    public static final double NAVE_WIDTH_PERCENT = 3.0 / 40;
    public static final double NAVE_LENGTH_PERCENT = 1.0 / 10;

    // text size 1/18 of screen width
    public static final double TEXT_SIZE_PERCENT = 1.0 / 30;

    private ThreadDoJogo threadDoJogo; // controls the game loop
    private Activity activity; // to display Game Over dialog in GUI thread
    private boolean dialogIsDisplayed = false;

    //Elementos do jogo
    private Nave nave;
    private List<NaveInimiga> navesInimigas;

    private Random random;

    private Handler handler;

    // dimension variables
    private int screenWidth;
    private int screenHeight;

    // variables for the game loop and tracking statistics
    private boolean gameOver; // is the game over?
    private int pontos = 0;

    // Paint variables used when drawing each item on the screen
    private Paint textPaint; // Paint used to draw text
    private Paint backgroundPaint; // Paint used to clear the drawing area

    // constructor
    public ViewDoJogo(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        activity = (Activity) context; // store reference to MainActivity

        getHolder().addCallback(this);

        random = new Random();

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        navesInimigas = new ArrayList<>();

        textPaint = new Paint();

        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
        textPaint.setAntiAlias(true); // smoothes the text

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void newGame() {

        pontos = 0;

        navesInimigas.clear();

        handler = new Handler();
        handler.postDelayed(criarNaveInimiga, 1000);

        int naveWidth = (int) (NAVE_WIDTH_PERCENT * screenWidth);
        int naveHeight = (int) (NAVE_LENGTH_PERCENT * screenHeight);
        nave = new Nave(this,
                (screenWidth / 2) - naveWidth,
                screenHeight - naveHeight,
                (screenWidth / 2) + naveWidth,
                screenHeight,
                200
        );

        if (gameOver) { // start a new game after the last game ended
            gameOver = false; // the game is not over
            threadDoJogo = new ThreadDoJogo(getHolder()); // create thread
            threadDoJogo.start(); // start the game loop thread
        }

        hideSystemBars();
    }

    Runnable criarNaveInimiga = new Runnable() {
        @Override
        public void run() {
            int positionInScreen = random.nextInt(screenWidth);
            navesInimigas.add(new NaveInimiga(
                    ViewDoJogo.this,
                    R.color.blue,
                    positionInScreen,
                    0,
                    positionInScreen + 20,
                    40,
                    gerarNumeroAleatorio(2, 4) * 100
            ));

            handler.postDelayed(this, 500);

        }
    };

    private int gerarNumeroAleatorio(int min, int max) {

        return random.nextInt((max - min) + 1) + min;
    }

    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0; // convert to seconds

        nave.update(interval);

        for (int i = 0; i < navesInimigas.size(); i++) {
            NaveInimiga naveInimiga = navesInimigas.get(i);
            naveInimiga.update(interval);
        }

    }

    // display an AlertDialog when the game ends
    private void showGameOverDialog() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setCancelable(false)
                        .setTitle("Game Over")
                        .setMessage("Você Perdeu!")
                        .setPositiveButton("Tentar Novamente", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                handler.removeCallbacks(criarNaveInimiga);

                                newGame();
                            }
                        });
                gameOver = true;
                builder.create().show();
            }
        });

    }

    // draws the game to the given Canvas
    public void drawGameElements(Canvas canvas) {

        // clear the background
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
                backgroundPaint);

        canvas.drawText("Pontos: " + pontos, 50, 100, textPaint);

        nave.draw(canvas);

        for (int i = 0; i < navesInimigas.size(); i++) {
            NaveInimiga naveInimiga = navesInimigas.get(i);
            naveInimiga.draw(canvas);
        }
    }

    // checks if the ball collides with the Blocker or any of the Targets
    // and handles the collisions
    public void verificarColisoesEPontuacao() {
        // remove any of the targets that the Cannonball
        // collides with
        for (int i = 0; i < navesInimigas.size(); i++) {
            NaveInimiga naveInimiga = navesInimigas.get(i);
            if (naveInimiga.colidiuComOutroElemento(nave)) {
                gameOver = false;
                threadDoJogo.setRunning(false);
                showGameOverDialog();
                break;
            } else if (naveInimiga.saiuDaTela()) {
                navesInimigas.remove(naveInimiga);
                pontos++;
                i--;
            }
        }

    }

    // stops the game: called by CannonGameFragment's onPause method
    public void stopGame() {
        if (threadDoJogo != null)
            threadDoJogo.setRunning(false); // tell thread to terminate
        handler.removeCallbacks(criarNaveInimiga);
    }

    // called when surface changes size
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    // called when surface is first created
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame(); // set up and start a new game
            threadDoJogo = new ThreadDoJogo(holder); // create thread
            threadDoJogo.setRunning(true); // start game running
            threadDoJogo.start(); // start the game loop thread
        }
    }

    // called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ensure that thread terminates properly
        boolean retry = true;
        threadDoJogo.setRunning(false); // terminate threadDoJogo

        while (retry) {
            try {
                threadDoJogo.join(); // wait for threadDoJogo to finish
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    // called when the user touches the screen in this activity
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // get int representing the type of action which caused this event
        int action = e.getAction();

        // the user touched the screen or dragged along the screen
        if (action == MotionEvent.ACTION_DOWN) {
            //Descobre a direção do toque
            int divisaoDaTela = screenWidth / 2;
            DirecaoDaNave direcao = e.getX() > divisaoDaTela ? DirecaoDaNave.DIREITA : DirecaoDaNave.ESQUERDA;
            nave.setMovendo(true);
            nave.setDirecaoDaNave(direcao);
        } else if (action == MotionEvent.ACTION_UP) {
            nave.setMovendo(false);
        }

        return true;
    }

    // Thread subclass to control the game loop
    private class ThreadDoJogo extends Thread {
        private SurfaceHolder surfaceHolder; // for manipulating canvas
        private boolean threadIsRunning = true; // running by default

        // initializes the surface holder
        public ThreadDoJogo(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("ThreadDoJogo");
        }

        // changes running state
        public void setRunning(boolean running) {
            threadIsRunning = running;
        }

        // controls the game loop
        @Override
        public void run() {
            Canvas canvas = null; // used for drawing
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    // get Canvas for exclusive drawing from this thread
                    canvas = surfaceHolder.lockCanvas(null);

                    // lock the surfaceHolder for drawing
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        updatePositions(elapsedTimeMS); // update game state
                        verificarColisoesEPontuacao(); // test for GameElement collisions
                        drawGameElements(canvas); // draw using the canvas
                        previousFrameTime = currentTime; // update previous time
                    }
                } finally {
                    // display canvas's contents on the CannonView
                    // and enable other threads to use the Canvas
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    // hide system bars and app bar
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // show system bars and app bar
    private void showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}
