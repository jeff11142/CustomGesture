package com.example.custom_gesture_lib.utility;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

public class PatternLockUtils {
    public static boolean isNeedtoShowGestureLock = true;
    public static final int REQUEST_CODE_CONFIRM_PATTERN = 1214;

    public static final int PATTERN_STATUS_WRONG = 0;
    public static final int PATTERN_STATUS_UNLOCK = 1;
    public static final int PATTERN_STATUS_DELETE = 2;

    public static final int GESTURE = 997;
    public static final int GESTURE_LOGOUT = 996;
    public static final int GESTURE_FORGOT = 995;

    public static final int CONFIRM_PATTERN_LOCK = 998;
    public static final int CLOSE_APP = 999;

    private static VersionInfo mVersionInfo;

    private static int mPatternStatus;

    public static Boolean isUpdateDialogShowed = false;
    public static String appVersion = "AppVersion";

    private PatternLockUtils() {
    }

    public static VersionInfo getVersionInfo() {
        return mVersionInfo;
    }

    public static void setVersionInfo(VersionInfo versionInfo) {
        mVersionInfo = versionInfo;
    }

    public static void setPatternStatus(int patternStatus) {
        mPatternStatus = patternStatus;
    }

    public static int getPatternStatus() {
        return mPatternStatus;
    }

    public static void setPattern(List<PatternView.Cell> pattern, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_SHA1,
                PatternUtils.patternToSha1String(pattern), context);
    }

    public static String getPattern(Context context) {
        return PreferenceUtils.getString(PreferenceContract.KEY_PATTERN_SHA1,
                PreferenceContract.DEFAULT_PATTERN_SHA1, context);
    }

    public static void setDeletePattern(List<PatternView.Cell> pattern, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_DELETE_PATTERN_SHA1,
                PatternUtils.patternToSha1String(pattern), context);
    }

    public static String getDeletePattern(Context context) {
        return PreferenceUtils.getString(PreferenceContract.KEY_DELETE_PATTERN_SHA1,
                PreferenceContract.DEFAULT_DELETE_PATTERN_SHA1, context);
    }

//    public static void setUserId(int userId, Context context) {
//        PreferenceUtils.putInt(PreferenceContract.KEY_USER_ID,
//                userId, context);
//    }
//
//    public static int getUserId(Context context) {
//        return PreferenceUtils.getInt(PreferenceContract.KEY_USER_ID,
//                PreferenceContract.DEFAULT_USER_ID, context);
//    }

    public static void setUserIdSet(ArrayList<String> userIdList, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_USER_ID,
                userIdList.toString(), context);
    }

    public static List<String> getUserIdSet(Context context) {
        getOldVersionSetStringAndClear(context);
        return convertStringToList(PreferenceUtils.getString(PreferenceContract.KEY_USER_ID,
                PreferenceContract.DEFAULT_USER_ID, context));
    }

    public static List<String> convertStringToList(String stringList) {
        if (stringList != null && !stringList.isEmpty()) {
            return Arrays.asList(stringList.replaceAll("\\[", "").replaceAll("\\]", "").split(", "));
        } else {
            return new ArrayList<>();
        }
    }

    private static void getOldVersionSetStringAndClear(Context context) {
        if (PreferenceUtils.containKey(PreferenceContract.KEY_USER_ID_SET, context)) {
            Set<String> stringSet = PreferenceUtils.getStringSet(PreferenceContract.KEY_USER_ID_SET,
                    PreferenceContract.DEFAULT_USER_ID_SET, context);
            if (stringSet.size() > 0) {
                String string = Arrays.toString(Arrays.copyOf(stringSet.toArray(), stringSet.size(),
                        String[].class));

                PreferenceUtils.putString(PreferenceContract.KEY_USER_ID,
                        string, context);
            }
            PreferenceUtils.remove(PreferenceContract.KEY_USER_ID_SET, context);
        }
    }

    public static boolean hasPattern(Context context) {
        return !TextUtils.isEmpty(getPattern(context));
    }

    public static int getPatternType(List<PatternView.Cell> pattern, Context context) {
        int patternType;
        if (TextUtils.equals(PatternUtils.patternToSha1String(pattern), getPattern(context))) {
            patternType = PATTERN_STATUS_UNLOCK;
        } else if (TextUtils.equals(PatternUtils.patternToSha1String(pattern), getDeletePattern(context))) {
            patternType = PATTERN_STATUS_DELETE;
        } else {
            patternType = PATTERN_STATUS_WRONG;
        }
        return patternType;
    }

    public static boolean isPatternSame(List<PatternView.Cell> pattern, Context context) {
        return TextUtils.equals(PatternUtils.patternToSha1String(pattern), getPattern(context));
    }

    public static boolean isNeedUpdate(VersionInfo versionInfo, String version) {
        if (versionInfo == null) {
            return false;
        } else {
            String serverVersion = versionInfo.getVersion();
            return doNumberFormat(serverVersion) > doNumberFormat(version);
        }
    }

    private static int doNumberFormat(String text) {
        text = text.replace(".", "");
        if (text.length() > 5) {
            return Integer.parseInt(text);
        }
        String pattern = "%-5s";
        return Integer.parseInt(String.format(pattern, text).replace(" ", "0"));
    }

    public static void clearAppData(Context context) {
        ((ActivityManager) context.getSystemService(ACTIVITY_SERVICE))
                .clearApplicationUserData();
    }

    public static void clearPattern(Context context) {
        PreferenceUtils.remove(PreferenceContract.KEY_PATTERN_SHA1, context);
        PreferenceUtils.remove(PreferenceContract.KEY_DELETE_PATTERN_SHA1, context);
    }

    public static <ActivityType extends Activity & OnConfirmPatternResultListener> boolean
    checkConfirmPatternResult(ActivityType activity, int requestCode, int resultCode) {
        if (requestCode == REQUEST_CODE_CONFIRM_PATTERN) {
            activity.onConfirmPatternResult(resultCode == Activity.RESULT_OK);
            return true;
        } else {
            return false;
        }
    }

    public interface OnConfirmPatternResultListener {
        void onConfirmPatternResult(boolean successful);
    }
}