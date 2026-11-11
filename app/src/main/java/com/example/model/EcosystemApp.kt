package com.example.model

import android.graphics.drawable.Drawable

data class EcosystemApp(
    val packageName: String,
    val label: String,
    val versionName: String = "1.0",
    val isSystemHephaistos: Boolean = true,
    val isManualIncluded: Boolean = false,
    val icon: Drawable? = null
)
