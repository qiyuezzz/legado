package io.legado.app.lib.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.core.graphics.ColorUtils.calculateLuminance
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import io.legado.app.R
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import splitties.init.appCtx
import kotlin.math.ceil

internal val dialogCornerRadius get() =
    splitties.init.appCtx.resources.getDimension(R.dimen.floating_panel_corner_radius)

internal fun Window.showBottomPanel(root: View, height: Int = LayoutParams.WRAP_CONTENT) {
    clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    setBackgroundDrawableResource(android.R.color.transparent)
    decorView.setPadding(0, 0, 0, 0)
    setGravity(Gravity.BOTTOM)
    setLayout(LayoutParams.MATCH_PARENT, height)
    root.clipToOutline = false
    (root.background as? ColorDrawable)?.let {
        setBackgroundDrawable(ColorDrawable(it.color))
        navigationBarColor = it.color
        WindowInsetsControllerCompat(this, decorView).isAppearanceLightNavigationBars =
            ColorUtils.isColorLight(it.color)
    }
}

internal fun dialogPill(fill: Int, active: Int) =
    actionBackground(fill, active, appCtx.resources.getDimensionPixelSize(R.dimen.corner_radius_pill))

private fun actionBackground(fill: Int, active: Int, radius: Int) = Selector.shapeBuild()
    .setCornerRadius(radius)
    .setDefaultBgColor(fill)
    .setFocusedBgColor(active)
    .setPressedBgColor(active)
    .setCheckedBgColor(active)
    .setSelectedBgColor(active)
    .create()

internal fun View.setDialogActionBackground(surface: Int, ink: Int) {
    val isLightSurface = calculateLuminance(surface) >= 0.179
    val fill = if (isLightSurface) Color.parseColor("#F1F3F6") else Color.parseColor("#2A2D35")
    val activeFill = if (isLightSurface) Color.parseColor("#E5E8EE") else Color.parseColor("#353944")
    stateListAnimator = null
    elevation = 0f
    translationZ = 0f
    backgroundTintList = null
    background = actionBackground(
        fill,
        activeFill,
        resources.getDimensionPixelSize(R.dimen.corner_radius_control)
    )
}

internal fun ViewGroup.applyReadingCardColors() {
    val surface = context.readingSurfaceColor ?: return
    val ink = context.overlayTextColor
    val card = background?.mutate() as? RippleDrawable ?: return
    val fill = card.findDrawableByLayerId(android.R.id.background) as? GradientDrawable ?: return
    fill.setColor(blendARGB(surface, ink, 0.06f))
    card.setColor(ColorStateList.valueOf(blendARGB(surface, ink, 0.13f)))
    children.filterIsInstance<TextView>().forEach { it.setTextColor(ink) }
}

internal fun Button.styleDialogAction(surface: Int, accent: Int, isPrimary: Boolean) {
    val isLightSurface = calculateLuminance(surface) >= 0.179
    val radius = 10.dpToPx()
    val fill: Int
    val activeFill: Int
    val textColor: Int
    if (isPrimary) {
        fill = accent
        activeFill = ColorUtils.darkenColor(accent)
        textColor = if (ColorUtils.isColorLight(accent)) Color.BLACK else Color.WHITE
    } else {
        fill = if (isLightSurface) Color.parseColor("#F1F3F6") else Color.parseColor("#2A2D35")
        activeFill = if (isLightSurface) Color.parseColor("#E5E8EE") else Color.parseColor("#353944")
        textColor = if (isLightSurface) Color.parseColor("#4E5460") else Color.parseColor("#D0D4DC")
    }
    val shape = Selector.shapeBuild()
        .setCornerRadius(radius)
        .setDefaultBgColor(fill)
        .setPressedBgColor(activeFill)
        .setFocusedBgColor(activeFill)

    stateListAnimator = null
    elevation = 0f
    translationZ = 0f
    backgroundTintList = null
    isAllCaps = false
    background = InsetDrawable(shape.create(), 5.dpToPx(), 4.dpToPx(), 5.dpToPx(), 4.dpToPx())
    setTextColor(textColor)
    minimumHeight = 40.dpToPx()
    minHeight = 40.dpToPx()
    setPaddingRelative(18.dpToPx(), 8.dpToPx(), 18.dpToPx(), 8.dpToPx())
}

internal fun Button.styleConfirmationAction(surface: Int, isPositive: Boolean) {
    val ink = if (calculateLuminance(surface) >= 0.179) Color.BLACK else Color.WHITE
    val fill = if (isPositive) ink else surface
    val activeFill = if (isPositive) {
        blendARGB(ink, surface, 0.06f)
    } else {
        blendARGB(surface, ink, 0.12f)
    }
    val shape = Selector.shapeBuild()
        .setCornerRadius(24.dpToPx())
        .setDefaultBgColor(fill)
        .setPressedBgColor(activeFill)
        .setFocusedBgColor(activeFill)
    if (!isPositive) {
        shape.setStrokeWidth(1.dpToPx())
            .setDefaultStrokeColor(blendARGB(surface, ink, 0.55f))
    }

    stateListAnimator = null
    elevation = 0f
    translationZ = 0f
    backgroundTintList = null
    background = InsetDrawable(shape.create(), 4.dpToPx(), 3.dpToPx(), 4.dpToPx(), 3.dpToPx())
    setTextColor(if (isPositive) surface else ink)
    minimumHeight = 48.dpToPx()
    setPaddingRelative(20.dpToPx(), 10.dpToPx(), 20.dpToPx(), 10.dpToPx())
}

internal fun PopupWindow.styleFloatingPanel() {
    contentView.setFloatingPanelBackground(contentView.context.overlaySurfaceColor)
    elevation = contentView.resources.getDimension(R.dimen.floating_panel_elevation)
}

internal fun View.setFloatingPanelBackground(surface: Int) {
    background = GradientDrawable().apply {
        setColor(surface)
        cornerRadius = resources.getDimension(R.dimen.corner_radius_surface)
    }
    clipToOutline = true
}

internal fun View.setTopRoundedPanelBackground(surface: Int) {
    val radius = resources.getDimension(R.dimen.corner_radius_surface)
    background = GradientDrawable().apply {
        setColor(surface)
        cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
    }
    outlineProvider = TopRoundedPanelOutline
    clipToOutline = true
}

private object TopRoundedPanelOutline : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val radius = view.resources.getDimension(R.dimen.corner_radius_surface)
        // Extending the outline keeps both bottom corners square and supports clipping on API 21.
        outline.setRoundRect(0, 0, view.width, view.height + ceil(radius).toInt(), radius)
    }
}
