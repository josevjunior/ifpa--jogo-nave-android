package br.com.ifpa;

import android.graphics.Canvas;

public class Nave extends ElementoDoJogo {

    public enum DirecaoDaNave {
        DIREITA, ESQUERDA;
    }

    private boolean movendo;
    private DirecaoDaNave direcaoDaNave;

    public Nave(ViewDoJogo view, int left, int top, int right, int bottom, float velocityY) {
        super(view, R.color.blue, left, top, right, bottom, velocityY);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(shape, paint);
    }

    public void setDirecaoDaNave(DirecaoDaNave direcaoDaNave) {
        this.direcaoDaNave = direcaoDaNave;
        if ((direcaoDaNave == DirecaoDaNave.DIREITA && velocity < 0)
                || (direcaoDaNave == DirecaoDaNave.ESQUERDA && velocity > 0)) {
            velocity *= -1;
        }
    }

    @Override
    public void update(double interval) {
        if (movendo) {
            int movimento = (int) (velocity * interval);

            if (((direcaoDaNave == DirecaoDaNave.ESQUERDA)
                && (shape.left + movimento >= 0))
                || ((direcaoDaNave == DirecaoDaNave.DIREITA)
                && (shape.left + movimento <= view.getScreenWidth() - shape.width()))) {
                shape.offset(movimento, 0);
            }
        }
    }

    public void setMovendo ( boolean movendo){
        this.movendo = movendo;
    }
}
