package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.databinding.DialogReadingPanelBinding
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.book.toc.TocDialog
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.viewbindingdelegate.viewBinding

/** One window and one navigation bar; switching tabs only changes the embedded controls. */
class ReadingPanelDialog : BaseDialogFragment(R.layout.dialog_reading_panel) {
    private val binding by viewBinding(DialogReadingPanelBinding::bind)
    private val host get() = activity as? ReadBookActivity
    private var selected = ReadingPanel.CATALOG
    private var counted = false

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        selected = ReadingPanel.valueOf(
            savedInstanceState?.getString(SELECTED) ?: arguments?.getString(SELECTED)
                ?: ReadingPanel.CATALOG.name
        )
        host?.let {
            it.bottomDialog++
            counted = true
        }
        val paper = requireContext().readingPanelColor
        val radius = 20.dpToPx().toFloat()
        view.setBackgroundColor(Color.TRANSPARENT)
        view.setOnClickListener { dismiss() }
        binding.readingPanelSurface.background = GradientDrawable().apply {
            setColor(paper)
            cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
        }
        binding.readingPanelSurface.clipToOutline = true
        val navigation = binding.navigation
        val buttons = listOf(
            navigation.llCatalog, navigation.llReadAloud, navigation.llFont, navigation.llSetting
        )
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val next = ReadingPanel.entries[index]
                if (next == selected && next == ReadingPanel.CATALOG) {
                    val activity = host
                    dismiss()
                    activity?.showMenuBar()
                } else {
                    selectPanel(next)
                }
            }
        }
        showContent(selected)
    }

    override fun onStart() {
        // Configure the window before DialogFragment shows it in super.onStart().
        dialog?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawableResource(android.R.color.transparent)
            WindowCompat.setDecorFitsSystemWindows(this, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                attributes = attributes.apply {
                    setFitInsetsTypes(0)
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            } else {
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR)
            }
            decorView.setPadding(0, 0, 0, 0)
            setGravity(Gravity.BOTTOM)
            setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            )
            navigationBarColor = requireContext().readingPanelColor
            WindowInsetsControllerCompat(this, decorView).apply {
                val light = ColorUtils.isColorLight(requireContext().readingPanelColor)
                isAppearanceLightNavigationBars = light
                isAppearanceLightStatusBars = light
            }
            setWindowAnimations(0)
            setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        updateHeight()
        super.onStart()
        binding.root.doOnPreDraw {
            host?.hideMainMenuForReadingPanel()
        }
    }

    internal fun selectPanel(panel: ReadingPanel) {
        if (panel == selected || childFragmentManager.isStateSaved) return
        if (selected == ReadingPanel.STYLE) ReadBookConfig.save()
        selected = panel
        if (view != null) {
            showContent(panel)
            updateHeight()
        }
    }

    private fun showContent(panel: ReadingPanel) {
        val tag = "reading-panel-${panel.name}"
        val existing = childFragmentManager.findFragmentByTag(tag)
        val content = existing ?: when (panel) {
            ReadingPanel.CATALOG -> TocDialog().apply {
                arguments = Bundle().apply { putString("bookUrl", ReadBook.book?.bookUrl) }
            }
            ReadingPanel.READ_ALOUD -> ReadAloudDialog()
            ReadingPanel.STYLE -> ReadStyleDialog()
            ReadingPanel.SETTINGS -> MoreConfigDialog()
        }.apply { showsDialog = false }
        val transaction = childFragmentManager.beginTransaction().setCustomAnimations(0, 0)
        childFragmentManager.fragments.forEach {
            if (it != content) {
                transaction.hide(it).setMaxLifecycle(it, Lifecycle.State.STARTED)
            }
        }
        if (existing == null) transaction.add(R.id.reading_panel_content, content, tag)
        else transaction.show(content)
        transaction.setMaxLifecycle(content, Lifecycle.State.RESUMED).commitNow()
        updateSelection()
    }

    private fun updateSelection() = binding.navigation.run {
        val paper = requireContext().readingPanelColor
        val ink = requireContext().getPrimaryTextColor(ColorUtils.isColorLight(paper))
        root.setBackgroundColor(paper)
        val buttons = listOf(llCatalog, llReadAloud, llFont, llSetting)
        val icons = listOf(ivCatalog, ivReadAloud, ivFont, ivSetting)
        val labels = listOf(tvCatalog, tvReadAloud, tvFont, tvSetting)
        buttons.forEachIndexed { index, button ->
            val active = index == selected.ordinal
            val color = if (active) requireContext().accentColor else ink
            button.isSelected = active
            icons[index].setColorFilter(color)
            labels[index].setTextColor(color)
        }
    }

    private fun updateHeight() {
        val decor = host?.window?.decorView ?: return
        val insets = ViewCompat.getRootWindowInsets(decor)
            ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars()) ?: Insets.NONE
        val screenHeight = decor.height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels
        val available = (screenHeight - insets.top - insets.bottom - 72.dpToPx())
            .coerceAtLeast(160.dpToPx())
        val contentHeight = minOf(360.dpToPx(), available)
        val height = contentHeight + 56.dpToPx()
        val fullPage = selected == ReadingPanel.CATALOG
        binding.root.setPadding(0, insets.top, 0, 0)
        binding.readingPanelSurface.setPadding(0, 0, 0, insets.bottom)
        binding.root.setBackgroundColor(if (fullPage) requireContext().readingPanelColor else Color.TRANSPARENT)
        (binding.readingPanelSurface.background as? GradientDrawable)?.let {
            val radius = if (fullPage) 0f else 20.dpToPx().toFloat()
            it.cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
        }
        binding.readingPanelSurface.updateLayoutParams<FrameLayout.LayoutParams> {
            this.height = if (fullPage) LayoutParams.MATCH_PARENT else height + insets.bottom
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SELECTED, selected.name)
        super.onSaveInstanceState(outState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (counted) {
            counted = false
            host?.let { it.bottomDialog-- }
        }
    }

    companion object {
        const val TAG = "reading-panel"
        private const val SELECTED = "selected-panel"

        internal fun create(panel: ReadingPanel) = ReadingPanelDialog().apply {
            arguments = Bundle().apply { putString(SELECTED, panel.name) }
        }
    }
}
