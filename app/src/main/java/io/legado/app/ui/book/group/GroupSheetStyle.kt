package io.legado.app.ui.book.group

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.overlaySurfaceColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor

internal class GroupSheetPalette(context: Context) {
    val isEInk: Boolean = AppConfig.isEInkMode
    val surface: Int = if (isEInk) Color.WHITE else context.getCompatColor(R.color.surface_card)
    val primaryInk: Int = if (isEInk) Color.BLACK else {
        context.getPrimaryTextColor(ColorUtils.isColorLight(surface))
    }
    val secondaryInk: Int = if (isEInk) Color.BLACK else {
        AndroidColorUtils.setAlphaComponent(primaryInk, 0x99)
    }
    val tertiaryInk: Int = if (isEInk) Color.BLACK else {
        AndroidColorUtils.setAlphaComponent(primaryInk, 0x66)
    }
    val accent: Int = if (isEInk) Color.BLACK else context.accentColor
    val danger: Int = if (isEInk) Color.BLACK else context.getCompatColor(R.color.error)
    val dividerColor: Int = if (isEInk) {
        Color.BLACK
    } else {
        AndroidColorUtils.setAlphaComponent(primaryInk, 0x16)
    }
    val handleColor: Int = if (isEInk) {
        Color.BLACK
    } else {
        AndroidColorUtils.setAlphaComponent(primaryInk, 0x2E)
    }

    fun applySheetSurface(root: View, handle: View, vararg dividers: View) {
        if (!isEInk) {
            val radius = 20.dpToPx().toFloat()
            root.background = GradientDrawable().apply {
                setColor(surface)
                cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            }
            root.clipToOutline = true
        }
        handle.background = GradientDrawable().apply {
            setColor(handleColor)
            cornerRadius = 2.dpToPx().toFloat()
        }
        dividers.forEach { it.setBackgroundColor(dividerColor) }
    }

    fun cardBackground(selected: Boolean = false): Drawable {
        if (isEInk) {
            val stroke = if (selected) 2.dpToPx() else 1.dpToPx()
            return Selector.shapeBuild()
                .setCornerRadius(14.dpToPx())
                .setDefaultBgColor(Color.WHITE)
                .setStrokeWidth(stroke)
                .setDefaultStrokeColor(Color.BLACK)
                .create()
        }
        val fill = if (selected) {
            AndroidColorUtils.blendARGB(surface, accent, 0.10f)
        } else {
            AndroidColorUtils.blendARGB(surface, primaryInk, 0.045f)
        }
        val pressed = if (selected) {
            AndroidColorUtils.blendARGB(surface, accent, 0.18f)
        } else {
            AndroidColorUtils.blendARGB(surface, primaryInk, 0.10f)
        }
        val shape = Selector.shapeBuild()
            .setCornerRadius(14.dpToPx())
            .setDefaultBgColor(fill)
            .setPressedBgColor(pressed)
            .setFocusedBgColor(pressed)
        if (selected) {
            shape.setStrokeWidth(1.dpToPx())
                .setDefaultStrokeColor(AndroidColorUtils.setAlphaComponent(accent, 0x88))
        }
        return shape.create()
    }

    fun sectionCardBackground(): Drawable {
        return GradientDrawable().apply {
            cornerRadius = 14.dpToPx().toFloat()
            if (isEInk) {
                setColor(Color.WHITE)
                setStroke(1.dpToPx(), Color.BLACK)
            } else {
                setColor(AndroidColorUtils.blendARGB(surface, primaryInk, 0.045f))
            }
        }
    }

    fun inputBackground(): Drawable {
        return GradientDrawable().apply {
            cornerRadius = 12.dpToPx().toFloat()
            if (isEInk) {
                setColor(Color.WHITE)
                setStroke(1.dpToPx(), Color.BLACK)
            } else {
                setColor(AndroidColorUtils.blendARGB(surface, primaryInk, 0.045f))
                setStroke(1.dpToPx(), AndroidColorUtils.setAlphaComponent(primaryInk, 0x22))
            }
        }
    }

    fun badgeBackground(): Drawable {
        return GradientDrawable().apply {
            cornerRadius = 6.dpToPx().toFloat()
            if (isEInk) {
                setColor(Color.WHITE)
                setStroke(1.dpToPx(), Color.BLACK)
            } else {
                setColor(AndroidColorUtils.blendARGB(surface, primaryInk, 0.09f))
            }
        }
    }

    fun subtlePillBackground(): Drawable {
        if (isEInk) {
            return Selector.shapeBuild()
                .setCornerRadius(18.dpToPx())
                .setDefaultBgColor(Color.WHITE)
                .setStrokeWidth(1.dpToPx())
                .setDefaultStrokeColor(Color.BLACK)
                .create()
        }
        return Selector.shapeBuild()
            .setCornerRadius(18.dpToPx())
            .setDefaultBgColor(AndroidColorUtils.blendARGB(surface, accent, 0.12f))
            .setPressedBgColor(AndroidColorUtils.blendARGB(surface, accent, 0.22f))
            .setFocusedBgColor(AndroidColorUtils.blendARGB(surface, accent, 0.22f))
            .create()
    }

    fun dangerPillBackground(): Drawable {
        if (isEInk) {
            return Selector.shapeBuild()
                .setCornerRadius(18.dpToPx())
                .setDefaultBgColor(Color.WHITE)
                .setStrokeWidth(1.dpToPx())
                .setDefaultStrokeColor(Color.BLACK)
                .create()
        }
        return Selector.shapeBuild()
            .setCornerRadius(18.dpToPx())
            .setDefaultBgColor(AndroidColorUtils.blendARGB(surface, danger, 0.10f))
            .setPressedBgColor(AndroidColorUtils.blendARGB(surface, danger, 0.20f))
            .setFocusedBgColor(AndroidColorUtils.blendARGB(surface, danger, 0.20f))
            .create()
    }

    fun iconButtonBackground(): Drawable {
        if (isEInk) {
            return Selector.shapeBuild()
                .setCornerRadius(18.dpToPx())
                .setDefaultBgColor(Color.TRANSPARENT)
                .create()
        }
        return Selector.shapeBuild()
            .setCornerRadius(18.dpToPx())
            .setDefaultBgColor(Color.TRANSPARENT)
            .setPressedBgColor(AndroidColorUtils.blendARGB(surface, primaryInk, 0.12f))
            .setFocusedBgColor(AndroidColorUtils.blendARGB(surface, primaryInk, 0.12f))
            .create()
    }

    fun primaryButtonBackground(): Drawable {
        val fill = primaryInk
        val active = AndroidColorUtils.blendARGB(primaryInk, surface, 0.12f)
        return Selector.shapeBuild()
            .setCornerRadius(24.dpToPx())
            .setDefaultBgColor(fill)
            .setPressedBgColor(active)
            .setFocusedBgColor(active)
            .create()
    }

    fun secondaryButtonBackground(): Drawable {
        val active = AndroidColorUtils.blendARGB(surface, primaryInk, 0.10f)
        val border = if (isEInk) Color.BLACK else AndroidColorUtils.setAlphaComponent(primaryInk, 0x38)
        return Selector.shapeBuild()
            .setCornerRadius(24.dpToPx())
            .setDefaultBgColor(surface)
            .setPressedBgColor(active)
            .setFocusedBgColor(active)
            .setStrokeWidth(1.dpToPx())
            .setDefaultStrokeColor(border)
            .create()
    }
}

internal fun Window.configureGroupBottomSheet(
    context: Context,
    height: Int = LayoutParams.WRAP_CONTENT,
    adjustResize: Boolean = false
) {
    setBackgroundDrawableResource(android.R.color.transparent)
    WindowCompat.setDecorFitsSystemWindows(this, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        attributes = attributes.apply {
            setFitInsetsTypes(if (adjustResize) WindowInsets.Type.ime() else 0)
        }
    }
    decorView.setPadding(0, 0, 0, 0)
    setGravity(Gravity.BOTTOM)
    setLayout(LayoutParams.MATCH_PARENT, height)
    if (adjustResize) {
        @Suppress("DEPRECATION")
        setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
    }
    if (!AppConfig.isEInkMode) {
        val surface = context.getCompatColor(R.color.surface_card)
        @Suppress("DEPRECATION")
        navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(this, decorView).isAppearanceLightNavigationBars =
            ColorUtils.isColorLight(surface)
    }
}
