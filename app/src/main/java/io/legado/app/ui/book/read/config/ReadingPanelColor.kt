package io.legado.app.ui.book.read.config

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.R
import androidx.appcompat.view.ContextThemeWrapper

internal const val READING_PANEL_COLOR = Color.WHITE

internal fun Context.withLightReadingPanelTheme(): Context =
    ContextThemeWrapper(this, R.style.Theme_AppCompat_Light).apply {
        applyOverrideConfiguration(Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_NO
        })
    }
