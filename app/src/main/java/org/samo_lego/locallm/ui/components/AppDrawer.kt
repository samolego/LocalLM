package org.samo_lego.locallm.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.samo_lego.locallm.ui.navigation.Routes
import org.samo_lego.locallm.ui.navigation.navigate

@Composable
fun AppDrawer(navController: NavHostController, drawerState: DrawerState) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row {
                        Button(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            onClick = {
                                // Launch settings
                                navController.navigate(Routes.SETTINGS)

                                // Close drawer
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Settings, "Settings button")
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = "Settings",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { /* do something */ },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, "Localized description")
                    }
                }
            )
        },
    ) { innerPadding ->
        Text(
            modifier = Modifier.padding(innerPadding),
            text = "Example of a scaffold with a bottom app bar."
        )
    }

}