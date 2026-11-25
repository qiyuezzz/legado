package io.legado.app.ui.book.bookmark

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor
import io.legado.app.utils.spToPx
import kotlin.math.min

class BookmarkDecoration(
    context: Context,
    private val adapter: BookmarkAdapter
) : RecyclerView.ItemDecoration() {

    private val horizontalPadding = 16.dpToPx()
    private val cardGap = 8.dpToPx()
    private val groupGap = 12.dpToPx()
    private val bottomEdge = 12.dpToPx()
    private val headerHeight = 40.dpToPx()

    private val headerPaint = Paint().apply {
        color = if (AppConfig.isEInkMode) Color.WHITE else context.getCompatColor(R.color.surface_canvas)
    }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f.spToPx()
        typeface = Typeface.DEFAULT_BOLD
        color = if (AppConfig.isEInkMode) Color.BLACK else context.getCompatColor(R.color.secondaryText)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val count = parent.childCount
        for (i in 0 until count) {
            val view = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(view)
            if (pos != RecyclerView.NO_POSITION && adapter.isItemHeader(pos)) {
                val top = (view.top - headerHeight).toFloat()
                drawHeader(c, parent.width, top, adapter.getHeaderText(pos))
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (adapter.itemCount == 0) return
        val lm = parent.layoutManager as? LinearLayoutManager ?: return
        val firstPos = lm.findFirstVisibleItemPosition()
        if (firstPos == RecyclerView.NO_POSITION || firstPos >= adapter.itemCount) return
        val firstView = parent.findViewHolderForAdapterPosition(firstPos)?.itemView ?: return
        val nextIsHeader = firstPos + 1 < adapter.itemCount && adapter.isItemHeader(firstPos + 1)
        var headerTop = 0f
        if (nextIsHeader) {
            val nextView = parent.findViewHolderForAdapterPosition(firstPos + 1)?.itemView
            val nextHeaderTop = nextView?.let { (it.top - headerHeight).toFloat() }
                ?: (firstView.bottom + groupGap).toFloat()
            headerTop = min(0f, nextHeaderTop - headerHeight)
        }
        val saveCount = c.save()
        c.clipRect(0f, 0f, parent.width.toFloat(), headerTop + headerHeight)
        drawHeader(c, parent.width, headerTop, adapter.getHeaderText(firstPos))
        c.restoreToCount(saveCount)
    }

    private fun drawHeader(c: Canvas, width: Int, top: Float, text: String) {
        c.drawRect(0f, top, width.toFloat(), top + headerHeight, headerPaint)
        val maxWidth = maxOf(0f, width - horizontalPadding * 2f)
        val ellipsized = TextUtils.ellipsize(text, textPaint, maxWidth, TextUtils.TruncateAt.END).toString()
        val fm = textPaint.fontMetrics
        val baseline = top + (headerHeight - fm.ascent - fm.descent) / 2f
        c.drawText(ellipsized, horizontalPadding.toFloat(), baseline, textPaint)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        outRect.left = horizontalPadding
        outRect.right = horizontalPadding
        outRect.top = if (adapter.isItemHeader(position)) headerHeight else 0
        val isLast = position == adapter.itemCount - 1
        val nextIsHeader = !isLast && adapter.isItemHeader(position + 1)
        outRect.bottom = when {
            isLast -> bottomEdge
            nextIsHeader -> groupGap
            else -> cardGap
        }
    }

}
