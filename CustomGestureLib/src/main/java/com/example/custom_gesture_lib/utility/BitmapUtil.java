package com.example.custom_gesture_lib.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import androidx.annotation.Nullable;

public class BitmapUtil {
    @Nullable
    public static Bitmap createScaledCircleBitmap(Context context, int radius, int imageId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageId);
        if (bitmap == null) {
            return null;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, radius * 2, radius * 2, false);
        return createCircleImage(bitmap);
    }

    @Nullable
    public static Bitmap createCircleImage(Bitmap source) {
        if (source == null) {
            return null;
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int size = Math.min(source.getWidth(), source.getHeight());
        Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, -(source.getWidth() - size) / 2.0F, -(source.getHeight() - size) / 2.0F, paint);
        return target;
    }
}
