package io.legado.app.ui.book.read.config

import android.content.Context
import android.graphics.Color
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.lib.theme.bottomBackground

internal val Context.readingPanelColor: Int
    get() {
        val config = ReadBookConfig.durConfig
        return if (AppConfig.readBarStyleFollowPage && config.curBgType() == 0) {
            runCatching { Color.parseColor(config.curBgStr()) }.getOrDefault(bottomBackground)
        } else {
            bottomBackground
        }
    }
