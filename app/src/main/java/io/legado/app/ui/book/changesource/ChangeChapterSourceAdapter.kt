package io.legado.app.ui.book.changesource

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import io.legado.app.R
import io.legado.app.base.adapter.DiffRecyclerAdapter
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.data.entities.SearchBook
import io.legado.app.databinding.ItemChangeSourceBinding
import io.legado.app.lib.theme.applyReadingCardColors
import io.legado.app.lib.theme.overlayTextColor
import io.legado.app.lib.theme.readingSurfaceColor
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.getCompatColor
import io.legado.app.utils.gone
import io.legado.app.utils.invisible
import io.legado.app.utils.visible
import splitties.views.onLongClick


class ChangeChapterSourceAdapter(
    context: Context,
    val viewModel: ChangeChapterSourceViewModel,
    val callBack: CallBack
) : DiffRecyclerAdapter<SearchBook, ItemChangeSourceBinding>(context) {

    override val diffItemCallback = object : DiffUtil.ItemCallback<SearchBook>() {
        override fun areItemsTheSame(oldItem: SearchBook, newItem: SearchBook): Boolean {
            return oldItem.bookUrl == newItem.bookUrl
        }

        override fun areContentsTheSame(oldItem: SearchBook, newItem: SearchBook): Boolean {
            return oldItem.originName == newItem.originName
                    && oldItem.getDisplayLastChapterTitle() == newItem.getDisplayLastChapterTitle()
        }

    }

    override fun getViewBinding(parent: ViewGroup): ItemChangeSourceBinding {
        return ItemChangeSourceBinding.inflate(inflater, parent, false).also {
            it.root.applyReadingCardColors()
            if (it.root.context.readingSurfaceColor != null) {
                it.ivChecked.setColorFilter(it.root.context.overlayTextColor)
            }
        }
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemChangeSourceBinding,
        item: SearchBook,
        payloads: MutableList<Any>
    ) {
        binding.apply {
            if (payloads.isEmpty()) {
                tvOrigin.text = item.originName
                tvAuthor.text = item.author
                tvLast.text = item.getDisplayLastChapterTitle()
                tvCurrentChapterWordCount.text = item.chapterWordCountText
                tvRespondTime.text = context.getString(R.string.respondTime, item.respondTime)
                if (callBack.oldBookUrl == item.bookUrl) {
                    ivChecked.visible()
                } else {
                    ivChecked.invisible()
                }
            } else {
                for (i in payloads.indices) {
                    val bundle = payloads[i] as Bundle
                    bundle.keySet().forEach {
                        when (it) {
                            "name" -> tvOrigin.text = item.originName
                            "latest" -> tvLast.text = item.getDisplayLastChapterTitle()
                            "upCurSource" -> if (callBack.oldBookUrl == item.bookUrl) {
                                ivChecked.visible()
                            } else {
                                ivChecked.invisible()
                            }
                        }
                    }
                }
            }
            applyScoreStyle(binding, callBack.getBookScore(item))

            if (AppConfig.changeSourceLoadWordCount && !item.chapterWordCountText.isNullOrBlank()) {
                tvCurrentChapterWordCount.visible()
            } else {
                tvCurrentChapterWordCount.gone()
            }

            if (AppConfig.changeSourceLoadWordCount && item.respondTime >= 0) {
                tvRespondTime.visible()
            } else {
                tvRespondTime.gone()
            }
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemChangeSourceBinding) {
        binding.ivGood.setOnClickListener {
            val newScore = if (binding.ivBad.isVisible) 1 else 0
            applyScoreStyle(binding, newScore)
            getItem(holder.layoutPosition)?.let {
                callBack.setBookScore(it, newScore)
            }
        }
        binding.ivBad.setOnClickListener {
            val newScore = if (binding.ivGood.isVisible) -1 else 0
            applyScoreStyle(binding, newScore)
            getItem(holder.layoutPosition)?.let {
                callBack.setBookScore(it, newScore)
            }
        }
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                callBack.openToc(it)
            }
        }
        holder.itemView.onLongClick {
            showMenu(holder.itemView, getItem(holder.layoutPosition))
        }
    }

    private fun showMenu(view: View, searchBook: SearchBook?) {
        searchBook ?: return
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.change_source_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_top_source -> {
                    callBack.topSource(searchBook)
                }
                R.id.menu_bottom_source -> {
                    callBack.bottomSource(searchBook)
                }
                R.id.menu_edit_source -> {
                    callBack.editSource(searchBook)
                }
                R.id.menu_disable_source -> {
                    callBack.disableSource(searchBook)
                }
                R.id.menu_delete_source -> {
                    callBack.deleteSource(searchBook)
                    updateItems(0, itemCount, listOf<Int>())
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun applyScoreStyle(binding: ItemChangeSourceBinding, score: Int) {
        val activeColor = if (context.readingSurfaceColor != null) {
            context.overlayTextColor
        } else {
            context.getCompatColor(R.color.primaryText)
        }
        val mutedColor = ColorUtils.setAlphaComponent(activeColor, 0x45)
        fun tintIcon(iv: androidx.appcompat.widget.AppCompatImageView, color: Int) {
            iv.drawable?.mutate()?.let { d ->
                DrawableCompat.setTint(d, color)
                iv.setImageDrawable(d)
            }
        }
        when {
            score > 0 -> {
                binding.ivBad.gone()
                binding.ivGood.visible()
                tintIcon(binding.ivGood, activeColor)
            }
            score < 0 -> {
                binding.ivGood.gone()
                binding.ivBad.visible()
                tintIcon(binding.ivBad, activeColor)
            }
            else -> {
                binding.ivGood.visible()
                binding.ivBad.visible()
                tintIcon(binding.ivGood, mutedColor)
                tintIcon(binding.ivBad, mutedColor)
            }
        }
    }

    interface CallBack {
        val oldBookUrl: String?
        fun openToc(searchBook: SearchBook)
        fun topSource(searchBook: SearchBook)
        fun bottomSource(searchBook: SearchBook)
        fun editSource(searchBook: SearchBook)
        fun disableSource(searchBook: SearchBook)
        fun deleteSource(searchBook: SearchBook)
        fun setBookScore(searchBook: SearchBook, score: Int)
        fun getBookScore(searchBook: SearchBook): Int
    }
}
