package io.legado.app.utils

import android.app.Dialog
import android.graphics.Color
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import androidx.core.view.descendants
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.ActionButtonRole
import io.legado.app.lib.theme.overlaySurfaceColor
import io.legado.app.lib.theme.overlayTextColor
import io.legado.app.lib.theme.readingSurfaceColor
import io.legado.app.lib.theme.styleActionButton
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.applyEInkDialogContrast
import io.legado.app.lib.theme.filletBackground
import splitties.systemservices.windowManager

fun AlertDialog.applyTint(emphasizePositiveAction: Boolean = false): AlertDialog {
    window?.applyEInkDialogContrast()
    window?.setBackgroundDrawable(context.filletBackground.apply {
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
        val isEInkMode = AppConfig.isEInkMode
        val actionSurface = if (isEInkMode) Color.WHITE else paper
        val minHeight = if (emphasizePositiveAction) 48.dpToPx() else 44.dpToPx()
        val deleteLabel = context.getString(R.string.delete)
        val removeLabel = context.getString(R.string.remove_from_bookshelf)
        listOf(AlertDialog.BUTTON_NEGATIVE, AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEUTRAL)
            .forEach { which ->
                getButton(which)?.apply {
                    val role = when (which) {
                        AlertDialog.BUTTON_POSITIVE -> {
                            val label = text?.toString()
                            if (label == deleteLabel || label == removeLabel) {
                                ActionButtonRole.DANGER
                            } else {
                                ActionButtonRole.PRIMARY
                            }
                        }

                        AlertDialog.BUTTON_NEGATIVE -> ActionButtonRole.SECONDARY
                        else -> ActionButtonRole.SUBTLE
                    }
                    styleActionButton(
                        surface = actionSurface,
                        role = role,
                        minHeightPx = minHeight,
                        insetHorizontalPx = 4.dpToPx(),
                        insetVerticalPx = 3.dpToPx()
                    )
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
