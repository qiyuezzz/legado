package io.legado.app.lib.prefs

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat
import androidx.preference.PreferenceDialogFragmentCompat
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.applyTint
import io.legado.app.utils.dpToPx

class MultiSelectListPreferenceDialog : MultiSelectListPreferenceDialogFragmentCompat() {

    companion object {

        fun newInstance(key: String?): MultiSelectListPreferenceDialog {
            val fragment =
                MultiSelectListPreferenceDialog()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as AlertDialog).applyTint()
    }

    override fun onStart() {
        super.onStart()
        if (AppConfig.isEInkMode) {
            dialog?.window?.let {
                it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                val attr = it.attributes
                attr.dimAmount = 0.0f
                attr.windowAnimations = 0
                it.attributes = attr
                it.setBackgroundDrawableResource(R.color.transparent)
                when (attr.gravity) {
                    Gravity.TOP -> it.decorView.setBackgroundResource(R.drawable.bg_eink_border_bottom)
                    Gravity.BOTTOM -> it.decorView.setBackgroundResource(R.drawable.bg_eink_border_top)
                    else -> {
                        val padding = 2.dpToPx();
                        it.decorView.setPadding(padding, padding, padding, padding)
                        it.decorView.setBackgroundResource(R.drawable.bg_eink_border_dialog)
                    }
                }
            }
        }
    }

}