package io.legado.app.ui.widget.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.legado.app.R
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor

class AccentStrokeTextView(context: Context, attrs: AttributeSet) :
    AppCompatTextView(context, attrs) {

    private var radius = resources.getDimensionPixelSize(R.dimen.corner_radius_control)
    private val isBottomBackground: Boolean

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AccentStrokeTextView)
        radius = typedArray.getDimensionPixelOffset(R.styleable.StrokeTextView_radius, radius)
        isBottomBackground =
            typedArray.getBoolean(R.styleable.StrokeTextView_isBottomBackground, false)
        typedArray.recycle()
        upStyle()
    }

    private fun upStyle() {
        val isLight = ColorUtils.isColorLight(context.bottomBackground)
        val disableTextColor = if (isLight) {
            context.getCompatColor(R.color.icon_muted)
        } else {
            context.getCompatColor(R.color.md_dark_disabled)
        }
        val disableBgColor = if (isLight) {
            android.graphics.Color.parseColor("#F1F3F6")
        } else {
            android.graphics.Color.parseColor("#262930")
        }
        val accentColor = if (isInEditMode) {
            context.getCompatColor(R.color.accent)
        } else {
            ThemeStore.accentColor(context)
        }
        val softBg = ColorUtils.withAlpha(accentColor, 0.12f)
        val pressedBg = ColorUtils.withAlpha(accentColor, 0.22f)
        background = Selector.shapeBuild()
            .setCornerRadius(radius)
            .setDefaultBgColor(softBg)
            .setPressedBgColor(pressedBg)
            .setDisabledBgColor(disableBgColor)
            .create()
        setTextColor(
            Selector.colorBuild()
                .setDefaultColor(accentColor)
                .setDisabledColor(disableTextColor)
                .create()
        )
    }

}
