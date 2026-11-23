package io.legado.app.ui.book.explore

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.legado.app.BuildConfig
import io.legado.app.base.BaseViewModel
import io.legado.app.constant.AppLog
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.SearchBook
import io.legado.app.data.entities.rule.ExploreFilter
import io.legado.app.help.book.isNotShelf
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.model.webBook.WebBook
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.stackTraceStr
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap


@OptIn(ExperimentalCoroutinesApi::class)
class ExploreShowViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : BaseViewModel(application) {
    val bookshelf: MutableSet<String> = ConcurrentHashMap.newKeySet()
    val upAdapterLiveData = MutableLiveData<String>()
    val booksData = MutableLiveData<List<SearchBook>?>()
    val errorLiveData = MutableLiveData<String?>()
    var filters: List<ExploreFilter> = emptyList()
        private set

    private var loadRequest: Coroutine<List<SearchBook>>? = null
    private var bookSource: BookSource? = null
    private var exploreUrl: String? = null
    private var page = 1
    private val books = linkedSetOf<SearchBook>()
    private var selectedFilters = IntArray(0)

    init {
        execute {
            appDb.bookDao.flowAll().mapLatest { books ->
                val keys = arrayListOf<String>()
                books.filterNot { it.isNotShelf }
                    .forEach {
                        keys.add("${it.name}-${it.author}")
                        keys.add(it.name)
                        keys.add(it.bookUrl)
                    }
                keys
            }.catch {
                AppLog.put("发现列表界面获取书籍数据失败\n${it.localizedMessage}", it)
            }.collect {
                bookshelf.clear()
                bookshelf.addAll(it)
                upAdapterLiveData.postValue("isInBookshelf")
            }
        }.onError {
            AppLog.put("加载书架数据失败", it)
        }
    }

    fun initData(intent: Intent) {
        if (exploreUrl != null) return
        val sourceUrl = intent.getStringExtra("sourceUrl") ?: return
        exploreUrl = intent.getStringExtra("exploreUrl") ?: return
        filters = IntentCompat.getParcelableArrayListExtra(
            intent, "exploreFilters", ExploreFilter::class.java
        ).orEmpty().filter { it.key.isNotBlank() && it.options.isNotEmpty() }
        val restored = savedState.get<IntArray>("selectedFilters")
        selectedFilters = IntArray(filters.size) { index ->
            restored?.getOrNull(index)?.takeIf { it in filters[index].options.indices } ?: 0
        }
        execute {
            appDb.bookSourceDao.getBookSource(sourceUrl)
        }.onSuccess {
            bookSource = it
            explore()
        }.onError {
            errorLiveData.value = it.stackTraceStr
        }
    }

    fun selectedOption(index: Int): Int = selectedFilters[index]

    fun selectFilter(index: Int, option: Int) {
        val filter = filters.getOrNull(index) ?: return
        if (option !in filter.options.indices || selectedFilters[index] == option) return

        selectedFilters[index] = option
        savedState["selectedFilters"] = selectedFilters.copyOf()
        loadRequest?.cancel()
        loadRequest = null
        books.clear()
        page = 1
        errorLiveData.value = null
        booksData.value = null
        explore()
    }

    fun explore() {
        if (loadRequest?.isActive == true) return
        val source = bookSource ?: return
        var url = exploreUrl ?: return
        filters.forEachIndexed { index, filter ->
            val option = filter.options[selectedFilters[index]]
            url = url.replace("{{filter:${filter.key}}}", Uri.encode(option.value))
        }
        loadRequest = WebBook.exploreBook(viewModelScope, source, url, page)
            .timeout(if (BuildConfig.DEBUG) 0L else 30000L)
            .onSuccess { searchBooks ->
                withContext(IO) { appDb.searchBookDao.insert(*searchBooks.toTypedArray()) }
                books.addAll(searchBooks)
                page++
                errorLiveData.value = null
                booksData.value = books.toList()
            }.onError {
                it.printOnDebug()
                errorLiveData.value = it.stackTraceStr
            }
    }

    fun isInBookShelf(book: SearchBook): Boolean {
        val name = book.name
        val author = book.author
        val bookUrl = book.bookUrl
        val key = if (author.isNotBlank()) "$name-$author" else name
        return bookshelf.contains(key) || bookshelf.contains(bookUrl)
    }

}
