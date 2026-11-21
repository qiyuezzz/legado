package io.legado.app.ui.widget.dialog

import android.app.Dialog
import android.content.Context
import io.legado.app.databinding.DialogWaitBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.lib.theme.setFloatingPanelBackground
import io.legado.app.utils.setBackgroundKeepPadding


@Suppress("unused")
class WaitDialog(context: Context) : Dialog(context) {

    val binding = DialogWaitBinding.inflate(layoutInflater)

    init {
        setCanceledOnTouchOutside(false)
        setContentView(binding.root)
        if (!AppConfig.isEInkMode) {
            window?.decorView?.setBackgroundKeepPadding(android.R.color.transparent)
            binding.root.setFloatingPanelBackground(context.backgroundColor)
        }
    }

    fun setText(text: String): WaitDialog {
        binding.tvMsg.text = text
        return this
    }

    fun setText(res: Int): WaitDialog {
        binding.tvMsg.setText(res)
        return this
    }

}
