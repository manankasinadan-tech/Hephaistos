package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

enum class LauncherScreen {
    HOME,
    CONSOLE,
    SETTINGS
}

@Composable
fun MacosDock(
    currentScreen: LauncherScreen,
    onNavigate: (LauncherScreen) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val rotationAngle = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(32.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(32.dp)
                ),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Button (Quick return to launcher grid)
                DockIconButton(
                    icon = Icons.Default.Home,
                    contentDescription = "Accueil",
                    testTag = "dock_home_button",
                    isActive = currentScreen == LauncherScreen.HOME,
                    onClick = { onNavigate(LauncherScreen.HOME) }
                )

                // Refresh Button
                DockIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir les applications",
                    testTag = "dock_refresh_button",
                    isActive = false,
                    iconModifier = Modifier.rotate(rotationAngle.value),
                    onClick = {
                        coroutineScope.launch {
                            rotationAngle.animateTo(
                                targetValue = rotationAngle.value + 360f,
                                animationSpec = tween(durationMillis = 600, easing = LinearEasing)
                            )
                        }
                        onRefresh()
                    }
                )

                // Divider dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                // Console (Terminal) Button
                DockIconButton(
                    icon = Icons.Default.Terminal,
                    contentDescription = "Console Terminal",
                    testTag = "dock_console_button",
                    isActive = currentScreen == LauncherScreen.CONSOLE,
                    activeBadgeColor = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        if (currentScreen == LauncherScreen.CONSOLE) {
                            onNavigate(LauncherScreen.HOME)
                        } else {
                            onNavigate(LauncherScreen.CONSOLE)
                        }
                    }
                )

                // Settings Button
                DockIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Paramètres",
                    testTag = "dock_settings_button",
                    isActive = currentScreen == LauncherScreen.SETTINGS,
                    activeBadgeColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        if (currentScreen == LauncherScreen.SETTINGS) {
                            onNavigate(LauncherScreen.HOME)
                        } else {
                            onNavigate(LauncherScreen.SETTINGS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DockIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    activeBadgeColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isActive) activeBadgeColor.copy(alpha = 0.18f) else Color.Transparent
            )
            .border(
                width = if (isActive) 1.5.dp else 0.dp,
                color = if (isActive) activeBadgeColor.copy(alpha = 0.8f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) activeBadgeColor else MaterialTheme.colorScheme.onSurface,
            modifier = iconModifier.size(26.dp)
        )

        // Little macOS dock indicator dot under active icon
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(activeBadgeColor)
            )
        }
    }
}
