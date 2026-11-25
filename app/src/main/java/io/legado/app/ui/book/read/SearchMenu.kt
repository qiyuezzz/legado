package io.legado.app.ui.book.read

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import androidx.core.view.isVisible
import io.legado.app.R
import io.legado.app.databinding.ViewSearchMenuBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.setDialogActionBackground
import io.legado.app.lib.theme.setTopRoundedPanelBackground
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.read.config.READING_PANEL_COLOR
import io.legado.app.ui.book.searchContent.SearchResult
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.activity
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.dpToPx
import io.legado.app.utils.invisible
import io.legado.app.utils.loadAnimation
import io.legado.app.utils.visible

/**
 * 搜索界面菜单
 */
class SearchMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val callBack: CallBack get() = activity as CallBack
    private val binding = ViewSearchMenuBinding.inflate(LayoutInflater.from(context), this, true)

    private val menuBottomIn: Animation = loadAnimation(context, R.anim.anim_readbook_bottom_in)
    private val menuBottomOut: Animation = loadAnimation(context, R.anim.anim_readbook_bottom_out)
    private val bgColor: Int get() = READING_PANEL_COLOR
    private val textColor: Int get() = context.getPrimaryTextColor(ColorUtils.isColorLight(bgColor))
    private var onMenuOutEnd: (() -> Unit)? = null
    private var isMenuOutAnimating = false

    private val searchResultList: MutableList<SearchResult> = mutableListOf()
    private var currentSearchResultIndex: Int = -1
    private var lastSearchResultIndex: Int = -1
    private val hasSearchResult: Boolean
        get() = searchResultList.isNotEmpty()
    val selectedSearchResult: SearchResult?
        get() = searchResultList.getOrNull(currentSearchResultIndex)
    val previousSearchResult: SearchResult?
        get() = searchResultList.getOrNull(lastSearchResultIndex)
    val bottomMenuVisible get() = isVisible && binding.llBottomMenu.isVisible

    init {
        initAnimation()
        initView()
        bindEvent()
        updateSearchInfo()
    }

    fun upSearchResultList(resultList: List<SearchResult>) {
        searchResultList.clear()
        searchResultList.addAll(resultList)
        updateSearchInfo()
    }

    private fun initView() = binding.run {
        val surface = bgColor
        val ink = textColor
        val isEInk = AppConfig.isEInkMode
        if (isEInk) {
            llBottomMenu.setBackgroundResource(R.drawable.bg_eink_border_top)
        } else {
            llBottomMenu.setTopRoundedPanelBackground(surface)
        }
        llSearchBaseInfo.background = null
        llBottomBg.background = null
        val pillBg = if (isEInk) {
            Selector.shapeBuild()
                .setCornerRadius(20.dpToPx())
                .setDefaultBgColor(Color.WHITE)
                .setPressedBgColor(Color.LTGRAY)
                .setStrokeWidth(1.dpToPx())
                .setDefaultStrokeColor(Color.BLACK)
                .create()
        } else {
            Selector.shapeBuild()
                .setCornerRadius(20.dpToPx())
                .setDefaultBgColor(surface)
                .setPressedBgColor(AndroidColorUtils.blendARGB(surface, ink, 0.14f))
                .setStrokeWidth(1.dpToPx())
                .setDefaultStrokeColor(AndroidColorUtils.blendARGB(surface, ink, 0.16f))
                .create()
        }
        listOf(fabLeft, fabRight).forEach { fab ->
            fab.background = pillBg
            fab.setColorFilter(ink, PorterDuff.Mode.SRC_IN)
            if (isEInk) fab.elevation = 0f
        }
        listOf(llSearchResults, llMainMenu, llSearchExit).forEach {
            it.setDialogActionBackground(surface, ink)
        }
        tvMainMenu.setTextColor(ink)
        tvSearchResults.setTextColor(ink)
        tvSearchExit.setTextColor(ink)
        ivMainMenu.setColorFilter(ink, PorterDuff.Mode.SRC_IN)
        ivSearchResults.setColorFilter(ink, PorterDuff.Mode.SRC_IN)
        ivSearchExit.setColorFilter(ink, PorterDuff.Mode.SRC_IN)
        ivSearchContentUp.setColorFilter(ink, PorterDuff.Mode.SRC_IN)
        ivSearchContentDown.setColorFilter(ink, PorterDuff.Mode.SRC_IN)
        tvCurrentSearchInfo.setTextColor(ink)
        llBottomBg.applyNavigationBarPadding(withInitialPadding = true)
    }


    fun runMenuIn() {
        this.visible()
        binding.llBottomMenu.visible()
        binding.vwMenuBg.visible()
        binding.llBottomMenu.startAnimation(menuBottomIn)
    }

    fun runMenuOut(onMenuOutEnd: (() -> Unit)? = null) {
        if (isMenuOutAnimating) {
            return
        }
        this.onMenuOutEnd = onMenuOutEnd
        if (this.isVisible) {
            binding.llBottomMenu.startAnimation(menuBottomOut)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateSearchInfo() {
        ReadBook.curTextChapter?.let {
            binding.tvCurrentSearchInfo.text =
                """${context.getString(R.string.search_content_size)}: ${searchResultList.size} / 当前章节: ${it.title}"""
        }
    }

    fun updateSearchResultIndex(updateIndex: Int) {
        lastSearchResultIndex = currentSearchResultIndex
        currentSearchResultIndex = when {
            updateIndex < 0 -> 0
            updateIndex >= searchResultList.size -> searchResultList.size - 1
            else -> updateIndex
        }
    }

    private fun bindEvent() = binding.run {
        //搜索结果
        llSearchResults.setOnClickListener {
            runMenuOut {
                callBack.openSearchActivity(selectedSearchResult?.query)
            }
        }

        //主菜单
        llMainMenu.setOnClickListener {
            runMenuOut {
                callBack.cancelSelect()
                callBack.showMenuBar()
                this@SearchMenu.invisible()
            }
        }

        //退出
        llSearchExit.setOnClickListener {
            runMenuOut {
                callBack.exitSearchMenu()
            }
        }

        fabLeft.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex - 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }

        ivSearchContentUp.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex - 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }

        ivSearchContentDown.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex + 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }

        fabRight.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex + 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }
    }

    private fun initAnimation() {
        //显示菜单
        menuBottomIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                callBack.upSystemUiVisibility()
                binding.fabLeft.visible(hasSearchResult)
                binding.fabRight.visible(hasSearchResult)
            }

            @SuppressLint("RtlHardcoded")
            override fun onAnimationEnd(animation: Animation) {
                binding.vwMenuBg.setOnClickListener { runMenuOut() }
                callBack.upSystemUiVisibility()
            }

            override fun onAnimationRepeat(animation: Animation) = Unit
        })

        //隐藏菜单
        menuBottomOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isMenuOutAnimating = true
                binding.vwMenuBg.setOnClickListener(null)
            }

            override fun onAnimationEnd(animation: Animation) {
                isMenuOutAnimating = false
                binding.llBottomMenu.invisible()
                binding.vwMenuBg.invisible()
                binding.vwMenuBg.setOnClickListener { runMenuOut() }

                onMenuOutEnd?.invoke()
                callBack.upSystemUiVisibility()
            }

            override fun onAnimationRepeat(animation: Animation) = Unit
        })
    }

    interface CallBack {
        var isShowingSearchResult: Boolean
        fun openSearchActivity(searchWord: String?)
        fun showSearchSetting()
        fun upSystemUiVisibility()
        fun exitSearchMenu()
        fun showMenuBar()
        fun navigateToSearch(searchResult: SearchResult, index: Int)
        fun onMenuShow()
        fun onMenuHide()
        fun cancelSelect()
    }

}
