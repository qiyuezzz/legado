package io.legado.app.lib.theme.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import androidx.core.view.ViewCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.ShapeAppearanceModel
import io.legado.app.R
import io.legado.app.databinding.ViewNavigationBadgeBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getSecondaryTextColor
import io.legado.app.ui.widget.text.BadgeView
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx

class ThemeBottomNavigationVIew(context: Context, attrs: AttributeSet) :
    BottomNavigationView(context, attrs) {

    init {
        val bgColor = if (AppConfig.isEInkMode) {
            Color.WHITE
        } else {
            ContextCompat.getColor(context, R.color.surface_card)
        }
        setBackgroundColor(bgColor)
        val textIsDark = ColorUtils.isColorLight(bgColor)
        val textColor = context.getSecondaryTextColor(textIsDark)
        val accent = ThemeStore.accentColor(context)
        val colorStateList = Selector.colorBuild()
            .setDefaultColor(textColor)
            .setSelectedColor(accent).create()
        itemIconTintList = colorStateList
        itemTextColor = colorStateList

        itemActiveIndicatorWidth = 52.dpToPx()
        itemActiveIndicatorHeight = 32.dpToPx()
        itemActiveIndicatorShapeAppearance = ShapeAppearanceModel.builder()
            .setAllCornerSizes(16.dpToPx().toFloat())
            .build()
        isItemActiveIndicatorEnabled = true

        if (AppConfig.isEInkMode) {
            isItemHorizontalTranslationEnabled = false
            itemBackground = ColorDrawable(Color.TRANSPARENT)
            itemActiveIndicatorColor = ColorStateList.valueOf(Color.BLACK)
            itemIconTintList = Selector.colorBuild()
                .setDefaultColor(Color.BLACK)
                .setSelectedColor(Color.WHITE)
                .setCheckedColor(Color.WHITE)
                .create()
        } else {
            itemActiveIndicatorColor = ColorStateList.valueOf(
                AndroidColorUtils.setAlphaComponent(accent, 0x1E)
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(this, null)
    }

    fun addBadgeView(index: Int): BadgeView {
        //获取底部菜单view
        val menuView = getChildAt(0) as ViewGroup
        //获取第index个itemView
        val itemView = menuView.getChildAt(index) as ViewGroup
        val badgeBinding = ViewNavigationBadgeBinding.inflate(LayoutInflater.from(context))
        itemView.addView(badgeBinding.root)
        return badgeBinding.viewBadge
    }

}