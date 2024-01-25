package org.samo_lego.locallm.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.samo_lego.locallm.config.SettingsKeys
import org.samo_lego.locallm.config.appSettings


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Settings() {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Settings")
            })
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
        ) {
            var useChatML by remember {
                mutableStateOf(
                    appSettings.getBool(
                        SettingsKeys.USE_CHATML,
                        false
                    )
                )
            }
            Text(text = "Use ChatML")
            Switch(checked = useChatML, onCheckedChange = {
                useChatML = it
                appSettings.setBool(SettingsKeys.USE_CHATML, it)
            })
        }
    }
}