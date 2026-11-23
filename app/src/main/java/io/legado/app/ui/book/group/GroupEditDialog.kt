package io.legado.app.ui.book.group

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.BookGroup
import io.legado.app.databinding.DialogBookGroupEditBinding
import io.legado.app.lib.dialogs.AndroidAlertBuilder
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import splitties.views.onClick

class GroupEditDialog() : BaseDialogFragment(R.layout.dialog_book_group_edit) {

    constructor(bookGroup: BookGroup? = null) : this() {
        arguments = Bundle().apply {
            putParcelable("group", bookGroup?.copy())
        }
    }

    private val binding by viewBinding(DialogBookGroupEditBinding::bind)
    private val viewModel by viewModels<GroupViewModel>()
    private lateinit var palette: GroupSheetPalette
    private var bookGroup: BookGroup? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.configureGroupBottomSheet(requireContext(), adjustResize = true)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        palette = GroupSheetPalette(requireContext())
        applyStyle()
        @Suppress("DEPRECATION")
        bookGroup = arguments?.getParcelable("group")
        bookGroup?.let {
            binding.tvTitle.setText(R.string.group_edit)
            binding.llDelete.isVisible = it.groupId > 0 || it.groupId == Long.MIN_VALUE
            binding.tieGroupName.setText(it.groupName)
            binding.tieGroupName.setSelection(it.groupName.length)
            if (it.bookSort + 1 !in 0..<binding.spSort.count) {
                it.bookSort = -1
            }
            binding.spSort.setSelection(it.bookSort + 1)
            binding.swEnableRefresh.isChecked = it.enableRefresh
        } ?: let {
            binding.tvTitle.setText(R.string.add_group)
            binding.llDelete.isVisible = false
        }
        initViewEvent()
    }

    private fun applyStyle() = binding.run {
        palette.applySheetSurface(llRoot, vwHandle, vwDividerTop, vwCardDivider, vwDividerBottom)
        llRoot.applyNavigationBarPadding(withInitialPadding = true)
        tvTitle.setTextColor(palette.primaryInk)

        llDelete.background = palette.dangerPillBackground()
        ivDelete.imageTintList = ColorStateList.valueOf(palette.danger)
        tvDelete.setTextColor(palette.danger)

        tvNameLabel.setTextColor(palette.secondaryInk)
        flNameInput.background = palette.inputBackground()
        tieGroupName.setTextColor(palette.primaryInk)
        tieGroupName.setHintTextColor(palette.tertiaryInk)

        llSettingsCard.background = palette.sectionCardBackground()
        llSettingsCard.clipToOutline = true
        tvSortLabel.setTextColor(palette.primaryInk)
        tvRefreshTitle.setTextColor(palette.primaryInk)
        tvRefreshSummary.setTextColor(palette.secondaryInk)

        btnCancel.background = palette.secondaryButtonBackground()
        btnCancel.setTextColor(palette.primaryInk)
        btnOk.background = palette.primaryButtonBackground()
        btnOk.setTextColor(palette.surface)
    }

    private fun initViewEvent() = binding.run {
        llRefreshRow.onClick {
            swEnableRefresh.toggle()
        }
        btnCancel.onClick {
            dismiss()
        }
        btnOk.onClick {
            val groupName = tieGroupName.text?.toString()?.trim()
            if (groupName.isNullOrEmpty()) {
                toastOnUi("分组名称不能为空")
            } else {
                val bookSort = spSort.selectedItemPosition - 1
                val enableRefresh = swEnableRefresh.isChecked
                bookGroup?.let {
                    it.groupName = groupName
                    it.bookSort = bookSort
                    it.enableRefresh = enableRefresh
                    viewModel.upGroup(it) {
                        dismiss()
                    }
                } ?: let {
                    viewModel.addGroup(
                        groupName,
                        bookSort,
                        enableRefresh,
                        null
                    ) {
                        dismiss()
                    }
                }
            }
        }
        llDelete.onClick {
            deleteGroup {
                bookGroup?.let {
                    viewModel.delGroup(it) {
                        dismiss()
                    }
                }
            }
        }
    }

    private fun deleteGroup(ok: () -> Unit) {
        val groupName = bookGroup?.groupName.orEmpty()
        AndroidAlertBuilder(requireContext(), emphasizePositiveAction = true).apply {
            setTitle(R.string.delete)
            if (groupName.isNotEmpty()) {
                setMessage(getString(R.string.sure_del_any, groupName))
            } else {
                setMessage(R.string.sure_del)
            }
            positiveButton(R.string.delete) {
                ok.invoke()
            }
            cancelButton()
        }.show()
    }

}
