package com.example.custom_gesture_lib.utility;

import android.content.Context;

public class DimensionUtility {
    /**
     * dp轉px
     *
     * @param context 上下文環境
     * @param dp      獨立像素密度（像素無關）
     */
    public static int dp2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5F);
    }

    /**
     * px轉dp
     *
     * @param context 上下文環境
     * @param px      像素
     */
    public static int px2dp(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5F);
    }
}
