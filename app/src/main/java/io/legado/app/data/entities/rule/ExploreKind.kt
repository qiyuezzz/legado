package io.legado.app.data.entities.rule

/**
 * 发现分类
 */
data class ExploreKind(
    val title: String = "",
    val url: String? = null,
    val style: FlexChildStyle? = null,
    val keywordPrompt: String? = null,
    val filters: List<ExploreFilter>? = null
) {

    fun style(): FlexChildStyle {
        return style ?: FlexChildStyle.defaultStyle
    }

}
