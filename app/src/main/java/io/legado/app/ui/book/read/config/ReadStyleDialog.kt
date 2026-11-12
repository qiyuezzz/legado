package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.core.view.children
import androidx.core.view.get
import com.github.liuyueyi.quick.transfer.constants.TransType
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.constant.EventBus
import io.legado.app.databinding.DialogReadBookStyleBinding
import io.legado.app.databinding.ItemReadStyleBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.lib.dialogs.selector
import io.legado.app.lib.theme.dialogPill
import io.legado.app.lib.theme.showBottomPanel
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.font.FontSelectDialog
import io.legado.app.utils.ChineseUtils
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getIndexById
import io.legado.app.utils.postEvent
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.viewbindingdelegate.viewBinding

class ReadStyleDialog : BaseDialogFragment(R.layout.dialog_read_book_style),
    FontSelectDialog.CallBack {

    private val binding by viewBinding(DialogReadBookStyleBinding::bind)
    private val callBack get() = activity as? ReadBookActivity
    private lateinit var styleAdapter: StyleAdapter

    override fun onStart() {
        super.onStart()
        dialog?.window?.showBottomPanel(requireView())
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        (activity as ReadBookActivity).bottomDialog++
        initView()
        updatePalette()
        initData()
        initViewEvent()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        ReadBookConfig.save()
        (activity as ReadBookActivity).bottomDialog--
    }

    private fun initView() = binding.run {
        spacingTabs.check(tabTextSize.id)
        spacingTabs.setOnCheckedChangeListener { group, id ->
            adjustments.displayedChild = group.getIndexById(id)
        }
        dsbTextSize.valueFormat = {
            (it + 5).toString()
        }
        dsbTextLetterSpacing.valueFormat = {
            ((it - 50) / 100f).toString()
        }
        dsbLineSize.valueFormat = { ((it - 10) / 10f).toString() }
        dsbParagraphSpacing.valueFormat = { (it / 10f).toString() }
        styleAdapter = StyleAdapter()
        rvStyle.adapter = styleAdapter
        addStyle.setOnClickListener {
            ReadBookConfig.configList.add(ReadBookConfig.Config())
            showBgTextConfig(ReadBookConfig.configList.lastIndex)
        }
    }

    private val panelColor get() = requireContext().readingPanelColor

    private val panelInk: Int
        get() = requireContext().getPrimaryTextColor(ColorUtils.isColorLight(panelColor))

    private fun updatePalette() = binding.run {
        val paper = panelColor
        val ink = panelInk
        val soft = blendARGB(paper, ink, 0.07f)
        val active = blendARGB(paper, ink, 0.19f)
        rootView.setBackgroundColor(paper)
        listOf(spacingTabs, rgPageAnim).forEach { group ->
            group.background = dialogPill(soft, soft)
            group.children.filterIsInstance<RadioButton>().forEach { option ->
                option.background = dialogPill(Color.TRANSPARENT, active)
                option.setTextColor(ink)
            }
        }
        listOf(textFontWeightConverter, tvTextFont, tvTextIndent, chineseConverter, tvPadding, tvTip)
            .forEach { button ->
                button.background = dialogPill(soft, active)
                button.setTextColor(ink)
            }
        addStyle.background = dialogPill(soft, active)
        addStyle.setColorFilter(ink)
        adjustments.background = dialogPill(soft, soft)
        listOf(dsbTextSize, dsbTextLetterSpacing, dsbLineSize, dsbParagraphSpacing).forEach {
            it.styleForReadingPanel(ink)
        }
        listOf(tvPageAnim, tvBgTs, tvShareLayout).forEach {
            it.setTextColor(blendARGB(paper, ink, 0.78f))
        }
    }

    private fun initData() {
        binding.cbShareLayout.isChecked = ReadBookConfig.shareLayout
        upView()
        styleAdapter.setItems(ReadBookConfig.configList)
    }

    private fun initViewEvent() = binding.run {
        chineseConverter.onChanged {
            ChineseUtils.unLoad(*TransType.entries.toTypedArray())
            postEvent(EventBus.UP_CONFIG, arrayListOf(5))
        }
        textFontWeightConverter.onChanged {
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 9, 6))
        }
        tvTextFont.setOnClickListener {
            showDialogFragment<FontSelectDialog>()
        }
        tvTextIndent.setOnClickListener {
            context?.selector(
                title = getString(R.string.text_indent),
                items = resources.getStringArray(R.array.indent).toList()
            ) { _, index ->
                ReadBookConfig.paragraphIndent = "　".repeat(index)
                postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
            }
        }
        tvPadding.setOnClickListener {
            dismissAllowingStateLoss()
            callBack?.showPaddingConfig()
        }
        tvShareLayout.setOnClickListener { cbShareLayout.performClick() }
        tvTip.setOnClickListener {
            TipConfigDialog().show(childFragmentManager, "tipConfigDialog")
        }
        rgPageAnim.setOnCheckedChangeListener { _, checkedId ->
            ReadBook.book?.setPageAnim(-1)
            ReadBookConfig.pageAnim = binding.rgPageAnim.getIndexById(checkedId)
            callBack?.upPageAnim()
            ReadBook.loadContent(false)
        }
        cbShareLayout.onCheckedChangeListener = { _, isChecked ->
            ReadBookConfig.shareLayout = isChecked
            upView()
            postEvent(EventBus.UP_CONFIG, arrayListOf(1, 2, 5))
        }
        dsbTextSize.onChanged = {
            ReadBookConfig.textSize = it + 5
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
        dsbTextLetterSpacing.onChanged = {
            ReadBookConfig.letterSpacing = (it - 50) / 100f
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
        dsbLineSize.onChanged = {
            ReadBookConfig.lineSpacingExtra = it
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
        dsbParagraphSpacing.onChanged = {
            ReadBookConfig.paragraphSpacing = it
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
    }

    private fun changeBgTextConfig(index: Int) {
        val oldIndex = ReadBookConfig.styleSelect
        if (index != oldIndex) {
            ReadBookConfig.styleSelect = index
            upView()
            updatePalette()
            styleAdapter.notifyDataSetChanged()
            postEvent(EventBus.UP_CONFIG, arrayListOf(1, 2, 5))
            if (AppConfig.readBarStyleFollowPage) {
                postEvent(EventBus.UPDATE_READ_ACTION_BAR, true)
            }
        }
    }

    private fun showBgTextConfig(index: Int): Boolean {
        dismissAllowingStateLoss()
        changeBgTextConfig(index)
        callBack?.showBgTextConfig()
        return true
    }

    private fun upView() = binding.run {
        textFontWeightConverter.upUi(ReadBookConfig.textBold)
        ReadBook.pageAnim().let {
            if (it >= 0 && it < rgPageAnim.childCount) {
                rgPageAnim.check(rgPageAnim[it].id)
            }
        }
        ReadBookConfig.let {
            dsbTextSize.progress = it.textSize - 5
            dsbTextLetterSpacing.progress = (it.letterSpacing * 100).toInt() + 50
            dsbLineSize.progress = it.lineSpacingExtra
            dsbParagraphSpacing.progress = it.paragraphSpacing
        }
    }

    override val curFontPath: String
        get() = ReadBookConfig.textFont

    override fun selectFont(path: String) {
        if (path != ReadBookConfig.textFont || path.isEmpty()) {
            ReadBookConfig.textFont = path
            postEvent(EventBus.UP_CONFIG, arrayListOf(2, 5))
        }
    }

    inner class StyleAdapter :
        RecyclerAdapter<ReadBookConfig.Config, ItemReadStyleBinding>(requireContext()) {

        override fun getViewBinding(parent: ViewGroup): ItemReadStyleBinding {
            return ItemReadStyleBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemReadStyleBinding,
            item: ReadBookConfig.Config,
            payloads: MutableList<Any>
        ) {
            binding.apply {
                val selected = ReadBookConfig.styleSelect == holder.layoutPosition
                val ink = panelInk
                val paper = panelColor
                styleName.text = item.name.ifBlank { getString(R.string.text) }
                styleName.setTextColor(ink)
                root.background = dialogPill(blendARGB(paper, ink, 0.06f), blendARGB(paper, ink, 0.18f))
                root.isSelected = selected
                ivStyle.borderColor = if (selected) ink else blendARGB(paper, ink, 0.28f)
                ivStyle.setImageDrawable(item.curBgDrawable(100, 150))
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemReadStyleBinding) {
            binding.apply {
                root.setOnClickListener {
                    changeBgTextConfig(holder.layoutPosition)
                }
                root.setOnLongClickListener {
                    showBgTextConfig(holder.layoutPosition)
                }
            }
        }

    }
}