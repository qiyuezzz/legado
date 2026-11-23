package io.legado.app.ui.book.read.config

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.view.ContextThemeWrapper
import io.legado.app.R
import io.legado.app.help.config.AppConfig

internal const val READING_PANEL_COLOR = Color.WHITE

internal fun Context.withLightReadingPanelTheme(): Context =
    ContextThemeWrapper(this, R.style.AppTheme_Light).apply {
        applyOverrideConfiguration(Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_NO
        })
        if (AppConfig.useWhitePageBackground) {
            theme.applyStyle(R.style.ThemeOverlay_WhitePages, true)
        }
        if (AppConfig.isEInkMode) {
            theme.applyStyle(R.style.ThemeOverlay_EInk, true)
        }
    }
