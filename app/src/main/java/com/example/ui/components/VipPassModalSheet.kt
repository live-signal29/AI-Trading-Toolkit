package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BullishGreen
import com.example.util.AdMobConfig
import kotlinx.coroutines.delay

/**
 * Compact Top-Bar badge/chip for unlocking VIP or showing active VIP status.
 * Replaces bulky home screen banners with a non-intrusive header trigger.
 */
@Composable
fun VipBadgeTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProActive = AdMobConfig.isRewardedProActive

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isProActive) BullishGreen.copy(alpha = 0.15f) else AccentGold.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isProActive) BullishGreen.copy(alpha = 0.4f) else AccentGold.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("vip_badge_trigger")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (isProActive) Icons.Default.CheckCircle else Icons.Default.WorkspacePremium,
                contentDescription = "VIP Status",
                tint = if (isProActive) BullishGreen else AccentGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isProActive) "VIP ACTIVE" else "GET VIP",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isProActive) BullishGreen else AccentGold
            )
        }
    }
}

/**
 * Dedicated modal sheet for voluntarily watching a rewarded ad to unlock VIP pass,
 * keeping the main home screen clean and uncluttered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipPassModalSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val activity = remember(context) { AdMobConfig.getActivity(context) }
    val isProActive = AdMobConfig.isRewardedProActive

    var remainingTimeText by remember { mutableStateOf("") }

    LaunchedEffect(isProActive, AdMobConfig.rewardedProExpiryTimestamp) {
        while (isProActive) {
            val remainingMs = AdMobConfig.rewardedProExpiryTimestamp - System.currentTimeMillis()
            if (remainingMs <= 0) {
                remainingTimeText = ""
                break
            }
            val minutes = (remainingMs / 60000).toInt()
            val hours = minutes / 60
            val mins = minutes % 60
            remainingTimeText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            delay(30000)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("vip_pass_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentGold.copy(alpha = 0.15f))
                            .border(1.dp, AccentGold.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "2-Hour VIP Pass",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enjoy 100% ad-free charts & real-time scanner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (isProActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BullishGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BullishGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BullishGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "VIP Pass Active",
                                fontWeight = FontWeight.Bold,
                                color = BullishGreen,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (remainingTimeText.isNotEmpty()) {
                                    "Ad-free experience active ($remainingTimeText remaining)"
                                } else {
                                    "Full VIP status currently active"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(AccentCyan.copy(alpha = 0.5f), AccentGold.copy(alpha = 0.5f)))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "What you get with VIP Pass:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        val benefits = listOf(
                            "Completely ad-free app experience (no banner or interstitial ads)",
                            "Uncapped real-time scanner & SMC alert frequencies",
                            "High-priority chart indicator calculations (EMA, Bollinger, Volume)",
                            "100% Free: Simply watch 1 short sponsor video"
                        )

                        benefits.forEach { benefit ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("• ", color = AccentCyan, fontWeight = FontWeight.Bold)
                                Text(
                                    text = benefit,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                AdMobConfig.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        Toast.makeText(
                                            context,
                                            "🎉 Reward unlocked! 2-Hour VIP Pass is now active.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onDismiss()
                                    },
                                    onDismissed = {
                                        // Dismiss callback
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("modal_watch_rewarded_ad_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Watch Sponsor Video to Unlock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
