package com.fitnessapp.tracker.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.R
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    // 1. Loading progress state (animates from 0f to 1f over 1.8s)
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1800, easing = LinearEasing),
        label = "loadingProgress"
    )
    
    // 2. Pulse breathing animation for the central logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoAlpha"
    )

    // Trigger progress loading and transition
    LaunchedEffect(Unit) {
        progress = 1f
        delay(1900) // Wait slightly longer than 1.8s for smooth fade
        onTimeout()
    }

    // Map loading progress to status text
    val statusText = when {
        animatedProgress < 0.3f -> "正在初始化数据引擎..."
        animatedProgress < 0.7f -> "正在加载动力图表..."
        else -> "已建立生物链接..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C)) // Deep space background fallback
    ) {
        // Space-dark background image with strong overlay contrast
        Image(
            painter = painterResource(id = R.drawable.default_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color(0xFF0A0A0C).copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo with pulsing neon ring and breathing animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
            ) {
                // Background neon glow circle
                Box(
                    modifier = Modifier
                        .size(106.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    themeColors.primary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                // Actual icon image wrapped in rounded border
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tech style title with character spacing and subtle colors
            Text(
                text = "马牛",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.primary,
                letterSpacing = 6.sp,
                modifier = Modifier.padding(start = 6.dp) // Offset for symmetry due to letterSpacing
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "智能运动追踪系统",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 2.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            // Cyberpunk loading components
            Box(
                modifier = Modifier.width(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Sleek loader track
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = themeColors.primary,
                        trackColor = Color(0xFF16161A)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Progress percentage + dynamic subtext
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.primary
                        )
                    }
                }
            }
        }
    }
}
