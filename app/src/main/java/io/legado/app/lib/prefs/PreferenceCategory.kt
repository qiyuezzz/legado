package io.legado.app.lib.prefs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.utils.ColorUtils


class PreferenceCategory(context: Context, attrs: AttributeSet) :
    PreferenceCategory(context, attrs) {

    init {
        isPersistent = true
        layoutResource = R.layout.view_preference_category
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val view = holder.findViewById(R.id.preference_title)
        if (view is TextView) {
            view.text = title
            view.isVisible = !title.isNullOrEmpty()
            if (view.isInEditMode) return
            view.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_muted))
            holder.findViewById(R.id.preference_divider_above)?.isVisible = false
            holder.findViewById(R.id.preference_divider_below)?.isVisible = false
        }
    }

}
