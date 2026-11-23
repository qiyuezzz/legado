package io.legado.app.lib.theme

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatViewInflater
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView

/**
 * Used by ThemeOverlay.EInk so muted/secondary gray text renders in crisp solid black ink
 * on E-Ink screens instead of hardware-dithered gray strokes, without affecting Light/Dark themes.
 */
@Keep
class EInkViewInflater : AppCompatViewInflater() {

    override fun createTextView(context: Context, attrs: AttributeSet?): AppCompatTextView {
        return super.createTextView(context, attrs).apply { sharpenEInkText() }
    }

    override fun createButton(context: Context, attrs: AttributeSet?): AppCompatButton {
        return super.createButton(context, attrs).apply { sharpenEInkText() }
    }

    override fun createEditText(context: Context, attrs: AttributeSet?): AppCompatEditText {
        return super.createEditText(context, attrs).apply { sharpenEInkText() }
    }

    override fun createCheckBox(context: Context, attrs: AttributeSet?): AppCompatCheckBox {
        return super.createCheckBox(context, attrs).apply { sharpenEInkText() }
    }

    override fun createRadioButton(context: Context, attrs: AttributeSet?): AppCompatRadioButton {
        return super.createRadioButton(context, attrs).apply { sharpenEInkText() }
    }

    override fun createCheckedTextView(
        context: Context,
        attrs: AttributeSet?
    ): AppCompatCheckedTextView {
        return super.createCheckedTextView(context, attrs).apply { sharpenEInkText() }
    }

    override fun createAutoCompleteTextView(
        context: Context,
        attrs: AttributeSet?
    ): AppCompatAutoCompleteTextView {
        return super.createAutoCompleteTextView(context, attrs).apply { sharpenEInkText() }
    }
}
