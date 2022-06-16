package com.example.custom_gesture_lib.gesture.view.model;

public class GesturePoint {

    public static final int POINT_NORMAL_STATUS = 0x0001; // 正常狀態
    public static final int POINT_PRESS_STATUS = 0x0002; // 按下狀態
    public static final int POINT_ERROR_STATUS = 0x0003; // 錯誤狀態
    public static final int POINT_RIGHT_STATUS = 0x0004; // 正確狀態

    /**
     * 單位點的圓心坐標和半徑
     */
    public int x;
    public int y;
    public int radius;

    /**
     * 點狀態
     */
    public int status;

    /**
     * 點下標 (取值範圍[0,8]，用於解鎖完成後把手勢密碼轉換成數字密碼)
     */
    public int index;
}
