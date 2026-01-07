package com.holylong.easytouch.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.holylong.easytouch.R

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var audioManager: AudioManager
    private var floatingBallView: View? = null
    private var satelliteMenuView: View? = null
    private var ballParams: WindowManager.LayoutParams? = null
    private var menuParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var isMenuExpanded = false

    // 卫星按钮
    private var mainButton: FrameLayout? = null
    private var mainIcon: ImageView? = null
    private val satelliteButtons = mutableListOf<FrameLayout>()

    companion object {
        private const val CHANNEL_ID = "floating_service_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.holylong.easytouch.STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        initFloatingBall()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持悬浮窗运行"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EasyTouch")
            .setContentText("悬浮窗正在运行")
            .setSmallIcon(R.drawable.ic_menu_satellite)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun initFloatingBall() {
        floatingBallView = LayoutInflater.from(this).inflate(R.layout.layout_satellite_menu, null)
        val container = floatingBallView?.findViewById<FrameLayout>(R.id.satelliteContainer)

        mainButton = floatingBallView?.findViewById(R.id.mainButton)
        mainIcon = floatingBallView?.findViewById(R.id.mainIcon)

        // 初始化卫星按钮
        satelliteButtons.apply {
            add(floatingBallView?.findViewById(R.id.satellite1)!!)
            add(floatingBallView?.findViewById(R.id.satellite2)!!)
            add(floatingBallView?.findViewById(R.id.satellite3)!!)
            add(floatingBallView?.findViewById(R.id.satellite4)!!)
            add(floatingBallView?.findViewById(R.id.satellite5)!!)
            add(floatingBallView?.findViewById(R.id.satellite6)!!)
            add(floatingBallView?.findViewById(R.id.satellite7)!!)
            add(floatingBallView?.findViewById(R.id.satellite8)!!)
            add(floatingBallView?.findViewById(R.id.satellite9)!!)
            add(floatingBallView?.findViewById(R.id.satellite10)!!)
        }

        // 设置卫星按钮点击事件
        satelliteButtons[0].setOnClickListener { performHomeAction() }
        satelliteButtons[1].setOnClickListener { performBackAction() }
        satelliteButtons[2].setOnClickListener { performRecentAction() }
        satelliteButtons[3].setOnClickListener { performScreenshotAction() }
        satelliteButtons[4].setOnClickListener { performVolumeAction() }
        satelliteButtons[5].setOnClickListener { performLockAction() }
        satelliteButtons[6].setOnClickListener { performSearchAction() }
        satelliteButtons[7].setOnClickListener { performGalleryAction() }
        satelliteButtons[8].setOnClickListener { performContactsAction() }
        satelliteButtons[9].setOnClickListener { performDialerAction() }

        ballParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER or Gravity.START
            x = 100
            y = 500
        }

        // 只在主按钮上设置触摸监听
        mainButton?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = ballParams?.x ?: 0
                        initialY = ballParams?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY
                        if (kotlin.math.abs(deltaX) > 10 || kotlin.math.abs(deltaY) > 10) {
                            isDragging = true
                            ballParams?.x = initialX + deltaX.toInt()
                            ballParams?.y = initialY + deltaY.toInt()
                            windowManager.updateViewLayout(floatingBallView, ballParams)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            toggleMenu()
                        }
                        // 移除自动贴边功能，让按钮停留在用户拖动的位置
                        isDragging = false
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingBallView, ballParams)
    }

    private fun toggleMenu() {
        if (isMenuExpanded) {
            collapseMenu()
        } else {
            expandMenu()
        }
    }

    private fun expandMenu() {
        isMenuExpanded = true

        // 旋转主按钮图标
        mainIcon?.animate()
            ?.rotation(45f)
            ?.setDuration(300)
            ?.start()

        // 卫星按钮展开参数
        val containerSize = 400.dpToPx() // 容器总大小
        val satelliteButtonSize = 52.dpToPx() // 卫星按钮大小

        val centerX = (containerSize / 2).toFloat() // 容器中心
        val centerY = (containerSize / 2).toFloat()

        // 螺旋状布局参数
        val baseRadius = 80.dpToPx().toFloat() // 第一圈半径
        val radiusIncrement = 45.dpToPx().toFloat() // 每圈半径增量

        // 为每个卫星按钮设置位置和动画
        satelliteButtons.forEachIndexed { index, button ->
            // 螺旋状布局：半径随索引增加
            // 第一圈6个按钮（index 0-5），第二圈4个按钮（index 6-9）
            val layer = index / 6 // 所在圈层
            val indexInLayer = index % 6 // 圈内的索引

            // 计算半径和角度
            val radius = baseRadius + layer * radiusIncrement
            val angleInLayer = 360.0 / 6.0 // 每圈6个位置
            val angle = Math.toRadians(150 + indexInLayer * angleInLayer)

            val targetX = centerX + radius * kotlin.math.cos(angle).toFloat() - (satelliteButtonSize / 2)
            val targetY = centerY + radius * kotlin.math.sin(angle).toFloat() - (satelliteButtonSize / 2)

            button.visibility = View.VISIBLE
            button.x = centerX - (satelliteButtonSize / 2) // 初始位置在中心
            button.y = centerY - (satelliteButtonSize / 2)

            // 创建动画
            val animatorX = ObjectAnimator.ofFloat(button, "x", targetX)
            val animatorY = ObjectAnimator.ofFloat(button, "y", targetY)
            val animatorScale = ObjectAnimator.ofFloat(button, "scaleX", 0f, 1f)
            val animatorScaleY = ObjectAnimator.ofFloat(button, "scaleY", 0f, 1f)
            val animatorAlpha = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animatorX, animatorY, animatorScale, animatorScaleY, animatorAlpha)
            animatorSet.duration = 400
            animatorSet.startDelay = (index * 50).toLong() // 依次展开
            animatorSet.interpolator = OvershootInterpolator(1.2f)
            animatorSet.start()
        }
    }

    private fun collapseMenu() {
        isMenuExpanded = false

        // 旋转回主按钮图标
        mainIcon?.animate()
            ?.rotation(0f)
            ?.setDuration(300)
            ?.start()

        // 卫星按钮收起动画
        val containerSize = 400.dpToPx() // 容器总大小
        val satelliteButtonSize = 52.dpToPx() // 卫星按钮大小
        val centerX = (containerSize / 2).toFloat() // 容器中心
        val centerY = (containerSize / 2).toFloat()

        satelliteButtons.forEachIndexed { index, button ->
            val startX = button.x
            val startY = button.y

            val animatorX = ObjectAnimator.ofFloat(button, "x", centerX - (satelliteButtonSize / 2))
            val animatorY = ObjectAnimator.ofFloat(button, "y", centerY - (satelliteButtonSize / 2))
            val animatorScale = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0f)
            val animatorScaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0f)
            val animatorAlpha = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animatorX, animatorY, animatorScale, animatorScaleY, animatorAlpha)
            animatorSet.duration = 300
            animatorSet.startDelay = ((satelliteButtons.size - 1 - index) * 30).toLong() // 依次收起
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (index == satelliteButtons.size - 1) {
                        // 最后一个动画结束后隐藏所有按钮
                        satelliteButtons.forEach { it.visibility = View.INVISIBLE }
                    }
                }
            })
            animatorSet.start()
        }
    }

    private fun snapToEdge() {
        val screenWidth = windowManager.currentWindowMetrics.bounds.width()
        val containerSize = 400.dpToPx()
        val mainButtonSize = 64.dpToPx()

        // 容器中心到容器左边缘的距离
        val containerCenterOffset = containerSize / 2
        // 主按钮半径
        val mainButtonRadius = mainButtonSize / 2

        val currentX = ballParams?.x ?: 0

        // 计算贴边位置，让主按钮贴到屏幕边缘
        val targetX = if (currentX < screenWidth / 2) {
            // 贴左边：主按钮左边缘对齐屏幕左边缘
            // 容器位置 = -(容器中心到左边缘距离) + 主按钮半径
            -(containerCenterOffset - mainButtonRadius)
        } else {
            // 贴右边：主按钮右边缘对齐屏幕右边缘
            // 容器位置 = 屏幕宽度 - 容器中心到左边缘距离 - 主按钮半径
            screenWidth - containerCenterOffset - mainButtonRadius
        }

        ballParams?.x = targetX
        windowManager.updateViewLayout(floatingBallView, ballParams)
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun performHomeAction() {
        collapseMenu()
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    private fun performBackAction() {
        collapseMenu()
        val accessibilityService = EasyAccessibilityService.getInstance()
        if (accessibilityService != null) {
            accessibilityService.performBackAction()
        } else {
            showAccessibilityDialog()
        }
    }

    private fun performRecentAction() {
        collapseMenu()
        val accessibilityService = EasyAccessibilityService.getInstance()
        if (accessibilityService != null) {
            accessibilityService.performRecentsAction()
        } else {
            showAccessibilityDialog()
        }
    }

    private fun performScreenshotAction() {
        collapseMenu()
        Toast.makeText(this, "请使用电源键+音量减键截图", Toast.LENGTH_SHORT).show()
    }

    private fun performVolumeAction() {
        collapseMenu()

        val themeContext = ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog)
        val dialogView = LayoutInflater.from(themeContext).inflate(R.layout.layout_volume_dialog, null)
        val seekBar = dialogView?.findViewById<SeekBar>(R.id.volumeSeekBar)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        seekBar?.max = maxVolume
        seekBar?.progress = currentVolume
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val dialog = AlertDialog.Builder(themeContext)
            .setTitle("音量调节")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .setNegativeButton("静音") { _, _ ->
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            }
            .create()

        dialog.window?.setType(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
        )
        dialog.show()
    }

    private fun performLockAction() {
        collapseMenu()
        Toast.makeText(this, "锁屏功能需要设备管理器权限", Toast.LENGTH_LONG).show()
    }

    private fun performSearchAction() {
        collapseMenu()
        try {
            val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("query", "")
            }
            startActivity(searchIntent)
        } catch (e: Exception) {
            // 如果 ACTION_SEARCH 失败，尝试使用 Google 搜索
            try {
                val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, "无法打开搜索", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performGalleryAction() {
        collapseMenu()
        try {
            val galleryIntent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(galleryIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开图库", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performContactsAction() {
        collapseMenu()
        try {
            val contactsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.provider.ContactsContract.Contacts.CONTENT_URI
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(contactsIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开联系人", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performDialerAction() {
        collapseMenu()
        try {
            val dialerIntent = Intent(Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(dialerIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开拨号界面", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAccessibilityDialog() {
        val themeContext = ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog)

        val dialog = AlertDialog.Builder(themeContext)
            .setTitle("需要无障碍权限")
            .setMessage("请先启用无障碍服务才能使用此功能：\n\n1. 进入系统设置\n2. 找到无障碍功能\n3. 启用 EasyTouch 服务")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.window?.setType(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
        )
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingBallView?.let {
            windowManager.removeView(it)
        }
        satelliteMenuView?.let {
            windowManager.removeView(it)
        }
    }
}
