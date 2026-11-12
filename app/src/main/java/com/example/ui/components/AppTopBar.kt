package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    adShieldActive: Boolean,
    onToggleShield: () -> Unit,
    onReload: () -> Unit,
    onPasteFromClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("app_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkSurface
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App Logo Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoPrimary, CyanSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "AR-Vids Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AR-Vids",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(IndigoPrimary.copy(alpha = 0.25f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyanSecondary
                            )
                        }
                    }
                    Text(
                        text = "Ad-Free Video Downloader",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        actions = {
            // Ad-Shield Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (adShieldActive) EmeraldTertiary.copy(alpha = 0.15f)
                        else TextMuted.copy(alpha = 0.15f)
                    )
                    .clickable { onToggleShield() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("shield_status_badge"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Status",
                        tint = if (adShieldActive) EmeraldTertiary else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (adShieldActive) "Shield ON" else "Shield OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (adShieldActive) EmeraldTertiary else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onPasteFromClipboard,
                modifier = Modifier.testTag("action_paste_clipboard")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste Clipboard Link",
                    tint = CyanSecondary
                )
            }

            IconButton(
                onClick = onReload,
                modifier = Modifier.testTag("action_refresh_page")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload Page",
                    tint = Color.White
                )
            }
        }
    )
}
