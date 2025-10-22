package com.example.thething4.overlay

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.thething4.ui.theme.CosTheme

class OverlayPermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosTheme {
                PermissionScreen(
                    onGrantClick = { OverlayController.openSystemPermission(this) },
                    onCancel = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this)) {
            setResult(RESULT_OK)
            finish()
        }
    }
}

@Composable
private fun PermissionScreen(
    onGrantClick: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Coś potrzebuje pozwolenia na wyświetlanie się nad innymi aplikacjami, aby żyć poza aplikacją.",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onGrantClick) {
                Text("Otwórz ustawienia")
            }
            Button(onClick = onCancel) {
                Text("Anuluj")
            }
        }
    }
}
