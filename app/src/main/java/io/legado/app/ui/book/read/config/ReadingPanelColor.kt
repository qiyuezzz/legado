package io.legado.app.ui.book.read.config

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.R as AppCompatR
import androidx.appcompat.view.ContextThemeWrapper
import io.legado.app.R
import io.legado.app.help.config.ThemeConfig

internal val Context.readingPanelColor: Int
    get() = if (ThemeConfig.isDarkTheme()) {
        getColor(R.color.app_theme_dark_primary)
    } else {
        Color.WHITE
    }

internal fun Context.withReadingPanelTheme(): Context {
    if (ThemeConfig.isDarkTheme()) return this
    return ContextThemeWrapper(this, AppCompatR.style.Theme_AppCompat_Light).apply {
        applyOverrideConfiguration(Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_NO
        })
    }
}
