package io.legado.app.ui.main.bookshelf

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.databinding.DialogBookshelfBookActionBinding
import io.legado.app.help.book.isLocal
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.overlaySurfaceColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 书架书籍长按快捷操作面板
 */
class BookActionDialog() : BaseDialogFragment(R.layout.dialog_bookshelf_book_action) {

    constructor(book: Book) : this() {
        currentBook = book
        arguments = Bundle().apply {
            putString(ARG_BOOK_URL, book.bookUrl)
        }
    }

    private val binding by viewBinding(DialogBookshelfBookActionBinding::bind)
    private var currentBook: Book? = null
    private val callback: Callback?
        get() = (parentFragment as? Callback) ?: (activity as? Callback)

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            WindowCompat.setDecorFitsSystemWindows(this, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                attributes = attributes.apply {
                    setFitInsetsTypes(0)
                }
            }
            decorView.setPadding(0, 0, 0, 0)
            setGravity(Gravity.BOTTOM)
            setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            if (!AppConfig.isEInkMode) {
                val surface = requireContext().getCompatColor(R.color.surface_card)
                @Suppress("DEPRECATION")
                navigationBarColor = Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isNavigationBarContrastEnforced = false
                }
                WindowInsetsControllerCompat(this, decorView).isAppearanceLightNavigationBars =
                    ColorUtils.isColorLight(surface)
            }
        }
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        applyPanelStyle()
        binding.root.applyNavigationBarPadding(withInitialPadding = true)
        val initialBook = currentBook
        if (initialBook != null) {
            bindBook(initialBook)
        } else {
            val bookUrl = arguments?.getString(ARG_BOOK_URL)
            if (bookUrl.isNullOrEmpty()) {
                dismissAllowingStateLoss()
                return
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val loaded = withContext(Dispatchers.IO) {
                    appDb.bookDao.getBook(bookUrl)
                }
                if (loaded == null) {
                    dismissAllowingStateLoss()
                } else {
                    currentBook = loaded
                    bindBook(loaded)
                }
            }
        }
    }

    private fun applyPanelStyle() = binding.run {
        val isEInk = AppConfig.isEInkMode
        val surface = if (isEInk) Color.WHITE else requireContext().getCompatColor(R.color.surface_card)
        val primaryInk = if (isEInk) Color.BLACK else {
            requireContext().getPrimaryTextColor(ColorUtils.isColorLight(surface))
        }
        val secondaryInk = if (isEInk) Color.BLACK else {
            AndroidColorUtils.setAlphaComponent(primaryInk, 0x99)
        }

        if (!isEInk) {
            val radius = 20.dpToPx().toFloat()
            llRoot.background = GradientDrawable().apply {
                setColor(surface)
                cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            }
            llRoot.clipToOutline = true
        }

        vwHandle.background = GradientDrawable().apply {
            setColor(if (isEInk) Color.BLACK else AndroidColorUtils.setAlphaComponent(primaryInk, 0x2E))
            cornerRadius = 2.dpToPx().toFloat()
        }
        vwDivider.setBackgroundColor(
            if (isEInk) Color.BLACK else AndroidColorUtils.setAlphaComponent(primaryInk, 0x1A)
        )

        tvName.setTextColor(primaryInk)
        tvSubtitle.setTextColor(secondaryInk)
        tvChapter.setTextColor(secondaryInk)
        ivArrow.imageTintList = ColorStateList.valueOf(secondaryInk)

        val tileFill = AndroidColorUtils.blendARGB(surface, primaryInk, 0.06f)
        val tilePressed = AndroidColorUtils.blendARGB(surface, primaryInk, 0.16f)
        val tileBorder = Color.BLACK

        val iconContainers = listOf(
            flIconInfo,
            flIconTop,
            flIconGroup,
            flIconUpdate,
            flIconManage
        )
        iconContainers.forEach { container ->
            container.background = if (isEInk) {
                Selector.shapeBuild()
                    .setCornerRadius(14.dpToPx())
                    .setDefaultBgColor(Color.WHITE)
                    .setStrokeWidth(1.dpToPx())
                    .setDefaultStrokeColor(tileBorder)
                    .create()
            } else {
                Selector.shapeBuild()
                    .setCornerRadius(14.dpToPx())
                    .setDefaultBgColor(tileFill)
                    .setPressedBgColor(tilePressed)
                    .create()
            }
        }

        val actionIcons = listOf(
            ivIconInfo,
            ivIconTop,
            ivIconGroup,
            ivIconUpdate,
            ivIconManage
        )
        actionIcons.forEach { icon ->
            icon.imageTintList = ColorStateList.valueOf(primaryInk)
        }

        val actionLabels = listOf(
            tvActionInfo,
            tvActionTop,
            tvActionGroup,
            tvActionUpdate,
            tvActionManage
        )
        actionLabels.forEach { label ->
            label.setTextColor(primaryInk)
        }

        val dangerColor = if (isEInk) Color.BLACK else requireContext().getCompatColor(R.color.error)
        ivIconDelete.imageTintList = ColorStateList.valueOf(dangerColor)
        tvActionDelete.setTextColor(dangerColor)
        llActionDelete.background = if (isEInk) {
            Selector.shapeBuild()
                .setCornerRadius(24.dpToPx())
                .setDefaultBgColor(Color.WHITE)
                .setStrokeWidth(1.dpToPx())
                .setDefaultStrokeColor(Color.BLACK)
                .create()
        } else {
            Selector.shapeBuild()
                .setCornerRadius(24.dpToPx())
                .setDefaultBgColor(AndroidColorUtils.blendARGB(surface, dangerColor, 0.10f))
                .setPressedBgColor(AndroidColorUtils.blendARGB(surface, dangerColor, 0.22f))
                .setFocusedBgColor(AndroidColorUtils.blendARGB(surface, dangerColor, 0.22f))
                .create()
        }
    }

    private fun bindBook(book: Book) = binding.run {
        ivCover.load(book.getDisplayCover(), book.name, book.author, false, book.origin)
        tvName.text = book.name

        val author = book.getRealAuthor()
        val originLabel = if (book.isLocal) getString(R.string.local_book) else book.originName
        val subtitle = listOf(author, originLabel).filter { it.isNotBlank() }.joinToString(" · ")
        tvSubtitle.text = subtitle
        tvSubtitle.isGone = subtitle.isEmpty()

        val chapterTitle = book.durChapterTitle.orEmpty()
        tvChapter.text = chapterTitle
        tvChapter.isGone = chapterTitle.isBlank()

        llActionUpdate.isGone = book.isLocal

        val openInfo = View.OnClickListener {
            dispatchAction { onOpenBookInfo(book) }
        }
        clHeader.setOnClickListener(openInfo)
        llActionInfo.setOnClickListener(openInfo)
        llActionTop.setOnClickListener {
            dispatchAction { onTopBook(book) }
        }
        llActionGroup.setOnClickListener {
            dispatchAction { onChangeGroup(book) }
        }
        llActionUpdate.setOnClickListener {
            dispatchAction { onUpdateToc(book) }
        }
        llActionManage.setOnClickListener {
            dispatchAction { onOpenBookshelfManage() }
        }
        llActionDelete.setOnClickListener {
            dispatchAction { onDeleteBook(book) }
        }
    }

    private inline fun dispatchAction(action: Callback.() -> Unit) {
        val cb = callback
        dismissAllowingStateLoss()
        cb?.action()
    }

    interface Callback {
        fun onOpenBookInfo(book: Book)
        fun onTopBook(book: Book)
        fun onChangeGroup(book: Book)
        fun onUpdateToc(book: Book)
        fun onOpenBookshelfManage()
        fun onDeleteBook(book: Book)
    }

    companion object {
        private const val ARG_BOOK_URL = "bookUrl"
    }
}
