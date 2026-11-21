package io.legado.app.ui.widget.recycler

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.utils.dpToPx

class CardSpacingItemDecoration(
    private val horizontalDp: Int = 16,
    private val verticalDp: Int = 8,
    private val edgeVerticalDp: Int = 12
) : RecyclerView.ItemDecoration() {

    private val horizontalPx = horizontalDp.dpToPx()
    private val verticalPx = verticalDp.dpToPx()
    private val edgeVerticalPx = edgeVerticalDp.dpToPx()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val pos = parent.getChildAdapterPosition(view)
        val count = parent.adapter?.itemCount ?: 0
        outRect.left = horizontalPx
        outRect.right = horizontalPx
        outRect.top = if (pos == 0) edgeVerticalPx else 0
        outRect.bottom = if (pos == count - 1) edgeVerticalPx else verticalPx
    }
}
