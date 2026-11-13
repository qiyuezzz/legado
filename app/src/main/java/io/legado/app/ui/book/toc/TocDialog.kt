package io.legado.app.ui.book.toc

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import io.legado.app.R
import io.legado.app.databinding.DialogReadTocBinding
import io.legado.app.lib.theme.dialogPill
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.read.config.ReadingPanelFragment
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.book.read.config.readingPanelColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.hideSoftInput
import io.legado.app.utils.viewbindingdelegate.viewBinding

/** Reading-page directory: shares the bottom navigation with the other reading panels. */
class TocDialog : ReadingPanelFragment(R.layout.dialog_read_toc) {
    private val binding by viewBinding(DialogReadTocBinding::bind)
    private val viewModel by viewModels<TocViewModel>()
    private val host get() = activity as? ReadBookActivity

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        if (!isEmbeddedReadingPanel) host?.let { it.bottomDialog++ }
        val paper = requireContext().readingPanelColor
        val ink = requireContext().getPrimaryTextColor(ColorUtils.isColorLight(paper))
        val soft = blendARGB(paper, ink, 0.07f)
        view.setBackgroundColor(Color.TRANSPARENT)
        binding.handleIndicator.background = dialogPill(blendARGB(paper, ink, 0.24f), soft)
        binding.searchTabs.background = dialogPill(soft, soft)
        binding.tabs.setBackgroundColor(Color.TRANSPARENT)
        binding.tabs.setTabTextColors(blendARGB(paper, ink, 0.65f), ink)
        binding.tabs.setSelectedTabIndicatorColor(Color.TRANSPARENT)
        binding.more.setColorFilter(ink)
        binding.search.setTextColor(ink)
        binding.search.setHintTextColor(ColorUtils.withAlpha(ink, 0.6f))
        TextViewCompat.setCompoundDrawableTintList(binding.search, ColorStateList.valueOf(ink))
        binding.bookTitle.setTextColor(ink)
        binding.bookTitle.text = ReadBook.book?.name
        viewModel.bookData.observe(viewLifecycleOwner) { binding.bookTitle.text = it.name }
        binding.viewPager.adapter = Pages()
        binding.tabs.setupWithViewPager(binding.viewPager)
        for (index in 0 until binding.tabs.tabCount) {
            binding.tabs.getTabAt(index)?.view?.background = dialogPill(Color.TRANSPARENT, paper)
        }
        listOf(binding.jumpCurrent, binding.jumpBottom).forEach {
            it.setTextColor(ink)
            it.background = dialogPill(soft, blendARGB(paper, ink, 0.14f))
        }
        binding.jumpCurrent.setOnClickListener {
            binding.search.text?.clear()
            binding.search.clearFocus()
            binding.search.hideSoftInput()
            chapterList()?.scrollToCurrentChapter()
        }
        binding.jumpBottom.setOnClickListener { chapterList()?.scrollToBottom() }
        binding.dragHandle.setOnClickListener { collapse() }
        var touchStart = 0f
        binding.dragHandle.setOnTouchListener { handle, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> touchStart = event.rawY
                MotionEvent.ACTION_UP -> {
                    val distance = event.rawY - touchStart
                    if (distance > 32.dpToPx()) collapse()
                    else if (kotlin.math.abs(distance) < 8.dpToPx()) handle.performClick()
                }
            }
            true
        }
        binding.search.doAfterTextChanged {
            viewModel.searchKey = it?.toString()
            searchCurrentPage()
        }
        binding.search.setOnEditorActionListener { view, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                view.hideSoftInput()
                view.clearFocus()
                true
            } else {
                false
            }
        }
        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                binding.chapterShortcuts.isVisible = position == 0
                searchCurrentPage()
            }
        })
        binding.more.setOnClickListener {
            val activity = host ?: return@setOnClickListener
            dismissNow()
            activity.openTocManagement()
        }
        arguments?.getString("bookUrl")?.let(viewModel::initBook)
    }

    private fun chapterList() = childFragmentManager.fragments
        .filterIsInstance<ChapterListFragment>().firstOrNull()

    private fun collapse() {
        val activity = host
        dismiss()
        activity?.showMenuBar()
    }

    private fun searchCurrentPage() {
        if (binding.viewPager.currentItem == 0) {
            viewModel.startChapterListSearch(viewModel.searchKey)
        } else {
            viewModel.startBookmarkSearch(viewModel.searchKey)
        }
    }

    fun openChapter(index: Int, position: Int = 0) {
        ReadBook.openChapter(index, position)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!isEmbeddedReadingPanel) host?.let { it.bottomDialog-- }
    }

    @Suppress("DEPRECATION")
    private inner class Pages : FragmentPagerAdapter(
        childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        override fun getCount() = 2
        override fun getItem(position: Int): Fragment =
            if (position == 0) ChapterListFragment() else BookmarkFragment()

        override fun getPageTitle(position: Int): CharSequence =
            getString(if (position == 0) R.string.chapter_list else R.string.bookmark)
    }
}
