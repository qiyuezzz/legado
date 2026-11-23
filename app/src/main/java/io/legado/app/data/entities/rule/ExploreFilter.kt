package io.legado.app.data.entities.rule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExploreFilter(
    val key: String = "",
    val title: String = "",
    val options: List<ExploreFilterOption> = emptyList()
) : Parcelable

@Parcelize
data class ExploreFilterOption(
    val title: String = "",
    val value: String = ""
) : Parcelable
