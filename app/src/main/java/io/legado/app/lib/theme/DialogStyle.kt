package io.legado.app.lib.theme

import io.legado.app.R
import android.widget.PopupWindow
import android.graphics.drawable.GradientDrawable
import android.graphics.Outline
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.ColorUtils.blendARGB
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.dpToPx

internal val dialogCornerRadius get() =
    splitties.init.appCtx.resources.getDimension(R.dimen.floating_panel_corner_radius)

private val bottomPanelOutline = object : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val radius = dialogCornerRadius
        outline.setRoundRect(0, 0, view.width, view.height + radius.toInt(), radius)
    }
}

internal fun Window.showBottomPanel(root: View, height: Int = LayoutParams.WRAP_CONTENT) {
    clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    setBackgroundDrawableResource(android.R.color.transparent)
    decorView.setPadding(0, 0, 0, 0)
    setGravity(Gravity.BOTTOM)
    setLayout(LayoutParams.MATCH_PARENT, height)
    if (!AppConfig.isEInkMode) {
        root.outlineProvider = bottomPanelOutline
        root.clipToOutline = true
    }
}

internal fun dialogPill(fill: Int, active: Int) = Selector.shapeBuild()
    .setCornerRadius(24.dpToPx())
    .setDefaultBgColor(fill)
    .setFocusedBgColor(active)
    .setPressedBgColor(active)
    .setCheckedBgColor(active)
    .setSelectedBgColor(active)
    .create()

internal fun View.setDialogActionBackground(surface: Int, ink: Int) {
    background = dialogPill(blendARGB(surface, ink, 0.07f), blendARGB(surface, ink, 0.19f))
}

internal fun PopupWindow.styleFloatingPanel() {
    contentView.setBackgroundResource(R.drawable.bg_floating_panel)
    contentView.clipToOutline = true
    elevation = contentView.resources.getDimension(R.dimen.floating_panel_elevation)
}

internal fun View.setFloatingPanelBackground(surface: Int) {
    background = GradientDrawable().apply {
        setColor(surface)
        cornerRadius = dialogCornerRadius
    }
    clipToOutline = true
}
