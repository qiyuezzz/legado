package io.legado.app.lib.prefs.fragment

import android.annotation.SuppressLint
import android.graphics.Canvas
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
import io.legado.app.lib.prefs.EditTextPreferenceDialog
import io.legado.app.lib.prefs.ListPreferenceDialog
import io.legado.app.lib.prefs.MultiSelectListPreferenceDialog
import io.legado.app.lib.theme.readingSurfaceColor
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.dpToPx

abstract class PreferenceFragment : PreferenceFragmentCompat() {

    private val dialogFragmentTag = "androidx.preference.PreferenceFragment.DIALOG"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.clipToPadding = false
        listView.applyNavigationBarPadding()
        if (requireContext().readingSurfaceColor == null) {
            setDivider(null)
            setDividerHeight(0)
            listView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_canvas))
            listView.addItemDecoration(CardGroupItemDecoration())
        }
    }

    @SuppressLint("RestrictedApi")
    private inner class CardGroupItemDecoration : RecyclerView.ItemDecoration() {
        private val horizontalMargin = 16.dpToPx()
        private val cornerRadius = 16.dpToPx().toFloat()
        private val dividerInset = 16.dpToPx()
        private val dividerHeight = 0.5f * resources.displayMetrics.density
        private val groupGap = 10.dpToPx()

        private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(requireContext(), R.color.surface_card)
        }
        private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(requireContext(), R.color.surface_divider)
        }
        private val path = Path()
        private val rectF = RectF()

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
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val pos = parent.getChildAdapterPosition(child)
                if (pos == RecyclerView.NO_POSITION || !isCardItem(adapter, pos)) continue

                val prevIsCard = isCardItem(adapter, pos - 1)
                val nextIsCard = isCardItem(adapter, pos + 1)

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