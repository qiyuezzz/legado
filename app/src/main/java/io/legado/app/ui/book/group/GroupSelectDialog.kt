package io.legado.app.ui.book.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
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
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookGroup
import io.legado.app.databinding.DialogBookshelfGroupPickerBinding
import io.legado.app.databinding.ItemGroupSelectBinding
import io.legado.app.ui.widget.recycler.ItemTouchCallback
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class GroupSelectDialog() : BaseDialogFragment(R.layout.dialog_bookshelf_group_picker) {

    constructor(groupId: Long, requestCode: Int = -1) : this() {
        arguments = Bundle().apply {
            putLong("groupId", groupId)
            putInt("requestCode", requestCode)
        }
    }

    private val binding by viewBinding(DialogBookshelfGroupPickerBinding::bind)
    private var requestCode: Int = -1
    private val viewModel: GroupViewModel by viewModels()
    private lateinit var palette: GroupSheetPalette
    private val adapter by lazy { GroupAdapter(requireContext()) }
    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchCallback(adapter).apply { isCanDrag = true })
    }
    private val callBack get() = (parentFragment as? CallBack) ?: (activity as? CallBack)
    private var groupId: Long = 0

    override fun onStart() {
        super.onStart()
        dialog?.window?.configureGroupBottomSheet(requireContext())
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        palette = GroupSheetPalette(requireContext())
        arguments?.let {
            groupId = savedInstanceState?.getLong("groupId", it.getLong("groupId"))
                ?: it.getLong("groupId")
            requestCode = it.getInt("requestCode", -1)
        }
        applyStyle()
        initView()
        initData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("groupId", groupId)
        super.onSaveInstanceState(outState)
    }

    private fun applyStyle() = binding.run {
        palette.applySheetSurface(clRoot, vwHandle, vwDividerTop, vwDividerBottom)
        clRoot.applyNavigationBarPadding(withInitialPadding = true)
        tvTitle.setTextColor(palette.primaryInk)
        tvSubtitle.setTextColor(palette.secondaryInk)

        llAdd.background = palette.subtlePillBackground()
        ivAdd.imageTintList = ColorStateList.valueOf(palette.accent)
        tvAdd.setTextColor(palette.accent)

        ivEmpty.imageTintList = ColorStateList.valueOf(palette.tertiaryInk)
        tvEmptyTitle.setTextColor(palette.primaryInk)
        tvEmptySummary.setTextColor(palette.secondaryInk)

        tvCancel.background = palette.secondaryButtonBackground()
        tvCancel.setTextColor(palette.primaryInk)
        tvOk.background = palette.primaryButtonBackground()
        tvOk.setTextColor(palette.surface)
    }

    private fun initView() = binding.run {
        (recyclerView.layoutParams as? ConstraintLayout.LayoutParams)?.let { lp ->
            lp.matchConstraintMaxHeight = (resources.displayMetrics.heightPixels * 0.52f).toInt()
            recyclerView.layoutParams = lp
        }
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
        tvCancel.setOnClickListener {
            dismissAllowingStateLoss()
        }
        tvOk.setOnClickListener {
            callBack?.upGroup(requestCode, groupId)
            dismissAllowingStateLoss()
        }
    }

    private fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            appDb.bookGroupDao.flowSelect().conflate().collect { groups ->
                adapter.setItems(groups)
                binding.llEmpty.isGone = groups.isNotEmpty()
                binding.recyclerView.isGone = groups.isEmpty()
                updateSelectionSubtitle(groups)
            }
        }
    }

    private fun updateSelectionSubtitle(groups: List<BookGroup> = adapter.getItems()) {
        val selectedCount = groups.count { (groupId and it.groupId) > 0 }
        binding.tvSubtitle.text = if (selectedCount > 0) {
            getString(R.string.group_select_count, selectedCount)
        } else {
            getString(R.string.group_select_hint)
        }
    }

    private inner class GroupAdapter(context: Context) :
        RecyclerAdapter<BookGroup, ItemGroupSelectBinding>(context),
        ItemTouchCallback.Callback {

        private var isMoved: Boolean = false

        override fun getViewBinding(parent: ViewGroup): ItemGroupSelectBinding {
            return ItemGroupSelectBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemGroupSelectBinding,
            item: BookGroup,
            payloads: MutableList<Any>
        ) = binding.run {
            val selected = (groupId and item.groupId) > 0
            llItem.background = palette.cardBackground(selected = selected)
            ivDrag.imageTintList = ColorStateList.valueOf(palette.tertiaryInk)
            ivEdit.background = palette.iconButtonBackground()
            ivEdit.imageTintList = ColorStateList.valueOf(palette.secondaryInk)
            cbGroup.isChecked = selected
            tvName.text = item.groupName
            tvName.setTextColor(if (selected) palette.accent else palette.primaryInk)
            tvName.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun registerListener(holder: ItemViewHolder, binding: ItemGroupSelectBinding) {
            binding.run {
                llItem.setOnClickListener {
                    val pos = holder.bindingAdapterPosition
                    getItem(pos)?.let { item ->
                        val isSelected = (groupId and item.groupId) > 0
                        groupId = if (isSelected) {
                            groupId and item.groupId.inv()
                        } else {
                            groupId or item.groupId
                        }
                        notifyItemChanged(pos)
                        updateSelectionSubtitle()
                    }
                }
                ivDrag.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(holder)
                    }
                    false
                }
                ivEdit.setOnClickListener {
                    getItem(holder.bindingAdapterPosition)?.let { bookGroup ->
                        showDialogFragment(GroupEditDialog(bookGroup))
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

    interface CallBack {
        fun upGroup(requestCode: Int, groupId: Long)
    }
}
