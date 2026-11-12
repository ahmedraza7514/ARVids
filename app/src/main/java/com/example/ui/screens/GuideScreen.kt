package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GuideScreen(
    onTrySampleLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("guide_screen")
    ) {
        Text(
            text = "How to Download",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )
        Text(
            text = "3 simple steps to save any social media video offline",
            fontSize = 13.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Step 1
        StepCard(
            stepNumber = "1",
            title = "Copy Video Link",
            description = "Open Instagram, TikTok, Facebook, or X/Twitter. Tap 'Share' on the video and select 'Copy Link'.",
            icon = Icons.Default.ContentCopy,
            badgeColor = CyanSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 2
        StepCard(
            stepNumber = "2",
            title = "Open AR-Vids",
            description = "AR-Vids auto-detects your copied link! Or tap 'Paste' in the top bar to instantly insert your link.",
            icon = Icons.Default.Share,
            badgeColor = IndigoPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 3
        StepCard(
            stepNumber = "3",
            title = "Tap Download",
            description = "Choose your preferred video quality (Full HD, 1080p, 720p, or MP3 audio). The video will download directly into your Downloads folder.",
            icon = Icons.Default.Download,
            badgeColor = EmeraldTertiary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Advantages Card
        Text(
            text = "Why AR-Vids?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FeatureRow(
                    icon = Icons.Default.Block,
                    iconColor = EmeraldTertiary,
                    title = "100% Ad-Free",
                    description = "No intrusive banner ads, redirect spam, or pop-up windows."
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.VerifiedUser,
                    iconColor = CyanSecondary,
                    title = "Clean Interface",
                    description = "No messy website headers, no URLs shown, no third-party branding."
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Bolt,
                    iconColor = IndigoPrimary,
                    title = "Fast Direct Downloads",
                    description = "Downloads save directly into Android DownloadManager with notifications."
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Lock,
                    iconColor = CyanSecondary,
                    title = "Private & Anonymous",
                    description = "No account or login required. Your download history stays on your device."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Supported Networks
        Text(
            text = "Supported Networks",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        val networks = listOf(
            "Instagram Reels & Posts" to CyanSecondary,
            "TikTok Videos (No Watermark)" to EmeraldTertiary,
            "Facebook Watch & Videos" to IndigoPrimary,
            "X / Twitter Videos" to CyanSecondary,
            "Pinterest Pins" to EmeraldTertiary,
            "Reddit Videos" to IndigoPrimary,
            "Threads Clips" to CyanSecondary,
            "Vimeo & Dailymotion" to EmeraldTertiary
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            networks.forEach { (network, color) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkOutline, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = network,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun FeatureRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
