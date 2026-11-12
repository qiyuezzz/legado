package io.legado.app.lib.theme

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.ui.book.read.BaseReadBookActivity
import io.legado.app.utils.ColorUtils

internal val Context.readingSurfaceColor: Int?
    get() {
        var current = this
        while (current is ContextWrapper) {
            if (current is BaseReadBookActivity) {
                val config = ReadBookConfig.durConfig
                return if (AppConfig.readBarStyleFollowPage && config.curBgType() == 0) {
                    runCatching { Color.parseColor(config.curBgStr()) }.getOrNull()
                } else null
            }
            current = current.baseContext
        }
        return null
    }

internal val Context.overlaySurfaceColor get() = readingSurfaceColor ?: backgroundColor

internal val Context.overlayTextColor: Int
    get() = getPrimaryTextColor(ColorUtils.isColorLight(overlaySurfaceColor))
