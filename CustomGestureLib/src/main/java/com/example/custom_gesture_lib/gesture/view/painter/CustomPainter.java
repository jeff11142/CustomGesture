package com.example.custom_gesture_lib.gesture.view.painter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import androidx.annotation.ColorInt;

import com.example.custom_gesture_lib.gesture.view.GestureLockView;
import com.example.custom_gesture_lib.gesture.view.model.GesturePoint;
import com.example.custom_gesture_lib.utility.BitmapUtil;


import java.util.List;

public abstract class CustomPainter {

    public static final int NORMAL_COLOR = Color.GRAY;
    public static final int PRESSED_COLOR = Color.BLACK;
    public static final int ERROR_COLOR = Color.RED;
    public static final int RIGHT_COLOR = Color.GREEN;
    public final Paint normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final Paint pressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final Paint errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final Paint rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap normalBitmap;
    private Bitmap pressedBitmap;
    private Bitmap errorBitmap;

    private GestureLockView mGestureLockView;

    public void attach(GestureLockView gestureLockView, Context context,
                       int normalColor, int pressColor, int errorColor, int rightColor,
                       int normalImageId, int pressImageId, int errorImageId) {
        // 1.关联手势解锁视图
        mGestureLockView = gestureLockView;
        // 2.设置Painter画笔颜色
        setNormalColor(normalColor);
        setPressColor(pressColor);
        setErrorColor(errorColor);
        setRightColor(rightColor);
        // 3.设置Painter图片引用
        setNormalBitmap(context, mGestureLockView.getRadius(), normalImageId);
        setPressBitmap(context, mGestureLockView.getRadius(), pressImageId);
        setErrorBitmap(context, mGestureLockView.getRadius(), errorImageId);
    }

    /**
     * 設置正常狀態畫筆顏色
     *
     * @param normalColor 正常狀態畫筆顏色 (具體顏色值,不是引用值)
     */
    public void setNormalColor(@ColorInt int normalColor) {
        normalPaint.setColor(normalColor);
    }

    /**
     * 設置按下狀態畫筆顏色
     *
     * @param pressColor 按下狀態畫筆顏色 (具體顏色值,不是引用值)
     */
    public void setPressColor(@ColorInt int pressColor) {
        pressedPaint.setColor(pressColor);
    }

    /**
     * 設置出錯狀態畫筆顏色
     *
     * @param errorColor 出錯狀態畫筆顏色 (具體顏色值,不是引用值)
     */
    public void setErrorColor(@ColorInt int errorColor) {
        errorPaint.setColor(errorColor);
    }

    /**
     * 設置正確狀態畫筆的顏色  (具體顏色值,不是引用值)
     */
    public void setRightColor(@ColorInt int rightColor){
        rightPaint.setColor(rightColor);
    }

    /**
     * 設置正常狀態圖片
     */
    public void setNormalBitmap(Context context, int radius, int normalImageId) {
        if (normalImageId != 0) {
            normalBitmap = BitmapUtil.createScaledCircleBitmap(context, radius, normalImageId);
        } else {
            normalBitmap = null;
        }
    }

    /**
     * 設置按下狀態圖片
     */
    public void setPressBitmap(Context context, int radius, int pressImageId) {
        if (pressImageId != 0) {
            pressedBitmap = BitmapUtil.createScaledCircleBitmap(context, radius, pressImageId);
        } else {
            pressedBitmap = null;
        }
    }

    /**
     * 設置出錯狀態圖片
     */
    public void setErrorBitmap(Context context, int radius, int errorImageId) {
        if (errorImageId != 0) {
            errorBitmap = BitmapUtil.createScaledCircleBitmap(context, radius, errorImageId);
        } else {
            errorBitmap = null;
        }
    }

    /**
     * 獲取手勢解鎖視圖
     */
    public GestureLockView getGestureLockView(){
        return mGestureLockView;
    }

    /**
     * 繪製輔助線
     *
     * @param viewSize 視圖邊長
     * @param canvas   畫布
     */
    public final void drawGuidesLine(int viewSize, Canvas canvas) {
        canvas.drawLine(0, 0, viewSize, 0, normalPaint);
        canvas.drawLine(0, viewSize / 3, viewSize, viewSize / 3, normalPaint);
        canvas.drawLine(0, 2 * viewSize / 3, viewSize, 2 * viewSize / 3, normalPaint);
        canvas.drawLine(0, viewSize - 1, viewSize - 1, viewSize - 1, normalPaint);
        canvas.drawLine(0, 0, 0, viewSize, normalPaint);
        canvas.drawLine(viewSize / 3, 0, viewSize / 3, viewSize, normalPaint);
        canvas.drawLine(2 * viewSize / 3, 0, 2 * viewSize / 3, viewSize, normalPaint);
        canvas.drawLine(viewSize - 1, 0, viewSize - 1, viewSize - 1, normalPaint);
    }


    /**
     * 3x3點繪製方法
     *
     * @param points 3x3點數組
     * @param canvas 畫布
     */
    public final void drawPoints(GesturePoint[][] points, Canvas canvas) {
        for (int i = 0; i < 3; i++) { // i為"行標"
            for (int j = 0; j < 3; j++) { // j為"列標"
                GesturePoint point = points[i][j];
                switch (point.status) {
                    case GesturePoint.POINT_NORMAL_STATUS: // 正常狀態的點
                        if (normalBitmap != null) {
                            drawBitmap(point, point.radius, normalBitmap, canvas, normalPaint);
                        } else {
                            drawNormalPoint(point, canvas, normalPaint);
                        }
                        break;
                    case GesturePoint.POINT_PRESS_STATUS: // 按下狀態的點
                        if (pressedBitmap != null) {
                            drawBitmap(point, point.radius, pressedBitmap, canvas, pressedPaint);
                        } else {
                            drawPressPoint(point, canvas, pressedPaint);
                        }
                        break;
                    case GesturePoint.POINT_ERROR_STATUS: // 出錯狀態的點
                        if (errorBitmap != null) {
                            drawBitmap(point, point.radius, errorBitmap, canvas, errorPaint);
                        } else {
                            drawErrorPoint(point, canvas, errorPaint);
                        }
                        break;
                    case GesturePoint.POINT_RIGHT_STATUS: // 正確狀態的點
                        if (errorBitmap != null) {
                            drawBitmap(point, point.radius, errorBitmap, canvas, rightPaint);
                        } else {
                            drawRightPoint(point, canvas, rightPaint);
                        }
                        break;
                }
            }
        }
    }

    /**
     * 繪製圖片
     *
     * @param point  單位點
     * @param radius 半徑
     * @param bitmap 圖片
     * @param canvas 畫布
     * @param paint  畫筆
     */
    private void drawBitmap(GesturePoint point, int radius, Bitmap bitmap, Canvas canvas, Paint paint) {
        // 判斷mGestureLockView開啟了動畫
        if (mGestureLockView.isUseAnim()) {
            RectF rectF = new RectF(point.x - mGestureLockView.getRadius(), point.y - mGestureLockView.getRadius(), point.x + mGestureLockView.getRadius(), point.y + mGestureLockView.getRadius());
            int index = canvas.saveLayer(rectF, paint, Canvas.ALL_SAVE_FLAG);
            canvas.drawCircle(point.x, point.y, radius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, point.x - mGestureLockView.getRadius(), point.y - mGestureLockView.getRadius(), paint);
            paint.setXfermode(null);
            canvas.restoreToCount(index);
        } else {
            canvas.drawBitmap(bitmap, point.x - radius, point.y - radius, null);
        }
//        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, radius * 2, radius * 2, false), point.x - radius, point.y - radius, null);
    }

    /**
     * 繪製正常狀態的點
     *
     * @param point       單位點
     * @param canvas      畫布
     * @param normalPaint 正常狀態畫筆
     */
    public abstract void drawNormalPoint(GesturePoint point, Canvas canvas, Paint normalPaint);

    /**
     * 繪製按下狀態的點
     *
     * @param point      單位點
     * @param canvas     畫布
     * @param pressPaint 按下狀態畫筆
     */
    public abstract void drawPressPoint(GesturePoint point, Canvas canvas, Paint pressPaint);

    /**
     * 繪製出錯狀態的點
     *
     * @param point      單位點
     * @param canvas     畫布
     * @param errorPaint 錯誤狀態畫筆
     */
    public abstract void drawErrorPoint(GesturePoint point, Canvas canvas, Paint errorPaint);


    /**
     * 繪製出正確狀態的點
     *
     * @param point      單位點
     * @param canvas     畫布
     * @param rightPaint 正確狀態畫筆
     */
    public abstract void drawRightPoint(GesturePoint point, Canvas canvas, Paint rightPaint);

    /** ********************************** 連線的繪製方法（↓） **************************************/

    /**
     * 繪製連線
     *
     * @param points   點集合（已被按下的點）
     * @param eventX   事件X坐標（當前觸摸位置）
     * @param eventY   事件Y坐標（當前觸摸位置）
     * @param lineSize 線的粗細值
     * @param canvas   畫布
     */
    public void drawLines(List<GesturePoint> points, float eventX, float eventY, int lineSize, Canvas canvas) {
        // 1.參數合法性判斷
        if (points.size() <= 0) {
            return;
        }
        // 2.根據點列表生成連線路徑
        Path path = generateLinePath(points, eventX, eventY);
        // 3.區分點的狀態，使用不同畫筆繪製連線
        switch (points.get(0).status) {
            case GesturePoint.POINT_PRESS_STATUS: // 按下狀態
                Paint pressPaint = new Paint(this.pressedPaint);
                pressPaint.setStyle(Paint.Style.STROKE);
                pressPaint.setStrokeWidth(lineSize);
                canvas.drawPath(path, pressPaint);
                break;
            case GesturePoint.POINT_ERROR_STATUS: // 出錯狀態
                Paint errorPaint = new Paint(this.errorPaint);
                errorPaint.setStyle(Paint.Style.STROKE);
                errorPaint.setStrokeWidth(lineSize);
                canvas.drawPath(path, errorPaint);
                break;
            case GesturePoint.POINT_RIGHT_STATUS: // 正確狀態
                Paint rightPaint = new Paint(this.rightPaint);
                rightPaint.setStyle(Paint.Style.STROKE);
                rightPaint.setStrokeWidth(lineSize);
                canvas.drawPath(path, rightPaint);
                break;
        }
    }

    /**
     * 生成連線路徑
     *
     * @param points 點集合（已被按下記錄的點）
     * @param eventX 事件X坐標（當前觸摸位置）
     * @param eventY 事件Y坐標（當前觸摸位置）
     */
    private Path generateLinePath(List<GesturePoint> points, float eventX, float eventY) {
        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            GesturePoint point = points.get(i);
            if (i == 0) {
                path.moveTo(point.x, point.y);
            } else {
                path.lineTo(point.x, point.y);
            }
        }
        path.lineTo(eventX, eventY);
        return path;
    }
}
