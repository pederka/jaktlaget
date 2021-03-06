package net.ddns.peder.jaktlaget.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.core.graphics.drawable.DrawableCompat;

public class BitmapUtil {
    static public BitmapDescriptor createPureTextIcon(String text, int color) {

        Paint textPaint = new Paint(); // Adapt to your needs

        textPaint.setTextSize(45);
        textPaint.setColor(color);

        float textWidth = textPaint.measureText(text);
        float textHeight = textPaint.getTextSize();
        int width = (int) (textWidth);
        int height = (int) (textHeight*1.2);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.LTGRAY);

        canvas.translate(0, height*0.8f);
        canvas.drawText(text, 0, 0, textPaint);
        return BitmapDescriptorFactory.fromBitmap(image);
    }

    static public Bitmap getBitmapFromVectorDrawable(int resId, int color, Context context) {
        //noinspection RestrictedApi
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context,
                            resId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        drawable.draw(canvas);
        return bitmap;
    }
}
