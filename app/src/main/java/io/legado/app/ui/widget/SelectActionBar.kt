package io.legado.app.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.widget.FrameLayout
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import io.legado.app.R
import io.legado.app.databinding.ViewSelectActionBarBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.ActionButtonRole
import io.legado.app.lib.theme.dialogPill
import io.legado.app.lib.theme.elevation
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.getSecondaryDisabledTextColor
import io.legado.app.lib.theme.styleActionButton
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.dpToPx
import io.legado.app.utils.visible


@Suppress("unused")
class SelectActionBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val surface = if (AppConfig.isEInkMode) {
        Color.WHITE
    } else {
        ContextCompat.getColor(context, R.color.surface_card)
    }
    private val bgIsLight = ColorUtils.isColorLight(surface)
    private val primaryTextColor = if (AppConfig.isEInkMode) Color.BLACK else context.getPrimaryTextColor(bgIsLight)
    private val disabledColor = if (AppConfig.isEInkMode) Color.BLACK else context.getSecondaryDisabledTextColor(bgIsLight)

    private var callBack: CallBack? = null
    private var selMenu: PopupMenu? = null
    private val binding = ViewSelectActionBarBinding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        if (!isInEditMode) {
            setBackgroundColor(surface)
            elevation = 0f
            binding.cbSelectedAll.setTextColor(primaryTextColor)
            binding.btnRevertSelection.styleActionButton(
                surface = surface,
                role = ActionButtonRole.SUBTLE,
                minHeightPx = 38.dpToPx(),
                horizontalPaddingPx = 14.dpToPx(),
                verticalPaddingPx = 8.dpToPx()
            )
            binding.btnSelectActionMain.styleActionButton(
                surface = surface,
                role = ActionButtonRole.PRIMARY,
                minHeightPx = 38.dpToPx(),
                horizontalPaddingPx = 16.dpToPx(),
                verticalPaddingPx = 8.dpToPx()
            )
            binding.ivMenuMore.background = dialogPill(
                AndroidColorUtils.blendARGB(surface, primaryTextColor, 0.07f),
                AndroidColorUtils.blendARGB(surface, primaryTextColor, 0.16f)
            )
            binding.ivMenuMore.setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_IN)
            binding.cbSelectedAll.setOnUserCheckedChangeListener { isChecked ->
                callBack?.selectAll(isChecked)
            }
            binding.btnRevertSelection.setOnClickListener { callBack?.revertSelection() }
            binding.btnSelectActionMain.setOnClickListener { callBack?.onClickSelectBarMainAction() }
            binding.ivMenuMore.setOnClickListener { selMenu?.show() }
            applyNavigationBarPadding()
        }
    }

    fun setMainActionText(text: String) = binding.run {
        btnSelectActionMain.text = text
        btnSelectActionMain.visible()
    }

    fun setMainActionText(@StringRes id: Int) = binding.run {
        btnSelectActionMain.setText(id)
        btnSelectActionMain.visible()
    }

    fun inflateMenu(@MenuRes resId: Int): Menu? {
        selMenu = PopupMenu(context, binding.ivMenuMore)
        selMenu?.inflate(resId)
        binding.ivMenuMore.visible()
        return selMenu?.menu
    }

    fun setCallBack(callBack: CallBack) {
        this.callBack = callBack
    }

    fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        selMenu?.setOnMenuItemClickListener(listener)
    }

    fun upCountView(selectCount: Int, allCount: Int) = binding.run {
        if (selectCount == 0) {
            cbSelectedAll.isChecked = false
        } else {
            cbSelectedAll.isChecked = selectCount >= allCount
        }

        //重置全选的文字
        if (cbSelectedAll.isChecked) {
            cbSelectedAll.text = context.getString(
                R.string.select_cancel_count,
                selectCount,
                allCount
            )
        } else {
            cbSelectedAll.text = context.getString(
                R.string.select_all_count,
                selectCount,
                allCount
            )
        }
        setMenuClickable(selectCount > 0)
    }

    private fun setMenuClickable(isClickable: Boolean) = binding.run {
        val actionAlpha = if (isClickable) 1f else 0.38f
        btnRevertSelection.isEnabled = isClickable
        btnRevertSelection.isClickable = isClickable
        btnRevertSelection.alpha = actionAlpha
        btnSelectActionMain.isEnabled = isClickable
        btnSelectActionMain.isClickable = isClickable
        btnSelectActionMain.alpha = actionAlpha
        ivMenuMore.setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_IN)
        ivMenuMore.alpha = actionAlpha
        ivMenuMore.isEnabled = isClickable
        ivMenuMore.isClickable = isClickable
    }

    interface CallBack {

        fun selectAll(selectAll: Boolean)

        fun revertSelection()

        fun onClickSelectBarMainAction() {}
    }
}