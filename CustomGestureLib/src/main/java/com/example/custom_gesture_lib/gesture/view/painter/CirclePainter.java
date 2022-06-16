package com.example.custom_gesture_lib.gesture.view.painter;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.custom_gesture_lib.gesture.view.model.GesturePoint;


/**
 * @ClassName: AliPayPainter
 * @Description: (仿)支付宝绘制者
 * @Author: wangnan7
 * @Date: 2017/9/21
 */

public class CirclePainter extends CustomPainter {

    /**
     * 繪製正常狀態的點
     *
     * @param point       單位點
     * @param canvas      畫布
     * @param normalPaint 正常狀態畫筆
     */
    @Override
    public void drawNormalPoint(GesturePoint point, Canvas canvas, Paint normalPaint) {
        canvas.drawCircle(point.x, point.y, point.radius / 3.0F, normalPaint);
    }

    /**
     * 繪製按下狀態的點
     *
     * @param point      單位點
     * @param canvas     畫布
     * @param pressPaint 按下狀態畫筆
     */
    @Override
    public void drawPressPoint(GesturePoint point, Canvas canvas, Paint pressPaint) {
        // 1.改變透明度繪製外層實心圓
        pressPaint.setAlpha(32);
        canvas.drawCircle(point.x, point.y, point.radius, pressPaint);
        // 2.還原透明度繪製內存實心圓
        pressPaint.setAlpha(255);
        canvas.drawCircle(point.x, point.y, point.radius / 3.0F, pressPaint);
    }

    /**
     * 繪製出錯狀態的點
     *
     * @param point      單位點
     * @param canvas     畫布
     * @param errorPaint 錯誤狀態畫筆
     */
    @Override
    public void drawErrorPoint(GesturePoint point, Canvas canvas, Paint errorPaint) {
        // 1.改變透明度繪製外層實心圓
        errorPaint.setAlpha(32);
        canvas.drawCircle(point.x, point.y, point.radius, errorPaint);
        // 2.還原透明度繪製內存實心圓
        errorPaint.setAlpha(255);
        canvas.drawCircle(point.x, point.y, point.radius / 3.0F, errorPaint);
    }

    @Override
    public void drawRightPoint(GesturePoint point, Canvas canvas, Paint errorPaint) {

    }
}
