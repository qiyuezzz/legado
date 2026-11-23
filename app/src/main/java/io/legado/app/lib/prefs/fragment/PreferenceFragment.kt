package io.legado.app.lib.prefs.fragment

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroupAdapter
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.prefs.EditTextPreferenceDialog
import io.legado.app.lib.prefs.ListPreferenceDialog
import io.legado.app.lib.prefs.MultiSelectListPreferenceDialog
import io.legado.app.lib.theme.pageCanvasColor
import io.legado.app.lib.theme.readingSurfaceColor
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.dpToPx

abstract class PreferenceFragment : PreferenceFragmentCompat() {

    private val dialogFragmentTag = "androidx.preference.PreferenceFragment.DIALOG"

    protected open val drawCardBackground: Boolean
        get() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.clipToPadding = false
        if (AppConfig.isEInkMode) {
            listView.itemAnimator = null
        }
        listView.applyNavigationBarPadding()
        if (requireContext().readingSurfaceColor == null) {
            setDivider(null)
            setDividerHeight(0)
            listView.setBackgroundColor(requireContext().pageCanvasColor)
            listView.addItemDecoration(CardGroupItemDecoration())
        }
    }

    @SuppressLint("RestrictedApi")
    private inner class CardGroupItemDecoration : RecyclerView.ItemDecoration() {
        private val isEInk = AppConfig.isEInkMode
        private val horizontalMargin = 16.dpToPx()
        private val cornerRadius = 16.dpToPx().toFloat()
        private val dividerInset = 16.dpToPx()
        private val dividerHeight = if (isEInk) 1.dpToPx().toFloat() else 0.5f * resources.displayMetrics.density
        private val strokeWidth = 1.dpToPx().toFloat()
        private val groupGap = 10.dpToPx()

        private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isEInk) Color.WHITE else ContextCompat.getColor(requireContext(), R.color.surface_card)
        }
        private val cardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = this@CardGroupItemDecoration.strokeWidth
            color = Color.BLACK
        }
        private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isEInk) Color.BLACK else ContextCompat.getColor(requireContext(), R.color.surface_divider)
        }
        private val path = Path()
        private val borderPath = Path()
        private val rectF = RectF()
        private val arcRectF = RectF()

        private fun isCardItem(adapter: PreferenceGroupAdapter, pos: Int): Boolean {
            if (pos < 0 || pos >= adapter.itemCount) return false
            return adapter.getItem(pos) !is PreferenceCategory
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (!drawCardBackground) {
                outRect.set(0, 0, 0, 0)
                return
            }
            val adapter = parent.adapter as? PreferenceGroupAdapter ?: return
            val pos = parent.getChildAdapterPosition(view)
            if (pos == RecyclerView.NO_POSITION) return

            if (!isCardItem(adapter, pos)) {
                outRect.set(0, 0, 0, 0)
                return
            }

            val prevIsCard = isCardItem(adapter, pos - 1)
            val nextIsCard = isCardItem(adapter, pos + 1)
            val top = if (!prevIsCard && pos == 0) groupGap else 0
            val bottom = if (!nextIsCard) groupGap else 0
            outRect.set(horizontalMargin, top, horizontalMargin, bottom)
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val adapter = parent.adapter as? PreferenceGroupAdapter ?: return
            val halfStroke = strokeWidth / 2f
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val pos = parent.getChildAdapterPosition(child)
                if (pos == RecyclerView.NO_POSITION || !isCardItem(adapter, pos)) continue

                val prevIsCard = isCardItem(adapter, pos - 1)
                val nextIsCard = isCardItem(adapter, pos + 1)

                if (drawCardBackground) {
                    val topRadius = if (!prevIsCard) cornerRadius else 0f
                    val bottomRadius = if (!nextIsCard) cornerRadius else 0f
                    val radii = floatArrayOf(
                        topRadius, topRadius,
                        topRadius, topRadius,
                        bottomRadius, bottomRadius,
                        bottomRadius, bottomRadius
                    )

                    rectF.set(
                        child.left.toFloat(),
                        child.top.toFloat(),
                        child.right.toFloat(),
                        child.bottom.toFloat()
                    )
                    path.reset()
                    path.addRoundRect(rectF, radii, Path.Direction.CW)
                    c.drawPath(path, cardPaint)

                    if (isEInk) {
                        val left = child.left + halfStroke
                        val right = child.right - halfStroke
                        val top = child.top + halfStroke
                        val bottom = child.bottom - halfStroke
                        val r = minOf(cornerRadius, (right - left) / 2f, (child.bottom - child.top) / 2f)
                        borderPath.reset()
                        when {
                            !prevIsCard && !nextIsCard -> {
                                rectF.set(left, top, right, bottom)
                                borderPath.addRoundRect(rectF, r, r, Path.Direction.CW)
                            }
                            !prevIsCard && nextIsCard -> {
                                borderPath.moveTo(left, child.bottom.toFloat())
                                borderPath.lineTo(left, top + r)
                                arcRectF.set(left, top, left + 2 * r, top + 2 * r)
                                borderPath.arcTo(arcRectF, 180f, 90f, false)
                                borderPath.lineTo(right - r, top)
                                arcRectF.set(right - 2 * r, top, right, top + 2 * r)
                                borderPath.arcTo(arcRectF, 270f, 90f, false)
                                borderPath.lineTo(right, child.bottom.toFloat())
                            }
                            prevIsCard && !nextIsCard -> {
                                borderPath.moveTo(left, child.top.toFloat())
                                borderPath.lineTo(left, bottom - r)
                                arcRectF.set(left, bottom - 2 * r, left + 2 * r, bottom)
                                borderPath.arcTo(arcRectF, 180f, -90f, false)
                                borderPath.lineTo(right - r, bottom)
                                arcRectF.set(right - 2 * r, bottom - 2 * r, right, bottom)
                                borderPath.arcTo(arcRectF, 90f, -90f, false)
                                borderPath.lineTo(right, child.top.toFloat())
                            }
                            else -> {
                                borderPath.moveTo(left, child.top.toFloat())
                                borderPath.lineTo(left, child.bottom.toFloat())
                                borderPath.moveTo(right, child.top.toFloat())
                                borderPath.lineTo(right, child.bottom.toFloat())
                            }
                        }
                        c.drawPath(borderPath, cardStrokePaint)
                    }
                }

                if (nextIsCard) {
                    c.drawRect(
                        (child.left + dividerInset).toFloat(),
                        child.bottom - dividerHeight,
                        (child.right - dividerInset).toFloat(),
                        child.bottom.toFloat(),
                        dividerPaint
                    )
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onDisplayPreferenceDialog(preference: Preference) {

        var handled = false
        if (callbackFragment is OnPreferenceDisplayDialogCallback) {
            handled =
                (callbackFragment as OnPreferenceDisplayDialogCallback)
                    .onPreferenceDisplayDialog(this, preference)
        }
        if (!handled && activity is OnPreferenceDisplayDialogCallback) {
            handled = (activity as OnPreferenceDisplayDialogCallback)
                .onPreferenceDisplayDialog(this, preference)
        }

        if (handled) {
            return
        }

        // check if dialog is already showing
        if (parentFragmentManager.findFragmentByTag(dialogFragmentTag) != null) {
            return
        }

        val dialogFragment: DialogFragment = when (preference) {
            is EditTextPreference -> {
                EditTextPreferenceDialog.newInstance(preference.getKey())
            }
            is ListPreference -> {
                ListPreferenceDialog.newInstance(preference.getKey())
            }
            is MultiSelectListPreference -> {
                MultiSelectListPreferenceDialog.newInstance(preference.getKey())
            }
            else -> {
                throw IllegalArgumentException(
                    "Cannot display dialog for an unknown Preference type: "
                            + preference.javaClass.simpleName
                            + ". Make sure to implement onPreferenceDisplayDialog() to handle "
                            + "displaying a custom dialog for this Preference."
                )
            }
        }
        @Suppress("DEPRECATION")
        dialogFragment.setTargetFragment(this, 0)

        dialogFragment.show(parentFragmentManager, dialogFragmentTag)
    }

}