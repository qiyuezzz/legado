package io.legado.app.ui.book.read.config

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils.calculateContrast
import androidx.core.graphics.ColorUtils.setAlphaComponent
import io.legado.app.utils.ColorUtils
import io.legado.app.lib.theme.readingSurfaceColor
import io.legado.app.lib.theme.bottomBackground

internal val Context.readingPanelColor: Int
    get() {
        val page = setAlphaComponent(readingSurfaceColor ?: bottomBackground, 255)
        val light = ColorUtils.isColorLight(page)
        val surface = if (light) Color.rgb(243, 244, 246) else Color.rgb(37, 40, 45)
        return if (calculateContrast(surface, page) >= 1.12) surface
        else if (light) Color.rgb(224, 229, 236) else Color.rgb(58, 63, 71)
    }
