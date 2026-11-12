package io.legado.app.ui.book.read.config

import android.content.Context
import io.legado.app.lib.theme.readingSurfaceColor
import io.legado.app.lib.theme.bottomBackground

internal val Context.readingPanelColor: Int
    get() = readingSurfaceColor ?: bottomBackground
