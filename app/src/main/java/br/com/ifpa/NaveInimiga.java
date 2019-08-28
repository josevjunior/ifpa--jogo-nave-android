// Target.java
// Subclass of GameElement customized for the Target
package br.com.ifpa;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class NaveInimiga extends ElementoDoJogo {

   // constructor
   public NaveInimiga(ViewDoJogo view, int color, int left, int top, int right, int bottom, float velocityY, Bitmap bitmap) {
      super(view, color, left, top, right, bottom, velocityY, bitmap);
   }

   @Override
   public void update(double interval) {
      shape.offset(0, (int)(velocity * interval));
   }

   public boolean saiuDaTela(){
      return shape.bottom > view.getScreenHeight();
   }

   public boolean colidiuComOutroElemento(ElementoDoJogo elemento) {
      return (Rect.intersects(shape, elemento.shape));
   }

}

