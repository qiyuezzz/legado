package io.legado.app.lib.theme

import android.graphics.Color
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import io.legado.app.help.config.AppConfig

internal fun Window.applyEInkDialogContrast() {
    if (AppConfig.isEInkMode) {
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        attributes = attributes.apply {
            dimAmount = 0f
            windowAnimations = 0
        }
    }
}

internal fun TextView.sharpenEInkText() {
    val colors = textColors
    if (colors != null && !colors.isStateful) {
        if (compositedLuminanceOnWhite(colors.defaultColor) < 210) {
            setTextColor(Color.BLACK)
        }
    }
    val hints = hintTextColors
    if (hints != null && !hints.isStateful) {
        val lum = compositedLuminanceOnWhite(hints.defaultColor)
        if (lum in 90..220) {
            setHintTextColor(Color.parseColor("#444444"))
        }
    }
}

private fun compositedLuminanceOnWhite(color: Int): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * a + 255 * (255 - a)) / 255
    val g = (Color.green(color) * a + 255 * (255 - a)) / 255
    val b = (Color.blue(color) * a + 255 * (255 - a)) / 255
    return (77 * r + 150 * g + 29 * b) shr 8
}
