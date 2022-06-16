package com.example.custom_gesture_lib.gesture

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.custom_gesture_lib.R
import com.example.custom_gesture_lib.databinding.ActivityGestureLookBinding
import com.example.custom_gesture_lib.gesture.view.listener.OnGestureLockListener
import com.example.custom_gesture_lib.gesture.view.painter.CirclePainter
import com.example.custom_gesture_lib.utility.PatternLockUtils
import com.example.custom_gesture_lib.utility.PatternLockUtils.*
import com.example.custom_gesture_lib.utility.VersionInfo
import com.example.custom_gesture_lib.gesture.viewmodel.GestureViewModel
import com.example.custom_gesture_lib.version.ApiService
import com.example.custom_gesture_lib.version.AppClientManager
import org.telegram.version.VersionData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CustomGestureActivity : AppCompatActivity(), OnGestureLockListener {
    private lateinit var binding: ActivityGestureLookBinding
    private var updateDialog: AlertDialog? = null
    private var forgotDialog: AlertDialog? = null
    private var appVersion: String? = null

    private val gestureViewModel: GestureViewModel by lazy {
        ViewModelProvider(this)[GestureViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        binding = ActivityGestureLookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setGestureViewModel()
        gestureViewModel.checkGestureLockFromSharedPreferences(this)
        initView()
        checkAppVersion()

        appVersion = intent.getStringExtra(PatternLockUtils.appVersion)
    }

    override fun onResume() {
        super.onResume()
        if (gestureViewModel.isNeedToShowSettingDialog(this, appVersion ?: "")) {
            showSettingDialog()
        }
    }

    private fun checkAppVersion() {
        val apiService = AppClientManager.client.create(ApiService::class.java)
        apiService.getVersionData().enqueue(object : Callback<VersionData> {
            override fun onResponse(call: Call<VersionData>, response: Response<VersionData>) {
                val data = response.body()?.android
                if (isNeedUpdate(
                        VersionInfo(
                            data?.force,
                            data?.version,
                            data?.downloadUrl
                        ), appVersion
                    )
                ) {
                    showUpdateDialog(
                        VersionInfo(
                            data?.force,
                            data?.version,
                            data?.downloadUrl
                        )
                    )
                }
            }

            override fun onFailure(call: Call<VersionData>, t: Throwable) {
            }
        })
    }

    private fun showUpdateDialog(versionInfo: VersionInfo) {
        if (!isUpdateDialogShowed) {
            if (updateDialog == null) {
                val builder = AlertDialog.Builder(this@CustomGestureActivity)
                builder.setCancelable(false)
                builder.setMessage("App has a new version.\nDo you want to update?")
                if (versionInfo.force == "0") {
                    builder.setPositiveButton(
                        getString(R.string.Cancel)
                    ) { _, _ ->
                        isUpdateDialogShowed = true
                    }
                }
                builder.setNegativeButton(
                    getString(R.string.Update)
                ) { _, _ ->
                    val url = versionInfo.downloadUrl
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    finish()
                }
                updateDialog = builder.create()
            } else {
                if (updateDialog?.isShowing == true) {
                    updateDialog?.dismiss()
                }
            }
            updateDialog?.show()
        }
    }

    private fun showForgotDialog() {
        if (forgotDialog == null) {
            val builder = AlertDialog.Builder(this@CustomGestureActivity)
            builder.setCancelable(false)
            builder.setMessage("Do you want to reset pattern ?")
            builder.setPositiveButton(
                getString(R.string.Cancel)
            ) { _, _ -> }
            builder.setNegativeButton(
                getString(R.string.Reset)
            ) { _, _ ->
                clearPattern(this@CustomGestureActivity)
                setResult(GESTURE_FORGOT)
                finish()
            }
            forgotDialog = builder.create()
        } else {
            if (forgotDialog?.isShowing == true) {
                forgotDialog?.dismiss()
            }
        }
        forgotDialog?.show()
    }

    private fun setStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) // 確認取消半透明設置。
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 跟系統表示要渲染 system bar 背景。
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun setGestureViewModel() {
        gestureViewModel.commonGestureLock.observe(this) {
            if (it.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.GetCommonGestureError), Toast.LENGTH_SHORT)
                    .show()
            }
            gestureViewModel.checkGestureLockFromSharedPreferences(this)
        }

        gestureViewModel.specialGestureLock.observe(this) {
            if (it.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.GetSpecialGestureError), Toast.LENGTH_SHORT)
                    .show()
            } else {
                isNeedtoShowGestureLock = false
                finish()
            }
        }

        gestureViewModel.nowType.observe(this) {
            when (it) {
                GestureViewModel.SettingType.UNLOCK -> {
                    isNeedtoShowGestureLock = false
                    finish()
                }
                GestureViewModel.SettingType.UNLOCK_LOGOUT -> {
                    isNeedtoShowGestureLock = false
                    setResult(GESTURE_LOGOUT)
                    finish()
                }
                else -> {
                    val anim = AlphaAnimation(1.0f, 0.0f)
                    anim.duration = 200
                    anim.repeatCount = 1
                    anim.repeatMode = Animation.REVERSE

                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                            binding.apply {
                                tvGestureDec.apply {
                                    text = getString(gestureViewModel.getDescribeText())
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            gestureViewModel.getDescribeTextColor()
                                        )
                                    )
                                }

                                tvStep.apply {
                                    text = getString(gestureViewModel.getStepText())
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            gestureViewModel.getDescribeTextColor()
                                        )
                                    )
                                }
                            }
                        }
                    })

                    binding.apply {
                        tvGestureDec.startAnimation(anim)
                        tvStep.startAnimation(anim)

                        tvRedraw.visibility = when (it) {
                            GestureViewModel.SettingType.COMMON_GESTURE_SETTING_AGAIN, GestureViewModel.SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> View.VISIBLE
                            else -> View.GONE
                        }

                        tvForgot.visibility = when (it) {
                            GestureViewModel.SettingType.LOCK -> View.VISIBLE
                            else -> View.GONE
                        }

                        with(rootView) {
                            setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    gestureViewModel.getBackgroundColor()
                                )
                            )
                        }

                        with(gestureView) {
                            when (it) {
                                GestureViewModel.SettingType.SPECIAL_GESTURE_SETTING, GestureViewModel.SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> {
                                    setNormalColor(Color.parseColor("#FFFFFF"))
                                    setPressedColor(Color.parseColor("#fdb915"))
                                }
                                else -> {
                                    setNormalColor(Color.parseColor("#808080"))
                                    setPressedColor(Color.parseColor("#2AABEE"))
                                }
                            }
                        }

                        with(tvGestureTitle) {
                            text = getString(gestureViewModel.getTitleText())
                            setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    gestureViewModel.getTitleTextColor()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initView() {

        binding.apply {
            gestureView.apply {
                setPainter(CirclePainter())
                setGestureLockListener(this@CustomGestureActivity)
            }

            tvGestureDec.apply {
                text = getString(gestureViewModel.getDescribeText())
            }

            tvRedraw.apply {
                setOnClickListener {
                    gestureViewModel.redrawGesture()
                }
            }

            tvForgot.apply {
                setOnClickListener {
                    showForgotDialog()
                }
            }

            tvGestureTitle.apply {
                setOnLongClickListener {
                    when (gestureViewModel.nowType.value) {
                        GestureViewModel.SettingType.LOCK -> {
                            showSettingDialog()
                        }
                        else -> {
                            //do nothing
                        }
                    }

                    true
                }
            }
        }
    }

    private fun showSettingDialog() {
//        val settingDialog = SettingAccountDialog(this@GestureActivity)
//        settingDialog.show(false)
    }

    override fun onStarted() {
    }

    override fun onProgress(progress: String?) {
    }

    override fun onComplete(result: String?) {
        when (gestureViewModel.getGestureLockSuccess(this, result)) {
            true -> {
                binding.gestureView.clearView()
            }
            else -> {
                binding.gestureView.showErrorStatus(1000)
            }
        }
    }
}