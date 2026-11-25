package io.legado.app.lib.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
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
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor
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

internal fun dialogPill(fill: Int, active: Int): Drawable {
    val radius = appCtx.resources.getDimensionPixelSize(R.dimen.corner_radius_pill)
    if (AppConfig.isEInkMode) {
        return if (fill == Color.TRANSPARENT) {
            Selector.shapeBuild()
                .setCornerRadius(radius)
                .setDefaultBgColor(Color.TRANSPARENT)
                .setFocusedBgColor(Color.BLACK)
                .setPressedBgColor(Color.BLACK)
                .setCheckedBgColor(Color.BLACK)
                .setSelectedBgColor(Color.BLACK)
                .create()
        } else {
            Selector.shapeBuild()
                .setCornerRadius(radius)
                .setDefaultBgColor(Color.WHITE)
                .setFocusedBgColor(Color.LTGRAY)
                .setPressedBgColor(Color.LTGRAY)
                .setCheckedBgColor(Color.BLACK)
                .setSelectedBgColor(Color.BLACK)
                .setStrokeWidth(1.dpToPx())
                .setDefaultStrokeColor(Color.BLACK)
                .create()
        }
    }
    return actionBackground(fill, active, radius)
}

private fun actionBackground(fill: Int, active: Int, radius: Int) = Selector.shapeBuild()
    .setCornerRadius(radius)
    .setDefaultBgColor(fill)
    .setFocusedBgColor(active)
    .setPressedBgColor(active)
    .setCheckedBgColor(active)
    .setSelectedBgColor(active)
    .create()

internal fun View.setDialogActionBackground(surface: Int, ink: Int) {
    val radius = resources.getDimensionPixelSize(R.dimen.corner_radius_control)
    stateListAnimator = null
    elevation = 0f
    translationZ = 0f
    backgroundTintList = null
    if (AppConfig.isEInkMode) {
        background = Selector.shapeBuild()
            .setCornerRadius(radius)
            .setDefaultBgColor(Color.WHITE)
            .setFocusedBgColor(Color.LTGRAY)
            .setPressedBgColor(Color.LTGRAY)
            .setCheckedBgColor(Color.LTGRAY)
            .setSelectedBgColor(Color.LTGRAY)
            .setStrokeWidth(1.dpToPx())
            .setDefaultStrokeColor(Color.BLACK)
            .create()
        return
    }
    val isLightSurface = calculateLuminance(surface) >= 0.179
    val fill = if (isLightSurface) Color.parseColor("#F1F3F6") else blendARGB(surface, ink, 0.07f)
    val activeFill = if (isLightSurface) Color.parseColor("#E5E8EE") else blendARGB(surface, ink, 0.16f)
    background = actionBackground(fill, activeFill, radius)
}

internal fun ViewGroup.applyReadingCardColors() {
    val surface = context.readingSurfaceColor ?: return
    val ink = context.overlayTextColor
    val card = background?.mutate() as? RippleDrawable ?: return
    val fill = card.findDrawableByLayerId(android.R.id.background) as? GradientDrawable ?: return
    if (AppConfig.isEInkMode) {
        fill.setColor(Color.WHITE)
        fill.setStroke(1.dpToPx(), Color.BLACK)
        card.setColor(ColorStateList.valueOf(Color.LTGRAY))
        children.filterIsInstance<TextView>().forEach { it.setTextColor(Color.BLACK) }
        return
    }
    fill.setColor(blendARGB(surface, ink, 0.06f))
    card.setColor(ColorStateList.valueOf(blendARGB(surface, ink, 0.13f)))
    children.filterIsInstance<TextView>().forEach { it.setTextColor(ink) }
}

internal enum class ActionButtonRole {
    PRIMARY,
    SECONDARY,
    DANGER,
    SUBTLE
}

internal fun TextView.styleActionButton(
    surface: Int,
    role: ActionButtonRole,
    cornerRadiusPx: Int = 24.dpToPx(),
    minHeightPx: Int = 44.dpToPx(),
    horizontalPaddingPx: Int = 20.dpToPx(),
    verticalPaddingPx: Int = 10.dpToPx(),
    insetHorizontalPx: Int = 0,
    insetVerticalPx: Int = 0
) {
    val isEInk = AppConfig.isEInkMode
    val actualSurface = if (isEInk) Color.WHITE else surface
    val isLightSurface = calculateLuminance(actualSurface) >= 0.179
    val ink = if (isEInk) Color.BLACK else if (isLightSurface) Color.BLACK else Color.WHITE
    val danger = if (isEInk) Color.BLACK else context.getCompatColor(R.color.error)

    val fill: Int
    val activeFill: Int
    val textColor: Int
    var strokeColor: Int? = null

    when (role) {
        ActionButtonRole.PRIMARY -> {
            fill = ink
            activeFill = if (isEInk) Color.DKGRAY else blendARGB(ink, actualSurface, 0.10f)
            textColor = actualSurface
        }

        ActionButtonRole.SECONDARY -> {
            fill = actualSurface
            activeFill = if (isEInk) Color.LTGRAY else blendARGB(actualSurface, ink, 0.10f)
            textColor = ink
            strokeColor = if (isEInk) Color.BLACK else blendARGB(actualSurface, ink, 0.32f)
        }

        ActionButtonRole.DANGER -> {
            if (isEInk) {
                fill = Color.WHITE
                activeFill = Color.LTGRAY
                textColor = Color.BLACK
                strokeColor = Color.BLACK
            } else {
                fill = blendARGB(actualSurface, danger, 0.12f)
                activeFill = blendARGB(actualSurface, danger, 0.22f)
                textColor = danger
            }
        }

        ActionButtonRole.SUBTLE -> {
            if (isEInk) {
                fill = Color.WHITE
                activeFill = Color.LTGRAY
                textColor = Color.BLACK
                strokeColor = Color.BLACK
            } else {
                fill = blendARGB(actualSurface, ink, 0.07f)
                activeFill = blendARGB(actualSurface, ink, 0.16f)
                textColor = ink
            }
        }
    }

    val shape = Selector.shapeBuild()
        .setCornerRadius(cornerRadiusPx)
        .setDefaultBgColor(fill)
        .setPressedBgColor(activeFill)
        .setFocusedBgColor(activeFill)
    strokeColor?.let {
        shape.setStrokeWidth(1.dpToPx()).setDefaultStrokeColor(it)
    }

    stateListAnimator = null
    elevation = 0f
    translationZ = 0f
    backgroundTintList = null
    isAllCaps = false
    val drawable = shape.create()
    background = if (insetHorizontalPx > 0 || insetVerticalPx > 0) {
        InsetDrawable(drawable, insetHorizontalPx, insetVerticalPx, insetHorizontalPx, insetVerticalPx)
    } else {
        drawable
    }
    setTextColor(textColor)
    setTag(R.id.ll_dialog_footer, true)
    minimumHeight = minHeightPx
    minHeight = minHeightPx
    setPaddingRelative(horizontalPaddingPx, verticalPaddingPx, horizontalPaddingPx, verticalPaddingPx)
}

internal val View.isStyledActionButton: Boolean
    get() = getTag(R.id.ll_dialog_footer) == true

internal fun Button.styleDialogAction(surface: Int, accent: Int, isPrimary: Boolean) {
    styleActionButton(
        surface = surface,
        role = if (isPrimary) ActionButtonRole.PRIMARY else ActionButtonRole.SECONDARY,
        insetHorizontalPx = 4.dpToPx(),
        insetVerticalPx = 3.dpToPx()
    )
}

internal fun Button.styleConfirmationAction(surface: Int, isPositive: Boolean) {
    styleActionButton(
        surface = surface,
        role = if (isPositive) ActionButtonRole.PRIMARY else ActionButtonRole.SECONDARY,
        minHeightPx = 48.dpToPx(),
        insetHorizontalPx = 4.dpToPx(),
        insetVerticalPx = 3.dpToPx()
    )
}

internal fun PopupWindow.styleFloatingPanel() {
    contentView.setFloatingPanelBackground(contentView.context.overlaySurfaceColor)
    if (AppConfig.isEInkMode) {
        animationStyle = 0
        elevation = 0f
    } else {
        elevation = contentView.resources.getDimension(R.dimen.floating_panel_elevation)
    }
}

internal fun View.setFloatingPanelBackground(surface: Int) {
    background = GradientDrawable().apply {
        setColor(if (AppConfig.isEInkMode) Color.WHITE else surface)
        cornerRadius = resources.getDimension(R.dimen.corner_radius_surface)
        if (AppConfig.isEInkMode) {
            setStroke(1.dpToPx(), Color.BLACK)
        }
    }
    clipToOutline = true
}

internal fun View.setTopRoundedPanelBackground(surface: Int) {
    val radius = resources.getDimension(R.dimen.corner_radius_surface)
    val shape = GradientDrawable().apply {
        setColor(if (AppConfig.isEInkMode) Color.WHITE else surface)
        cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
        if (AppConfig.isEInkMode) {
            setStroke(1.dpToPx(), Color.BLACK)
        }
    }
    background = if (AppConfig.isEInkMode) {
        val stroke = 1.dpToPx()
        LayerDrawable(arrayOf(shape)).apply {
            setLayerInset(0, -stroke, 0, -stroke, -stroke)
        }
    } else {
        shape
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
