package com.example.custom_gesture_lib.gesture.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.example.custom_gesture_lib.R;
import com.example.custom_gesture_lib.gesture.view.listener.OnGestureLockListener;
import com.example.custom_gesture_lib.gesture.view.model.GesturePoint;
import com.example.custom_gesture_lib.gesture.view.painter.CustomPainter;
import com.example.custom_gesture_lib.gesture.view.painter.GestureLockCustomPainter;
import com.example.custom_gesture_lib.utility.DimensionUtility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class GestureLockView extends View {
    @IntDef({NORMAL, REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleMode {
    }

    public static final int sideLength = 3;
    public static final int NORMAL = 0x0000; // 正常缩放
    public static final int REVERSE = 0x0001; // 反转缩放
    private final GesturePoint[][] points = new GesturePoint[sideLength][sideLength];  // 預設 3*3
    private final List<GesturePoint> pressPoints = new ArrayList<>(sideLength * sideLength);  // 紀錄已被選取的點 (基於 3*3)
    private final List<ValueAnimator> pointAnimators = new ArrayList<>(sideLength * sideLength);  // 紀錄已被選取的點的動畫
    private int viewSize;
    private int radius;  // 半徑
    private float radiusRatio;  // 半徑比例 (預設 0.6F)
    private int lineThickness;  // 連接線的粗細 (預設為 1dp)
    private boolean isShowGuides;  // 是否顯示輔助線
    private boolean isUseAnimation;  // 是否使用動畫
    private long animationDuration;  // 動畫長度 (預設 200毫秒)
    private int animationScaleMode;  // 動畫縮放模式
    private float animationScaleRate;  // 動畫縮放比例 (預設 1.5F)
    private boolean isUseVibrate;  // 是否開啟震動
    private long vibrateDuration;  // 震動時間 (預設 40毫秒)
    private boolean isLineTop;  // 繪製時線條是否在點的上方
    private int normalColor;
    private int pressedColor;
    private int errorColor;
    private int rightColor;
    private int normalImageId;
    private int pressedImageId;
    private int errorImageId;
    private float eventX;
    private float eventY;
    private OnGestureLockListener mGestureLockListener;
    private CustomPainter customPainter = new GestureLockCustomPainter();
    private Vibrator vibrator;
    private boolean isErrorStatus;  //是否錯誤

    public GestureLockView(Context context) {
        this(context, null);
    }

    public GestureLockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GestureLockView);
        radiusRatio = array.getFloat(R.styleable.GestureLockView_radius_ratio, 0.6F);
        lineThickness = array.getDimensionPixelSize(R.styleable.GestureLockView_line_thickness, DimensionUtility.dp2px(context, 1));
        normalColor = array.getColor(R.styleable.GestureLockView_normal_color, CustomPainter.NORMAL_COLOR);
        pressedColor = array.getColor(R.styleable.GestureLockView_pressed_color, CustomPainter.PRESSED_COLOR);
        errorColor = array.getColor(R.styleable.GestureLockView_error_color, CustomPainter.ERROR_COLOR);
        rightColor = array.getColor(R.styleable.GestureLockView_right_color, CustomPainter.RIGHT_COLOR);
        isShowGuides = array.getBoolean(R.styleable.GestureLockView_is_show_guides, false);
        isLineTop = array.getBoolean(R.styleable.GestureLockView_is_line_top, false);
        isUseAnimation = array.getBoolean(R.styleable.GestureLockView_is_use_animation, false);
        animationDuration = array.getInt(R.styleable.GestureLockView_animation_duration, 200);
        animationScaleMode = array.getInt(R.styleable.GestureLockView_animation_scale_mode, NORMAL);
        animationScaleRate = array.getFloat(R.styleable.GestureLockView_animation_scale_rate, 1.5F);
        isUseVibrate = array.getBoolean(R.styleable.GestureLockView_is_use_vibrate, false);
        vibrateDuration = array.getInt(R.styleable.GestureLockView_vibrate_duration, 40);
        normalImageId = array.getResourceId(R.styleable.GestureLockView_normal_image, 0);
        pressedImageId = array.getResourceId(R.styleable.GestureLockView_pressed_image, 0);
        errorImageId = array.getResourceId(R.styleable.GestureLockView_error_image, 0);
        array.recycle();

        radiusRatio = (radiusRatio < 0) ? 0 : radiusRatio > 1 ? 1 : radiusRatio;
        animationScaleRate = animationScaleRate < 0 ? 0 : animationScaleRate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //寬高取最小值以維持正方形
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        viewSize = Math.min(width, height);

        setMeasuredDimension(viewSize, viewSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        initParams();
        initPointArray();
        initPainter();
    }

    private void initParams() {
        radius = (int) (viewSize / 6 * radiusRatio);
    }

    private void initPointArray() {
        // 基於 3*3
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                GesturePoint point = new GesturePoint();
                point.x = (2 * j + 1) * viewSize / 6;
                point.y = (2 * i + 1) * viewSize / 6;
                point.radius = radius;
                point.status = GesturePoint.POINT_NORMAL_STATUS;
                point.index = i * sideLength + j;
                points[i][j] = point;
            }
        }
    }

    private void initPainter() {
        customPainter.attach(this, getContext(),
                normalColor, pressedColor, errorColor, rightColor,
                normalImageId, pressedImageId, errorImageId);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isShowGuides) {
            customPainter.drawGuidesLine(viewSize, canvas);
        }
        if (isLineTop) {
            customPainter.drawPoints(points, canvas);
            customPainter.drawLines(pressPoints, eventX, eventY, lineThickness, canvas);
        } else {
            customPainter.drawLines(pressPoints, eventX, eventY, lineThickness, canvas);
            customPainter.drawPoints(points, canvas);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        eventX = event.getX();
        eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downEventDeal(eventX, eventY);
                break;
            case MotionEvent.ACTION_MOVE:
                moveEventDeal(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                upEventDeal();
                break;
        }
        postInvalidate();
        return true;
    }

    private void downEventDeal(float eventX, float eventY) {
        if (mGestureLockListener != null) {
            mGestureLockListener.onStarted();
        }
        clear();
        modifyPointStatus(eventX, eventY);
    }

    private void moveEventDeal(float eventX, float eventY) {
        modifyPointStatus(eventX, eventY);
    }

    private void upEventDeal() {
        if (mGestureLockListener != null) {
            mGestureLockListener.onComplete(getPassword());
        }

        if (!pressPoints.isEmpty()) {
            eventX = pressPoints.get(pressPoints.size() - 1).x;
            eventY = pressPoints.get(pressPoints.size() - 1).y;
        }

        if (!pointAnimators.isEmpty()) {
            for (ValueAnimator animator : pointAnimators) {
                animator.end();
            }
            pointAnimators.clear();
        }

        postInvalidate();
    }

    private void modifyPointStatus(float x, float y) {
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                GesturePoint point = points[i][j];
                float dx = Math.abs(x - point.x);
                float dy = Math.abs(y - point.y);
                if (Math.sqrt(dx * dx + dy * dy) < radius) {
                    point.status = GesturePoint.POINT_PRESS_STATUS;
                    addPressedPoint(point);
                    return;
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void addPressedPoint(GesturePoint point) {
        if (pressPoints.contains(point)) {
            return;
        }

        if (!pressPoints.isEmpty()) {
            addMiddlePoint(point);
        }

        pressPoints.add(point);

        if (isUseAnimation) {
            startAnimation(point, animationDuration);
        }

        if (isUseVibrate) {
            if (vibrator == null) {
                vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            }
            vibrator.vibrate(vibrateDuration);
        }

        if (mGestureLockListener != null) {
            mGestureLockListener.onProgress(getPassword());
        }
    }

    private void addMiddlePoint(GesturePoint point) {
        GesturePoint lastPoint = pressPoints.get(pressPoints.size() - 1);
        if (lastPoint == point) {
            return;
        }
        int middleX = (lastPoint.x + point.x) / 2;
        int middleY = (lastPoint.y + point.y) / 2;
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                GesturePoint tempPoint = points[i][j];
                int dx = Math.abs(tempPoint.x - middleX);
                int dy = Math.abs(tempPoint.y - middleY);
                if (Math.sqrt(dx * dx + dy * dy) < radius) {
                    tempPoint.status = GesturePoint.POINT_PRESS_STATUS;
                    addPressedPoint(tempPoint);
                    return;
                }
            }
        }
    }

    private void startAnimation(final GesturePoint point, long duration) {
        ValueAnimator valueAnimator;
        if (pressedImageId == 0) {
            if (animationScaleMode == 1) {
                valueAnimator = ValueAnimator.ofInt(point.radius, (int) (animationScaleRate * point.radius), point.radius);
            } else {
                valueAnimator = ValueAnimator.ofInt((int) (animationScaleRate * point.radius), point.radius);
            }
        } else {
            valueAnimator = ValueAnimator.ofInt(0, point.radius);
        }
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> {
            point.radius = (int) animation.getAnimatedValue();
            postInvalidate();
        });
        valueAnimator.start();

        pointAnimators.add(valueAnimator);
    }

    private void clear() {
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                points[i][j].status = GesturePoint.POINT_NORMAL_STATUS;
                points[i][j].radius = radius;
            }
        }
        pressPoints.clear();
        pointAnimators.clear();
        isErrorStatus = false;
    }

    private String getPassword() {
        StringBuilder builder = new StringBuilder();
        for (GesturePoint pressPoint : pressPoints) {
            builder.append(pressPoint.index);
        }
        return builder.toString();
    }


    public int getRadius() {
        return radius;
    }

    /**
     * 設置半徑比例
     *
     * @param radiusRatio 半徑比例(0F ~ 1F)
     */
    public void setRadiusRatio(float radiusRatio) {
        this.radiusRatio = (radiusRatio < 0) ? 0 : radiusRatio > 1 ? 1 : radiusRatio;
        onSizeChanged(0, 0, 0, 0);
        postInvalidate();
    }

    /**
     * 設置線的粗細
     *
     * @param px 粗細
     */
    public void setLineThickness(int px) {
        this.lineThickness = px;
        postInvalidate();
    }

    /**
     * 設置正常狀態的顏色
     */
    public void setNormalColor(@ColorInt int normalColor) {
        this.normalColor = normalColor;
        customPainter.setNormalColor(normalColor);
        postInvalidate();
    }

    /**
     * 設置按下狀態的顏色
     */
    public void setPressedColor(@ColorInt int pressedColor) {
        this.pressedColor = pressedColor;
        customPainter.setPressColor(pressedColor);
        postInvalidate();
    }

    /**
     * 設置錯誤狀態的顏色
     */
    public void setErrorColor(@ColorInt int errorColor) {
        this.errorColor = errorColor;
        customPainter.setErrorColor(errorColor);
        postInvalidate();
    }

    /**
     * 設置正確狀態的顏色
     */
    public void setRightColor(@ColorInt int rightColor) {
        this.rightColor = rightColor;
        customPainter.setRightColor(rightColor);
        postInvalidate();
    }

    /**
     * 是否顯示輔助線
     *
     * @param isShowGuides 是否显示辅助线（true显示,false隐藏）
     */
    public void setShowGuides(boolean isShowGuides) {
        this.isShowGuides = isShowGuides;
        postInvalidate();
    }

    /**
     * 繪製時線是否在點上方
     */
    public void setLineTop(boolean isLineTop) {
        this.isLineTop = isLineTop;
        postInvalidate();
    }

    /**
     * 是否用動畫
     */
    public boolean isUseAnim() {
        return isUseAnimation;
    }

    /**
     * 設置動畫
     */
    public void setUseAnim(boolean isUseAnim) {
        this.isUseAnimation = isUseAnim;
    }

    /**
     * 設置動畫持續時間
     */
    public void setAnimationDuration(long duration) {
        this.animationDuration = duration;
    }

    /**
     * 設置動畫縮放模式
     */
    public void setAnimationScaleMode(@ScaleMode int scaleMode) {
        this.animationScaleMode = scaleMode;
    }

    /**
     * 設置動畫縮放比例
     */
    public void setAnimationScaleRate(float scaleRate) {
        scaleRate = scaleRate < 0 ? 0 : scaleRate;
        this.animationScaleRate = scaleRate;
    }

    /**
     * 是否開啟震動
     */
    public void setUseVibrate(boolean isUseVibrate) {
        this.isUseVibrate = isUseVibrate;
    }

    /**
     * 設置震動持續時間
     */
    public void setVibrateDuration(long duration) {
        this.vibrateDuration = duration;
    }

    /**
     * 設置一般狀態的圖片
     */
    public void setNormalImageResource(@DrawableRes int normalImageId) {
        this.normalImageId = normalImageId;
        if (radius != 0) {
            customPainter.setNormalBitmap(getContext(), radius, normalImageId);
        }
        postInvalidate();
    }

    /**
     * 設置按下狀態的圖片
     */
    public void setPressedImageResource(@DrawableRes int pressedImageId) {
        this.pressedImageId = pressedImageId;
        if (radius != 0) {
            customPainter.setPressBitmap(getContext(), radius, pressedImageId);
        }
        postInvalidate();
    }

    /**
     * 設置錯誤狀態的圖片
     */
    public void setErrorImageResource(@DrawableRes int errorImageId) {
        this.errorImageId = errorImageId;
        if (radius != 0) {
            customPainter.setErrorBitmap(getContext(), radius, errorImageId);
        }
        postInvalidate();
    }

    /**
     * 設置監聽
     */
    public void setGestureLockListener(OnGestureLockListener listener) {
        this.mGestureLockListener = listener;
    }

    public void setPainter(CustomPainter customPainter) {
        this.customPainter = customPainter;
        initPainter();
        postInvalidate();
    }

    /**
     * 顯示錯誤狀態
     */
    public void showErrorStatus() {
        isErrorStatus = true;
        for (GesturePoint point : pressPoints) {
            point.status = GesturePoint.POINT_ERROR_STATUS;
        }
        postInvalidate();
    }

    /**
     * 顯示錯誤狀態 (持續時間)
     *
     * @param millisecond 持續時間
     */
    public void showErrorStatus(long millisecond) {
        showErrorStatus();
        postDelayed(() -> {
            if (isErrorStatus) {
                clearView();
            }
        }, millisecond);
    }

    /**
     * 顯示正確狀態
     */
    public void showRightStatus() {
        isErrorStatus = false;
        for (GesturePoint point : pressPoints) {
            point.status = GesturePoint.POINT_RIGHT_STATUS;
        }
        postInvalidate();
    }

    /**
     * 顯示正確狀態 (持續時間)
     *
     * @param millisecond 持續時間
     */
    public void showRightStatus(long millisecond) {
        showRightStatus();
        postDelayed(() -> {
            if (!isErrorStatus) {
                clearView();
            }
        }, millisecond);
    }

    /**
     * 還原至初始狀態
     */
    public void clearView() {
        post(() -> {
            clear();
            postInvalidate();
        });
    }

}
