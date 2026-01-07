package com.holylong.easytouch

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.holylong.easytouch.service.FloatingService
import com.holylong.easytouch.ui.theme.EasytouchTheme

class MainActivity : ComponentActivity() {

    private var isServiceRunning by mutableStateOf(false)

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (canDrawOverlays()) {
            startFloatingService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasytouchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        isServiceRunning = isServiceRunning,
                        onStartService = { requestOverlayPermissionAndStartService() },
                        onStopService = { stopFloatingService() }
                    )
                }
            }
        }
    }

    private fun requestOverlayPermissionAndStartService() {
        if (canDrawOverlays()) {
            startFloatingService()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
        }
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServiceRunning = true
        showPermissionDialog()
    }

    private fun stopFloatingService() {
        val intent = Intent(this, FloatingService::class.java).apply {
            action = FloatingService.ACTION_STOP
        }
        startService(intent)
        isServiceRunning = false
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("权限说明")
            .setMessage(
                "悬浮窗已启动！\n\n" +
                "部分功能需要额外权限：\n" +
                "• 返回键：使用无障碍服务\n" +
                "• 截图：需要媒体投影权限\n" +
                "• 锁屏：需要设备管理器权限\n\n" +
                "您可以先使用基础功能（主页），稍后再配置其他权限。"
            )
            .setPositiveButton("知道了", null)
            .show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isServiceRunning: Boolean,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EasyTouch",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "快捷悬浮窗工具",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isServiceRunning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "悬浮窗正在运行",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "您可以看到屏幕上的悬浮球，拖动它可以移动位置",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStopService,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("停止悬浮窗")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "功能说明",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("• 悬浮球可在屏幕上自由拖动")
                    Text("• 点击悬浮球展开快捷菜单")
                    Text("• 支持主页、返回、最近任务等快捷操作")
                    Text("• 拖动结束后自动贴边")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartService,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("启动悬浮窗")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "首次启动需要授予悬浮窗权限",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}