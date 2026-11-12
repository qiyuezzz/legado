package io.legado.app.utils

import android.app.Dialog
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import androidx.core.view.descendants
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.overlaySurfaceColor
import io.legado.app.lib.theme.overlayTextColor
import io.legado.app.lib.theme.readingSurfaceColor
import io.legado.app.lib.theme.setDialogActionBackground
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.dialogCornerRadius
import io.legado.app.lib.theme.filletBackground
import splitties.systemservices.windowManager

fun AlertDialog.applyTint(): AlertDialog {
    window?.setBackgroundDrawable(context.filletBackground.apply {
        cornerRadius = dialogCornerRadius
        setColor(context.overlaySurfaceColor)
    })
    val colorStateList = Selector.colorBuild()
        .setDefaultColor(context.readingSurfaceColor?.let { context.overlayTextColor } ?: ThemeStore.accentColor(context))
        .setPressedColor(ColorUtils.darkenColor(ThemeStore.accentColor(context)))
        .create()
    window?.decorView?.post {
        if (context.readingSurfaceColor != null) {
            (window?.decorView as? android.view.ViewGroup)?.descendants?.filterIsInstance<TextView>()?.forEach {
                it.setTextColor(context.overlayTextColor)
            }
        }
        val paper = context.overlaySurfaceColor
        val ink = context.getPrimaryTextColor(ColorUtils.isColorLight(paper))
        listOf(AlertDialog.BUTTON_NEGATIVE, AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEUTRAL)
            .forEach { which ->
                getButton(which)?.apply {
                    setTextColor(colorStateList)
                    if (!AppConfig.isEInkMode) setDialogActionBackground(paper, ink)
                }
            }
        listView?.forEach {
            it.applyTint(context.readingSurfaceColor?.let { context.overlayTextColor } ?: context.accentColor)
        }
    }
    return this
}

fun AlertDialog.requestInputMethod() {
    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}

fun DialogFragment.setLayout(widthMix: Float, heightMix: Float) {
    dialog?.setLayout(widthMix, heightMix)
}

fun Dialog.setLayout(widthMix: Float, heightMix: Float) {
    val dm = context.windowManager.windowSize
    window?.setLayout(
        (dm.widthPixels * widthMix).toInt(),
        (dm.heightPixels * heightMix).toInt()
    )
}

fun DialogFragment.setLayout(width: Int, heightMix: Float) {
    dialog?.setLayout(width, heightMix)
}

fun Dialog.setLayout(width: Int, heightMix: Float) {
    val dm = context.windowManager.windowSize
    window?.setLayout(
        width,
        (dm.heightPixels * heightMix).toInt()
    )
}

fun DialogFragment.setLayout(widthMix: Float, height: Int) {
    dialog?.setLayout(widthMix, height)
}

fun Dialog.setLayout(widthMix: Float, height: Int) {
    val dm = context.windowManager.windowSize
    window?.setLayout(
        (dm.widthPixels * widthMix).toInt(),
        height
    )
}

fun DialogFragment.setLayout(width: Int, height: Int) {
    dialog?.setLayout(width, height)
}

fun Dialog.setLayout(width: Int, height: Int) {
    window?.setLayout(width, height)
}