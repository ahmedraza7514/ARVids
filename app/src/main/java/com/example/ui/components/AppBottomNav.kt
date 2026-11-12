package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NavTab

@Composable
fun AppBottomNav(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("app_bottom_nav"),
        containerColor = DarkSurface,
        contentColor = TextSecondary
    ) {
        NavigationBarItem(
            modifier = Modifier.testTag("nav_item_downloader"),
            selected = selectedTab == NavTab.DOWNLOADER,
            onClick = { onTabSelected(NavTab.DOWNLOADER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Downloader"
                )
            },
            label = { Text("Downloader") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CyanSecondary,
                indicatorColor = IndigoPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            modifier = Modifier.testTag("nav_item_library"),
            selected = selectedTab == NavTab.LIBRARY,
            onClick = { onTabSelected(NavTab.LIBRARY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Downloads Library"
                )
            },
            label = { Text("Downloads") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CyanSecondary,
                indicatorColor = IndigoPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            modifier = Modifier.testTag("nav_item_guide"),
            selected = selectedTab == NavTab.GUIDE,
            onClick = { onTabSelected(NavTab.GUIDE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "User Guide"
                )
            },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CyanSecondary,
                indicatorColor = IndigoPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            modifier = Modifier.testTag("nav_item_settings"),
            selected = selectedTab == NavTab.SETTINGS,
            onClick = { onTabSelected(NavTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = CyanSecondary,
                indicatorColor = IndigoPrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )
    }
}
