package io.legado.app.ui.book.manage

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookGroup
import io.legado.app.databinding.ItemArrangeBookBinding
import io.legado.app.help.book.isLocal
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.ui.widget.recycler.DragSelectTouchHelper
import io.legado.app.ui.widget.recycler.ItemTouchCallback
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor
import java.util.Collections

class BookAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<Book, ItemArrangeBookBinding>(context),
    ItemTouchCallback.Callback {

    val groupRequestCode = 12
    private val selectedBooks: HashSet<Book> = hashSetOf()
    var actionItem: Book? = null

    private val isEInk = AppConfig.isEInkMode
    private val pageBg = if (isEInk) Color.WHITE else context.backgroundColor
    private val primaryInk = if (isEInk) Color.BLACK else {
        context.getPrimaryTextColor(ColorUtils.isColorLight(pageBg))
    }
    private val secondaryInk = if (isEInk) Color.BLACK else {
        AndroidColorUtils.setAlphaComponent(primaryInk, 0x99)
    }
    private val accent = if (isEInk) Color.BLACK else context.accentColor
    private val danger = if (isEInk) Color.BLACK else context.getCompatColor(R.color.error)
    private val coverRadius = 6.dpToPx().toFloat()

    val selection: List<Book>
        get() {
            return getItems().filter {
                selectedBooks.contains(it)
            }
        }

    override fun getViewBinding(parent: ViewGroup): ItemArrangeBookBinding {
        return ItemArrangeBookBinding.inflate(inflater, parent, false)
    }

    override fun onCurrentListChanged() {
        callBack.upSelectCount()
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemArrangeBookBinding,
        item: Book,
        payloads: MutableList<Any>
    ) {
        val selected = selectedBooks.contains(item)
        if (payloads.isNotEmpty()) {
            binding.checkbox.isChecked = selected
            binding.llCard.background = cardBackground(selected)
            return
        }
        binding.apply {
            llCard.background = cardBackground(selected)
            checkbox.isChecked = selected

            if (flCover.outlineProvider === ViewOutlineProvider.BACKGROUND) {
                flCover.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, coverRadius)
                    }
                }
                flCover.clipToOutline = true
            }
            ivCover.load(item.getDisplayCover(), item.name, item.author, false, item.origin)

            tvName.text = item.name
            tvName.setTextColor(primaryInk)

            val author = item.getRealAuthor().trim()
            val origin = if (item.isLocal) context.getString(R.string.local_book) else item.originName
            val meta = listOf(author, origin).filter { it.isNotBlank() }.joinToString(" · ")
            tvMeta.text = meta
            tvMeta.setTextColor(secondaryInk)
            tvMeta.visibility = if (meta.isEmpty()) View.GONE else View.VISIBLE

            val groupName = getGroupName(item.group)
            val hasGroup = groupName.isNotEmpty()
            tvGroupS.text = if (hasGroup) groupName else context.getString(R.string.no_group)
            tvGroupS.setTextColor(if (hasGroup) accent else secondaryInk)
            tvGroupS.background = groupBadgeBackground(hasGroup)

            ivGroup.background = iconActionBackground()
            ivGroup.imageTintList = ColorStateList.valueOf(secondaryInk)
            ivDelete.background = iconActionBackground()
            ivDelete.imageTintList = ColorStateList.valueOf(danger)
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemArrangeBookBinding) {
        binding.apply {
            checkbox.setOnUserCheckedChangeListener { isChecked ->
                val pos = holder.bindingAdapterPosition
                getItem(pos)?.let {
                    if (isChecked) {
                        selectedBooks.add(it)
                    } else {
                        selectedBooks.remove(it)
                    }
                    llCard.background = cardBackground(isChecked)
                    callBack.upSelectCount()
                }
            }
            llCard.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                getItem(pos)?.let {
                    val next = !selectedBooks.contains(it)
                    if (next) {
                        selectedBooks.add(it)
                    } else {
                        selectedBooks.remove(it)
                    }
                    checkbox.isChecked = next
                    llCard.background = cardBackground(next)
                    callBack.upSelectCount()
                }
            }
            tvName.setOnClickListener {
                if (AppConfig.openBookInfoByClickTitle) {
                    getItem(holder.bindingAdapterPosition)?.let {
                        callBack.openBook(it)
                    }
                } else {
                    llCard.performClick()
                }
            }
            val openGroup = View.OnClickListener {
                getItem(holder.bindingAdapterPosition)?.let {
                    actionItem = it
                    callBack.selectGroup(groupRequestCode, it.group)
                }
            }
            tvGroupS.setOnClickListener(openGroup)
            ivGroup.setOnClickListener(openGroup)
            ivDelete.setOnClickListener {
                getItem(holder.bindingAdapterPosition)?.let {
                    callBack.deleteBook(it)
                }
            }
        }
    }

    private fun cardBackground(selected: Boolean): Drawable {
        val cardSurface = if (isEInk) Color.WHITE else context.getCompatColor(R.color.background_card)
        if (isEInk) {
            val stroke = if (selected) 2.dpToPx() else 1.dpToPx()
            val strokeColor = if (selected) Color.BLACK else AndroidColorUtils.setAlphaComponent(Color.BLACK, 0x44)
            return Selector.shapeBuild()
                .setCornerRadius(14.dpToPx())
                .setDefaultBgColor(Color.WHITE)
                .setStrokeWidth(stroke)
                .setDefaultStrokeColor(strokeColor)
                .create()
        }
        val fill = if (selected) {
            AndroidColorUtils.blendARGB(cardSurface, accent, 0.08f)
        } else {
            cardSurface
        }
        val pressed = if (selected) {
            AndroidColorUtils.blendARGB(cardSurface, accent, 0.16f)
        } else {
            AndroidColorUtils.blendARGB(cardSurface, primaryInk, 0.07f)
        }
        val border = if (selected) {
            AndroidColorUtils.setAlphaComponent(accent, 0x80)
        } else {
            AndroidColorUtils.setAlphaComponent(primaryInk, 0x12)
        }
        return Selector.shapeBuild()
            .setCornerRadius(14.dpToPx())
            .setDefaultBgColor(fill)
            .setPressedBgColor(pressed)
            .setFocusedBgColor(pressed)
            .setStrokeWidth(1.dpToPx())
            .setDefaultStrokeColor(border)
            .create()
    }

    private fun groupBadgeBackground(hasGroup: Boolean): Drawable {
        return GradientDrawable().apply {
            cornerRadius = 8.dpToPx().toFloat()
            if (isEInk) {
                setColor(Color.WHITE)
                setStroke(1.dpToPx(), AndroidColorUtils.setAlphaComponent(Color.BLACK, 0x55))
            } else {
                val base = context.getCompatColor(R.color.background_card)
                setColor(
                    if (hasGroup) {
                        AndroidColorUtils.blendARGB(base, accent, 0.12f)
                    } else {
                        AndroidColorUtils.blendARGB(base, primaryInk, 0.06f)
                    }
                )
            }
        }
    }

    private fun iconActionBackground(): Drawable {
        if (isEInk) {
            return Selector.shapeBuild()
                .setCornerRadius(18.dpToPx())
                .setDefaultBgColor(Color.TRANSPARENT)
                .create()
        }
        return Selector.shapeBuild()
            .setCornerRadius(18.dpToPx())
            .setDefaultBgColor(Color.TRANSPARENT)
            .setPressedBgColor(AndroidColorUtils.setAlphaComponent(primaryInk, 0x18))
            .setFocusedBgColor(AndroidColorUtils.setAlphaComponent(primaryInk, 0x18))
            .create()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(selectAll: Boolean) {
        if (selectAll) {
            getItems().forEach {
                selectedBooks.add(it)
            }
        } else {
            selectedBooks.clear()
        }
        notifyDataSetChanged()
        callBack.upSelectCount()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun revertSelection() {
        getItems().forEach {
            if (selectedBooks.contains(it)) {
                selectedBooks.remove(it)
            } else {
                selectedBooks.add(it)
            }
        }
        notifyDataSetChanged()
        callBack.upSelectCount()
    }

    fun checkSelectedInterval() {
        val selectedPosition = linkedSetOf<Int>()
        getItems().forEachIndexed { index, it ->
            if (selectedBooks.contains(it)) {
                selectedPosition.add(index)
            }
        }
        if (selectedPosition.isEmpty()) return
        val minPosition = Collections.min(selectedPosition)
        val maxPosition = Collections.max(selectedPosition)
        val itemCount = maxPosition - minPosition + 1
        for (i in minPosition..maxPosition) {
            getItem(i)?.let {
                selectedBooks.add(it)
            }
        }
        notifyItemRangeChanged(minPosition, itemCount, bundleOf(Pair("selected", null)))
        callBack.upSelectCount()
    }

    private fun getGroupList(groupId: Long): List<String> {
        val groupNames = arrayListOf<String>()
        callBack.groupList.forEach {
            if (it.groupId > 0 && it.groupId and groupId > 0) {
                groupNames.add(it.groupName)
            }
        }
        return groupNames
    }

    private fun getGroupName(groupId: Long): String {
        val groupNames = getGroupList(groupId)
        if (groupNames.isEmpty()) {
            return ""
        }
        return groupNames.joinToString(" · ")
    }

    private var isMoved = false

    override fun swap(srcPosition: Int, targetPosition: Int): Boolean {
        val srcItem = getItem(srcPosition)
        val targetItem = getItem(targetPosition)
        if (srcItem != null && targetItem != null) {
            if (srcItem.order == targetItem.order) {
                for ((index, item) in getItems().withIndex()) {
                    item.order = index + 1
                }
            } else {
                val pos = srcItem.order
                srcItem.order = targetItem.order
                targetItem.order = pos
            }
        }
        swapItem(srcPosition, targetPosition)
        isMoved = true
        return true
    }

    override fun onClearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (isMoved) {
            callBack.updateBook(*getItems().toTypedArray())
        }
        isMoved = false
    }

    val dragSelectCallback: DragSelectTouchHelper.Callback =
        object : DragSelectTouchHelper.AdvanceCallback<Book>(Mode.ToggleAndReverse) {
            override fun currentSelectedId(): MutableSet<Book> {
                return selectedBooks
            }

            override fun getItemId(position: Int): Book {
                return getItem(position)!!
            }

            override fun updateSelectState(position: Int, isSelected: Boolean): Boolean {
                getItem(position)?.let {
                    if (isSelected) {
                        selectedBooks.add(it)
                    } else {
                        selectedBooks.remove(it)
                    }
                    notifyItemChanged(position, bundleOf(Pair("selected", null)))
                    callBack.upSelectCount()
                    return true
                }
                return false
            }
        }

    interface CallBack {
        val groupList: List<BookGroup>

        fun upSelectCount()

        fun updateBook(vararg book: Book)

        fun deleteBook(book: Book)

        fun selectGroup(requestCode: Int, groupId: Long)

        fun openBook(book: Book)
    }
}
