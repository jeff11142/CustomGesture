package com.example.custom_gesture_lib.gesture.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.custom_gesture_lib.R
import com.example.custom_gesture_lib.gesture.view.GestureLockView
import com.example.custom_gesture_lib.utility.PatternLockUtils
import com.example.custom_gesture_lib.utility.PreferenceContract
import com.example.custom_gesture_lib.utility.PreferenceUtils
import me.zhanghai.android.patternlock.PatternUtils
import me.zhanghai.android.patternlock.PatternView.Cell


class GestureViewModel(application: Application) : AndroidViewModel(application) {
    enum class SettingType { COMMON_GESTURE_SETTING, COMMON_GESTURE_SETTING_AGAIN, SPECIAL_GESTURE_SETTING, SPECIAL_GESTURE_SETTING_AGAIN, LOCK, UNLOCK, UNLOCK_LOGOUT }

    private val TAG = "GestureViewModel"
    private var tmpCommonGestureLockResult: String = ""
    private var tmpSpecialGestureLockResult: String = ""
    private val gestureLength = 4

    var commonGestureLock = MutableLiveData<String>()
    var specialGestureLock = MutableLiveData<String>()
    var nowType = MutableLiveData<SettingType>()

    fun getDescribeText(): Int {
        return when (nowType.value) {
            SettingType.COMMON_GESTURE_SETTING -> R.string.CommonGestureLookText
            SettingType.COMMON_GESTURE_SETTING_AGAIN -> R.string.CommonGestureLookAgainText
            SettingType.SPECIAL_GESTURE_SETTING -> R.string.SpecialGestureLookText
            SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.string.SpecialGestureLookAgainText
            else -> R.string.GestureLookText
        }
    }

    fun getDescribeTextColor(): Int {
        return when (nowType.value) {
            SettingType.SPECIAL_GESTURE_SETTING, SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.color.gesture_special_text
            else -> R.color.gesture_common_text
        }
    }

    fun getStepText(): Int {
        return when (nowType.value) {
            SettingType.COMMON_GESTURE_SETTING -> R.string.StepOne
            SettingType.COMMON_GESTURE_SETTING_AGAIN -> R.string.StepTwo
            SettingType.SPECIAL_GESTURE_SETTING -> R.string.StepThree
            SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.string.StepFour
            else -> R.string.Null
        }
    }

    fun getTitleText(): Int {
        return when (nowType.value) {
            SettingType.LOCK -> R.string.Gesture
            else -> R.string.GestureSetting
        }
    }

    fun getTitleTextColor(): Int {
        return when (nowType.value) {
            SettingType.SPECIAL_GESTURE_SETTING, SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.color.gesture_special_text
            else -> R.color.gesture_common_title_text
        }
    }

    fun getBackgroundColor(): Int {
        return when (nowType.value) {
            SettingType.SPECIAL_GESTURE_SETTING, SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.color.gesture_special_background
            else -> R.color.gesture_common_background
        }
    }

    fun checkGestureLockFromSharedPreferences(context: Context) {
        val specialGesture = PatternLockUtils.getDeletePattern(context)
        val commonGesture = PatternLockUtils.getPattern(context)

        nowType.value = if (commonGesture.isNullOrBlank() || specialGesture.isNullOrBlank()) {
            when {
                commonGesture.isNullOrBlank() -> {
                    SettingType.COMMON_GESTURE_SETTING
                }
                else -> {
                    SettingType.SPECIAL_GESTURE_SETTING
                }
            }
        } else {
            SettingType.LOCK
        }
    }

    fun isNeedToShowSettingDialog(context: Context, appVersion: String): Boolean {
        val version: String? = PreferenceUtils.getString(
            PreferenceContract.KEY_APP_VERSION,
            PreferenceContract.DEFAULT_APP_VERSION, context
        )
        return when {
            version == null -> true
            appVersion != version -> true
            else -> false
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun setCommonGestureLockFromSharedPreferences(context: Context, result: String) {
        PatternLockUtils.setPattern(getResultList(result), context)

        Log.d(TAG, "save COMMOM_GESTURE_LOOK success")
        getCommonGestureLockFromSharedPreferences(context)
    }

    private fun getCommonGestureLockFromSharedPreferences(context: Context) {
        commonGestureLock.value = PatternLockUtils.getPattern(context)
    }

    @SuppressLint("CommitPrefEdits")
    fun setSpecialGestureLockFromSharedPreferences(context: Context, result: String) {
        PatternLockUtils.setDeletePattern(getResultList(result), context)
        Log.d(TAG, "save SPECIAL_GESTURE_LOOK success")
        getSpecialGestureLockFromSharedPreferences(context)
    }

    private fun getSpecialGestureLockFromSharedPreferences(context: Context) {
        specialGestureLock.value = PatternLockUtils.getDeletePattern(context)
    }


    fun redrawGesture() {
        when (nowType.value) {
            SettingType.COMMON_GESTURE_SETTING_AGAIN -> {
                tmpCommonGestureLockResult = ""
                nowType.value = SettingType.COMMON_GESTURE_SETTING
            }
            SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> {
                tmpSpecialGestureLockResult = ""
                nowType.value = SettingType.SPECIAL_GESTURE_SETTING
            }
            else -> {
                //do nothing
            }
        }
    }

    fun getGestureLockSuccess(context: Context, result: String?): Boolean {
        return when {
            result.isNullOrBlank() -> false
            else -> {
                when (nowType.value) {
                    SettingType.COMMON_GESTURE_SETTING -> {
                        if(result.length < gestureLength){
                            showErrorToast(context, context.getString(R.string.GestureLengthNotEnough))
                            false
                        }else {
                            nowType.value = SettingType.COMMON_GESTURE_SETTING_AGAIN
                            tmpCommonGestureLockResult = result
                            true
                        }

                    }
                    SettingType.COMMON_GESTURE_SETTING_AGAIN -> {
                        if (result == tmpCommonGestureLockResult) {
                            setCommonGestureLockFromSharedPreferences(context, result)
                            true
                        } else {
                            false
                        }
                    }
                    SettingType.SPECIAL_GESTURE_SETTING -> {
                        if(result.length < gestureLength){
                            showErrorToast(context, context.getString(R.string.GestureLengthNotEnough))
                            false
                        }else {
                            checkSpecialGestureLegal(context, result)
                        }
                    }
                    SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> {
                        if (result == tmpSpecialGestureLockResult) {
                            setSpecialGestureLockFromSharedPreferences(context, result)
                            true
                        } else {
                            false
                        }
                    }
                    else -> {
                        checkGestureIsCommonOrSpecial(context, result)
                    }
                }
            }
        }
    }

    private fun checkSpecialGestureLegal(context: Context, result: String): Boolean {
        tmpSpecialGestureLockResult = result

        val legal = !PatternLockUtils.isPatternSame(getResultList(result), context)
        if (!legal) {
            showErrorToast(context, context.getString(R.string.SpecialGestureCantSameAsCommon))
        } else {
            nowType.value = SettingType.SPECIAL_GESTURE_SETTING_AGAIN
        }
        return legal
    }

    private fun checkGestureIsCommonOrSpecial(context: Context, result: String): Boolean {
        val pattern = getResultList(result)
        var isCommonOrSpecial = false
        if (TextUtils.equals(
                PatternUtils.patternToSha1String(pattern),
                PatternLockUtils.getPattern(context)
            )
        ) {
            isCommonOrSpecial = true
            nowType.value = SettingType.UNLOCK
        } else if (TextUtils.equals(
                PatternUtils.patternToSha1String(pattern),
                PatternLockUtils.getDeletePattern(context)
            )
        ) {
            isCommonOrSpecial = true
            nowType.value = SettingType.UNLOCK_LOGOUT
        }

        return isCommonOrSpecial
    }

    private fun getResultList(result: String): List<Cell> {
        val array: Array<String> = result.toCharArray().map { it.toString() }.toTypedArray()
        val list: MutableList<Cell> = mutableListOf()
        array.forEach { string ->
            val row = string.toInt() / GestureLockView.sideLength
            val column = string.toInt() % GestureLockView.sideLength
            list.add(Cell.of(row, column))
        }
        return list
    }

    private fun showErrorToast(context: Context, error: String){
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }
}