package io.legado.app.ui.book.read.config

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import io.legado.app.base.BaseDialogFragment

/** Existing reading controls can also be hosted inside the shared reading panel. */
abstract class ReadingPanelFragment(@LayoutRes layoutId: Int) : BaseDialogFragment(layoutId) {
    protected val isEmbeddedReadingPanel get() = parentFragment is ReadingPanelDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isEmbeddedReadingPanel) {
            view.layoutParams?.let { params ->
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                view.layoutParams = params
            }
        }
    }

    override fun dismiss() {
        val panel = parentFragment as? ReadingPanelDialog
        if (panel != null) panel.dismiss() else super.dismiss()
    }

    override fun dismissAllowingStateLoss() {
        val panel = parentFragment as? ReadingPanelDialog
        if (panel != null) panel.dismissAllowingStateLoss() else super.dismissAllowingStateLoss()
    }

    override fun dismissNow() {
        val panel = parentFragment as? ReadingPanelDialog
        if (panel != null) panel.dismissNow() else super.dismissNow()
    }
}
