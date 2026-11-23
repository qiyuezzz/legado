package io.legado.app.ui.book.explore

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.SearchBook
import io.legado.app.databinding.ActivityExploreShowBinding
import io.legado.app.databinding.ItemFilletTextBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.lib.dialogs.selector
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.CardSpacingItemDecoration
import io.legado.app.utils.getCompatColor
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.dpToPx
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding

/**
 * 发现列表
 */
class ExploreShowActivity : VMBaseActivity<ActivityExploreShowBinding, ExploreShowViewModel>(),
    ExploreShowAdapter.CallBack {
    override val binding by viewBinding(ActivityExploreShowBinding::inflate)
    override val viewModel by viewModels<ExploreShowViewModel>()

    private val adapter by lazy { ExploreShowAdapter(this, this) }
    private val loadMoreView by lazy { LoadMoreView(this) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.titleBar.title = intent.getStringExtra("exploreName")
        initRecyclerView()
        viewModel.booksData.observe(this) {
            if (it == null) {
                loadMoreView.hasMore()
                adapter.setItems(emptyList())
                binding.recyclerView.scrollToPosition(0)
            } else {
                upData(it)
            }
        }
        viewModel.initData(intent)
        initFilters()
        viewModel.errorLiveData.observe(this) {
            if (it != null) loadMoreView.error(it)
        }
        viewModel.upAdapterLiveData.observe(this) {
            adapter.notifyItemRangeChanged(0, adapter.itemCount, bundleOf(it to null))
        }
    }

    private fun initFilters() {
        binding.filterScroll.isVisible = viewModel.filters.isNotEmpty()
        viewModel.filters.forEachIndexed { index, filter ->
            val chip = ItemFilletTextBinding.inflate(layoutInflater, binding.filterBar, false).root
            chip.minimumHeight = 48.dpToPx()

            fun updateLabel() {
                val option = filter.options[viewModel.selectedOption(index)]
                chip.text = "${filter.title} · ${option.title}"
            }

            updateLabel()
            chip.setOnClickListener {
                selector(filter.title, filter.options.map { it.title }) { _, position ->
                    viewModel.selectFilter(index, position)
                    updateLabel()
                }
            }
            binding.filterBar.addView(chip)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.setBackgroundColor(getCompatColor(R.color.surface_canvas))
        binding.recyclerView.addItemDecoration(CardSpacingItemDecoration())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.applyNavigationBarPadding()
        adapter.addFooterView {
            ViewLoadMoreBinding.bind(loadMoreView)
        }
        loadMoreView.startLoad()
        loadMoreView.setOnClickListener {
            if (!loadMoreView.isLoading) {
                scrollToBottom(true)
            }
        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    scrollToBottom()
                }
            }
        })
    }

    private fun scrollToBottom(forceLoad: Boolean = false) {
        if ((loadMoreView.hasMore && !loadMoreView.isLoading) || forceLoad) {
            loadMoreView.hasMore()
            viewModel.explore()
        }
    }

    private fun upData(books: List<SearchBook>) {
        loadMoreView.stopLoad()
        if (books.isEmpty() && adapter.isEmpty()) {
            loadMoreView.noMore(getString(R.string.empty))
        } else if (adapter.getActualItemCount() == books.size) {
            loadMoreView.noMore()
        } else {
            adapter.setItems(books)
        }
    }

    override fun isInBookshelf(book: SearchBook): Boolean {
        return viewModel.isInBookShelf(book)
    }

    override fun showBookInfo(book: SearchBook) {
        startActivity<BookInfoActivity> {
            putExtra("name", book.name)
            putExtra("author", book.author)
            putExtra("bookUrl", book.bookUrl)
        }
    }
}
