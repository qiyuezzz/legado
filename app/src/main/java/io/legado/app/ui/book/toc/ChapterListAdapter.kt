package io.legado.app.ui.book.toc

import android.content.Context
import android.graphics.Typeface
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.core.view.isGone
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import io.legado.app.R
import io.legado.app.base.adapter.DiffRecyclerAdapter
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookChapter
import io.legado.app.databinding.ItemChapterListBinding
import io.legado.app.help.book.ContentProcessor
import io.legado.app.help.config.AppConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.ThemeUtils
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.ui.book.read.config.readingPanelColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor
import io.legado.app.utils.gone
import io.legado.app.utils.longToastOnUi
import io.legado.app.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import java.util.concurrent.ConcurrentHashMap

class ChapterListAdapter(context: Context, val callback: Callback) :
    DiffRecyclerAdapter<BookChapter, ItemChapterListBinding>(context) {

    val cacheFileNames = hashSetOf<String>()
    private val displayTitleMap = ConcurrentHashMap<String, String>()
    private val handler = Handler(Looper.getMainLooper())

    override val diffItemCallback: DiffUtil.ItemCallback<BookChapter>
        get() = object : DiffUtil.ItemCallback<BookChapter>() {

            override fun areItemsTheSame(
                oldItem: BookChapter,
                newItem: BookChapter
            ): Boolean {
                return oldItem.index == newItem.index
            }

            override fun areContentsTheSame(
                oldItem: BookChapter,
                newItem: BookChapter
            ): Boolean {
                return oldItem.bookUrl == newItem.bookUrl
                        && oldItem.url == newItem.url
                        && oldItem.isVip == newItem.isVip
                        && oldItem.isPay == newItem.isPay
                        && oldItem.title == newItem.title
                        && oldItem.tag == newItem.tag
                        && oldItem.wordCount == newItem.wordCount
                        && oldItem.isVolume == newItem.isVolume
            }

        }

    private var upDisplayTileJob: Coroutine<*>? = null
    private var titleRange = IntRange.EMPTY

    override fun onCurrentListChanged() {
        super.onCurrentListChanged()
        callback.onListChanged()
    }

    fun clearDisplayTitle() {
        upDisplayTileJob?.cancel()
        titleRange = IntRange.EMPTY
        displayTitleMap.clear()
    }

    fun upDisplayTitles(startIndex: Int, endIndex: Int = startIndex + 32) {
        val items = getItems()
        if (items.isEmpty()) return
        val from = (startIndex - 8).coerceAtLeast(0)
        val end = (maxOf(endIndex, startIndex + 16) + 8).coerceAtMost(items.size)
        if (from >= end) return
        if (upDisplayTileJob?.isActive == true && from in titleRange && end - 1 in titleRange) return
        val missing = (from until end).filter { displayTitleMap[items[it].title] == null }
        if (missing.isEmpty()) return
        upDisplayTileJob?.cancel()
        titleRange = from until end
        upDisplayTileJob = Coroutine.async(callback.scope) {
            val book = callback.book ?: return@async
            val useReplace = AppConfig.tocUiUseReplace && book.getUseReplaceRule()
            val replaceRules = if (useReplace) {
                ContentProcessor.get(book.name, book.origin).getTitleReplaceRules()
            } else null
            val changed = arrayListOf<Int>()
            for (index in missing) {
                ensureActive()
                val item = items[index]
                val displayTitle = item.getDisplayTitle(replaceRules, useReplace)
                ensureActive()
                displayTitleMap[item.title] = displayTitle
                if (displayTitle != item.title) changed.add(index)
            }
            if (changed.isNotEmpty()) handler.post {
                if (getItems() === items) {
                    changed.forEach { notifyItemChanged(it, true) }
                }
            }
        }
    }

    private fun getDisplayTitle(chapter: BookChapter): String {
        return displayTitleMap[chapter.title] ?: chapter.title
    }

    override fun getViewBinding(parent: ViewGroup): ItemChapterListBinding {
        return ItemChapterListBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemChapterListBinding,
        item: BookChapter,
        payloads: MutableList<Any>
    ) {
        binding.run {
            val isDur = callback.durChapterIndex() == item.index
            val cached = callback.isLocalBook
                    || item.isVolume
                    || cacheFileNames.contains(item.getFileName())
            if (payloads.isEmpty()) {
                if (isDur) {
                    tvChapterName.setTextColor(context.accentColor)
                } else {
                    tvChapterName.setTextColor(context.getCompatColor(R.color.primaryText))
                }
                tvChapterName.text = getDisplayTitle(item)
                if (item.isVolume) {
                    //卷名，如第一卷 突出显示
                    tvChapterItem.setBackgroundColor(context.getCompatColor(R.color.btn_bg_press))
                } else {
                    //普通章节 保持不变
                    tvChapterItem.background =
                        ThemeUtils.resolveDrawable(context, android.R.attr.selectableItemBackground)
                }

                //卷名不显示
                if (!item.tag.isNullOrEmpty() && !item.isVolume) {
                    //更新时间规则
                    tvTag.text = item.tag
                    tvTag.visible()
                } else {
                    tvTag.gone()
                }
                if (AppConfig.tocCountWords && !item.wordCount.isNullOrEmpty() && !item.isVolume) {
                    //章节字数
                    tvWordCount.text = item.wordCount
                    tvWordCount.visible()
                } else {
                    tvWordCount.gone()
                }

                if (item.isVip && !item.isPay) {
                    ivLocked.visible()
                } else {
                    ivLocked.gone()
                }

                upHasCache(binding, isDur, cached)
            } else {
                tvChapterName.text = getDisplayTitle(item)
                upHasCache(binding, isDur, cached)
            }
            if (callback.isReadingPanel) {
                val paper = context.readingPanelColor
                val ink = context.getPrimaryTextColor(ColorUtils.isColorLight(paper))
                val soft = blendARGB(paper, ink, 0.06f)
                tvChapterItem.background = Selector.shapeBuild()
                    .setDefaultBgColor(if (item.isVolume) soft else paper)
                    .setPressedBgColor(soft)
                    .create()
                tvChapterItem.setPadding(20.dpToPx(), 16.dpToPx(), 20.dpToPx(), 16.dpToPx())
                tvChapterName.textSize = 16f
                tvChapterName.setTextColor(ink)
                tvChapterName.setTypeface(null, if (isDur || item.isVolume) Typeface.BOLD else Typeface.NORMAL)
                tvTag.gone()
                tvWordCount.text = context.getString(R.string.reading_chapter_marker)
                tvWordCount.setTextColor(context.accentColor)
                tvWordCount.isGone = !isDur
                ivChecked.setColorFilter(if (isDur) context.accentColor else ColorUtils.withAlpha(ink, 0.45f))
            }
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemChapterListBinding) {
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                callback.openChapter(it)
            }
        }
        holder.itemView.setOnLongClickListener {
            getItem(holder.layoutPosition)?.let { item ->
                context.longToastOnUi(getDisplayTitle(item))
            }
            true
        }
    }

    private fun upHasCache(binding: ItemChapterListBinding, isDur: Boolean, cached: Boolean) =
        binding.apply {
            ivChecked.setImageResource(R.drawable.ic_outline_cloud_24)
            ivChecked.visible(!cached)
            if (isDur) {
                ivChecked.setImageResource(R.drawable.ic_check)
                ivChecked.visible()
            }
        }

    interface Callback {
        val scope: CoroutineScope
        val book: Book?
        val isLocalBook: Boolean
        val isReadingPanel: Boolean get() = false
        fun openChapter(bookChapter: BookChapter)
        fun durChapterIndex(): Int
        fun onListChanged()
    }

}
