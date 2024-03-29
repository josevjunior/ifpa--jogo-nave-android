
package br.com.ifpa;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class ElementoDoJogo {
   protected ViewDoJogo view; // the view that contains this GameElement
   protected Paint paint = new Paint(); // Paint to draw this GameElement
   protected Rect shape; // the GameElement's rectangular bounds
   protected float velocity; // the vertical velocity of this GameElement
   protected Bitmap bitmap;

   public ElementoDoJogo(ViewDoJogo view, int color, int left, int top, int right, int bottom, float velocityY, Bitmap bitmap) {
      this.view = view;
      paint.setColor(color);
      this.bitmap = bitmap;
      shape = new Rect(left, top, right, bottom); // set bounds
      this.velocity = velocityY;
   }

   public void update(double interval) {

      shape.offset((int) (velocity * interval), 0);

      if (shape.top < 0 && velocity < 0 ||
         shape.bottom > view.getScreenHeight() && velocity > 0)
         velocity *= -1; // reverse this GameElement's velocity
   }

   public void draw(Canvas canvas) {
      canvas.drawRect(shape, paint);
      canvas.drawBitmap(bitmap, null, shape, paint);
   }

}
