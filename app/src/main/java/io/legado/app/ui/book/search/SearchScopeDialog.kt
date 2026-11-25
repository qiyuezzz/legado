package io.legado.app.ui.book.search

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.constant.AppLog
import io.legado.app.data.AppDatabase
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookSourcePart
import io.legado.app.databinding.DialogSearchScopeBinding
import io.legado.app.databinding.ItemCheckBoxBinding
import io.legado.app.databinding.ItemRadioButtonBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.dialogPill
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.overlaySurfaceColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.applyTint
import io.legado.app.utils.flowWithLifecycleAndDatabaseChange
import io.legado.app.utils.getCompatColor
import io.legado.app.utils.setLayout
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchScopeDialog : BaseDialogFragment(R.layout.dialog_search_scope) {

    private val binding by viewBinding(DialogSearchScopeBinding::bind)
    private var sourceFlowJob: Job? = null
    val callback: Callback get() = parentFragment as? Callback ?: activity as Callback
    var groups: List<String> = emptyList()
    val screenSources = arrayListOf<BookSourcePart>()
    var screenText: String? = null

    val adapter by lazy {
        RecyclerAdapter()
    }

    override fun onStart() {
        super.onStart()
        setLayout(0.9f, 0.8f)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val surface = if (AppConfig.isEInkMode) Color.WHITE else requireContext().getCompatColor(R.color.surface_card)
        val panelSurface = if (AppConfig.isEInkMode) Color.WHITE else requireContext().overlaySurfaceColor
        val ink = if (AppConfig.isEInkMode) Color.BLACK else {
            requireContext().getPrimaryTextColor(ColorUtils.isColorLight(panelSurface))
        }
        val soft = AndroidColorUtils.blendARGB(panelSurface, ink, 0.07f)
        val active = AndroidColorUtils.blendARGB(panelSurface, ink, 0.19f)
        val optionTextColors = if (AppConfig.isEInkMode) {
            Selector.colorBuild()
                .setDefaultColor(Color.BLACK)
                .setCheckedColor(Color.WHITE)
                .setSelectedColor(Color.WHITE)
                .create()
        } else {
            null
        }
        binding.toolBar.setBackgroundColor(surface)
        binding.rgScope.background = dialogPill(soft, soft)
        binding.rgScope.children.filterIsInstance<RadioButton>().forEach { option ->
            option.background = dialogPill(Color.TRANSPARENT, active)
            if (optionTextColors != null) {
                option.setTextColor(optionTextColors)
            } else {
                option.setTextColor(ink)
            }
        }
        binding.recyclerView.adapter = adapter
        initMenu()
        initSearchView()
        initOtherView()
        initData()
    }

    private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.book_search_scope)
        binding.toolBar.menu.applyTint(requireContext())
    }

    private fun initSearchView() {
        val searchView = binding.toolBar.menu.findItem(R.id.menu_screen).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                screenText = newText
                upData()
                return false
            }

        })
    }

    private fun initOtherView() {
        binding.rgScope.setOnCheckedChangeListener { _, checkedId ->
            binding.toolBar.menu.findItem(R.id.menu_screen)?.isVisible = checkedId == R.id.rb_source
            upData()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvAllSource.setOnClickListener {
            callback.onSearchScopeOk(SearchScope(""))
            dismiss()
        }
        binding.tvOk.setOnClickListener {
            if (binding.rbGroup.isChecked) {
                callback.onSearchScopeOk(SearchScope(adapter.selectGroups))
            } else {
                val selectSource = adapter.selectSource
                if (selectSource != null) {
                    callback.onSearchScopeOk(SearchScope(selectSource))
                } else {
                    callback.onSearchScopeOk(SearchScope(""))
                }
            }
            dismiss()
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            groups = withContext(IO) {
                appDb.bookSourceDao.allEnabledGroups()
            }
            upData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun upData() {
        if (binding.rbSource.isChecked) {
            upBookSource(screenText)
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun upBookSource(searchKey: String? = null) {
        sourceFlowJob?.cancel()
        sourceFlowJob = lifecycleScope.launch {
            when {
                searchKey.isNullOrEmpty() -> {
                    appDb.bookSourceDao.flowAll()
                }

                else -> {
                    appDb.bookSourceDao.flowSearch(searchKey)
                }
            }.flowWithLifecycleAndDatabaseChange(
                lifecycle,
                table = AppDatabase.BOOK_SOURCE_TABLE_NAME
            ).catch {
                AppLog.put("多分组/书源界面更新书源出错", it)
            }.flowOn(IO).conflate().collect { data ->
                screenSources.clear()
                screenSources.addAll(data)
                adapter.notifyDataSetChanged()
                delay(500)
            }
        }
    }

    inner class RecyclerAdapter : RecyclerView.Adapter<ItemViewHolder>() {

        val selectGroups = arrayListOf<String>()
        var selectSource: BookSourcePart? = null

        override fun getItemViewType(position: Int): Int {
            return if (binding.rbSource.isChecked) {
                1
            } else {
                0
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            return if (viewType == 1) {
                ItemViewHolder(ItemRadioButtonBinding.inflate(layoutInflater, parent, false))
            } else {
                ItemViewHolder(ItemCheckBoxBinding.inflate(layoutInflater, parent, false))
            }
        }

        override fun onBindViewHolder(
            holder: ItemViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
            } else {
                when (holder.binding) {
                    is ItemCheckBoxBinding -> {
                        groups.getOrNull(position)?.let {
                            holder.binding.checkBox.isChecked = selectGroups.contains(it)
                            holder.binding.checkBox.text = it
                        }
                    }

                    is ItemRadioButtonBinding -> {
                        screenSources.getOrNull(position)?.let {
                            holder.binding.radioButton.isChecked = selectSource == it
                            holder.binding.radioButton.text = it.bookSourceName
                        }
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            when (holder.binding) {
                is ItemCheckBoxBinding -> {
                    groups.getOrNull(position)?.let {
                        holder.binding.checkBox.isChecked = selectGroups.contains(it)
                        holder.binding.checkBox.text = it
                        holder.binding.checkBox.setOnUserCheckedChangeListener { isChecked ->
                            if (isChecked) {
                                selectGroups.add(it)
                            } else {
                                selectGroups.remove(it)
                            }
                            holder.itemView.post {
                                notifyItemRangeChanged(0, itemCount, "up")
                            }
                        }
                    }
                }

                is ItemRadioButtonBinding -> {
                    screenSources.getOrNull(position)?.let {
                        holder.binding.radioButton.isChecked = selectSource == it
                        holder.binding.radioButton.text = it.bookSourceName
                        holder.binding.radioButton.setOnUserCheckedChangeListener { isChecked ->
                            if (isChecked) {
                                selectSource = it
                            }
                            holder.itemView.post {
                                notifyItemRangeChanged(0, itemCount, "up")
                            }
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return if (binding.rbSource.isChecked) {
                screenSources.size
            } else {
                groups.size
            }
        }

    }

    interface Callback {

        /**
         * 搜索范围确认
         */
        fun onSearchScopeOk(searchScope: SearchScope)

    }

}