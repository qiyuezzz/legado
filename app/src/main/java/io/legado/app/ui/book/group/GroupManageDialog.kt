package io.legado.app.ui.book.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.constant.AppLog
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookGroup
import io.legado.app.databinding.DialogBookGroupManageBinding
import io.legado.app.databinding.ItemBookGroupManageBinding
import io.legado.app.ui.widget.recycler.ItemTouchCallback
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * 书籍分组管理
 */
class GroupManageDialog : BaseDialogFragment(R.layout.dialog_book_group_manage) {

    private val viewModel: GroupViewModel by viewModels()
    private val binding by viewBinding(DialogBookGroupManageBinding::bind)
    private lateinit var palette: GroupSheetPalette
    private val adapter by lazy { GroupAdapter(requireContext()) }
    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchCallback(adapter).apply { isCanDrag = true })
    }
    private val sortLabels by lazy { resources.getStringArray(R.array.book_sort) }

    override fun onStart() {
        super.onStart()
        val sheetHeight = (resources.displayMetrics.heightPixels * 0.78f).toInt()
        dialog?.window?.configureGroupBottomSheet(requireContext(), height = sheetHeight)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        palette = GroupSheetPalette(requireContext())
        applyStyle()
        initView()
        initData()
    }

    private fun applyStyle() = binding.run {
        palette.applySheetSurface(llRoot, vwHandle, vwDividerTop, vwDividerBottom)
        llRoot.applyNavigationBarPadding(withInitialPadding = true)
        tvTitle.setTextColor(palette.primaryInk)
        tvSubtitle.setTextColor(palette.secondaryInk)

        llAdd.background = palette.subtlePillBackground()
        ivAdd.imageTintList = ColorStateList.valueOf(palette.accent)
        tvAdd.setTextColor(palette.accent)

        tvDone.background = palette.primaryButtonBackground()
        tvDone.setTextColor(palette.surface)
    }

    private fun initView() = binding.run {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        itemTouchHelper.attachToRecyclerView(recyclerView)

        llAdd.setOnClickListener {
            if (appDb.bookGroupDao.canAddGroup) {
                showDialogFragment(GroupEditDialog())
            } else {
                toastOnUi(R.string.group_limit_reached)
            }
        }
        tvDone.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            appDb.bookGroupDao.flowAll().catch {
                AppLog.put("书籍分组管理界面获取分组数据失败\n${it.localizedMessage}", it)
            }.flowOn(IO).conflate().collect {
                adapter.setItems(it)
            }
        }
    }

    private fun builtInBadge(group: BookGroup): String? {
        val raw = when (group.groupId) {
            BookGroup.IdAll -> getString(R.string.all)
            BookGroup.IdAudio -> getString(R.string.audio)
            BookGroup.IdLocal -> getString(R.string.local)
            BookGroup.IdNetNone -> getString(R.string.net_no_group)
            BookGroup.IdLocalNone -> getString(R.string.local_no_group)
            BookGroup.IdError -> getString(R.string.update_book_fail)
            else -> null
        } ?: return null
        return if (raw == group.groupName) getString(R.string.group_builtin_badge) else raw
    }

    private fun groupMeta(group: BookGroup): String {
        val sortIndex = (group.bookSort + 1).coerceIn(0, sortLabels.lastIndex)
        val sortText = sortLabels[sortIndex]
        val refreshText = getString(
            if (group.enableRefresh) R.string.group_refresh_enabled else R.string.group_refresh_disabled
        )
        return "$sortText · $refreshText"
    }

    private inner class GroupAdapter(context: Context) :
        RecyclerAdapter<BookGroup, ItemBookGroupManageBinding>(context),
        ItemTouchCallback.Callback {

        private var isMoved = false

        override fun getViewBinding(parent: ViewGroup): ItemBookGroupManageBinding {
            return ItemBookGroupManageBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemBookGroupManageBinding,
            item: BookGroup,
            payloads: MutableList<Any>
        ) = binding.run {
            llItem.background = palette.cardBackground(selected = false)
            ivDrag.imageTintList = ColorStateList.valueOf(palette.tertiaryInk)
            ivEdit.background = palette.iconButtonBackground()
            ivEdit.imageTintList = ColorStateList.valueOf(palette.secondaryInk)

            tvGroup.text = item.groupName
            tvGroup.setTextColor(palette.primaryInk)

            val badge = builtInBadge(item)
            tvBadge.isGone = badge == null
            if (badge != null) {
                tvBadge.text = badge
                tvBadge.setTextColor(palette.secondaryInk)
                tvBadge.background = palette.badgeBackground()
            }

            tvMeta.text = groupMeta(item)
            tvMeta.setTextColor(palette.secondaryInk)

            swShow.isChecked = item.show
            val alpha = if (item.show) 1f else 0.55f
            tvGroup.alpha = alpha
            tvMeta.alpha = alpha
            tvBadge.alpha = alpha
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun registerListener(holder: ItemViewHolder, binding: ItemBookGroupManageBinding) {
            binding.run {
                val openEdit = View.OnClickListener {
                    getItem(holder.bindingAdapterPosition)?.let { bookGroup ->
                        showDialogFragment(GroupEditDialog(bookGroup))
                    }
                }
                llItem.setOnClickListener(openEdit)
                ivEdit.setOnClickListener(openEdit)
                ivDrag.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(holder)
                    }
                    false
                }
                swShow.setOnUserCheckedChangeListener { isChecked ->
                    getItem(holder.bindingAdapterPosition)?.let {
                        viewModel.upGroup(it.copy(show = isChecked))
                    }
                }
            }
        }

        override fun swap(srcPosition: Int, targetPosition: Int): Boolean {
            swapItem(srcPosition, targetPosition)
            isMoved = true
            return true
        }

        override fun onClearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            if (isMoved) {
                for ((index, item) in getItems().withIndex()) {
                    item.order = index + 1
                }
                viewModel.upGroup(*getItems().toTypedArray())
            }
            isMoved = false
        }
    }

}
