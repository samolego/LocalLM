package org.samo_lego.locallm.ui.navigation

import androidx.navigation.NavController

fun NavController.navigate(route: Routes) = navigate(route.path)

enum class Routes(val path: String) {
    HOME("home"),
    SETTINGS("settings"),
}