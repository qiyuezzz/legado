package io.legado.app.lib.theme.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import io.legado.app.R
import io.legado.app.lib.theme.accentColor
import io.legado.app.utils.dpToPx

class ThemeCheckBox(context: Context, attrs: AttributeSet) : AppCompatCheckBox(context, attrs) {

    private var isUserAction = false

    init {
        if (!isInEditMode) {
            applyModernButtonDrawable(context.accentColor)
        }
    }

    private fun applyModernButtonDrawable(accent: Int) {
        val sizePx = 20.dpToPx()
        val circleBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setSize(sizePx, sizePx)
            setColor(accent)
        }
        val checkMark = ContextCompat.getDrawable(context, R.drawable.ic_checkbox_checked_mark)
        val checkedDrawable = if (checkMark != null) {
            LayerDrawable(arrayOf(circleBg, checkMark))
        } else {
            circleBg
        }
        val uncheckedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_checkbox_unchecked)
        val states = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
            addState(intArrayOf(), uncheckedDrawable)
        }
        buttonDrawable = states
        buttonTintList = null
        if (compoundDrawablePadding == 0) {
            compoundDrawablePadding = 10.dpToPx()
        }
    }

    override fun performClick(): Boolean {
        isUserAction = true
        val result = super.performClick()
        isUserAction = false
        return result
    }

    fun setOnUserCheckedChangeListener(listener: ((Boolean) -> Unit)?) {
        if (listener == null) {
            return super.setOnCheckedChangeListener(null)
        }
        super.setOnCheckedChangeListener { _, isChecked ->
            if (isUserAction) {
                listener.invoke(isChecked)
            }
        }
    }

}
