package com.lantern.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lantern.app.ui.theme.LanternTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanternTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkStoragePermission()) }

    // Launcher for handling the permission intent result (though for Manage Storage we mostly check on resume/recomposition)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasPermission = checkStoragePermission()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Lantern",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 48.sp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Status Indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (viewModel.isServerRunning) MaterialTheme.colorScheme.primary else Color.Gray)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (viewModel.isServerRunning) "Server Running" else "Server Stopped",
            style = MaterialTheme.typography.bodyLarge
        )

        if (viewModel.isServerRunning) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "http://${viewModel.ipAddress}:${viewModel.port}",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (!hasPermission) {
             Text(
                text = "Full storage access is required to host files.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { 
                if (!checkStoragePermission()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.addCategory("android.intent.category.DEFAULT")
                            intent.data = Uri.parse(String.format("package:%s", context.packageName))
                            launcher.launch(intent)
                        } catch (e: Exception) {
                            val intent = Intent()
                            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                            launcher.launch(intent)
                        }
                    } else {
                         // For older Android versions, we would request READ_EXTERNAL_STORAGE here
                         // But targeting simple high-level logic for now
                    }
                } else {
                    val rootDir = Environment.getExternalStorageDirectory()
                    viewModel.toggleServer(rootDir) 
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!hasPermission) Color.Gray else if (viewModel.isServerRunning) Color.Red else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = if (!hasPermission) "Grant Permission" else if (viewModel.isServerRunning) "Stop Server" else "Start Server",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Share files easily",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

fun checkStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true // Assume true for older versions for this specific prototype path, or implement checkSelfPermission(READ_EXTERNAL_STORAGE)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LanternTheme {
        MainScreen()
    }
}