package io.legado.app.ui.book.read.config

import io.legado.app.ui.book.read.ReadBookActivity

internal enum class ReadingPanel { CATALOG, READ_ALOUD, STYLE, SETTINGS }

internal fun ReadBookActivity.openReadingPanel(panel: ReadingPanel) {
    val manager = supportFragmentManager
    if (manager.isStateSaved) return
    val current = manager.findFragmentByTag(ReadingPanelDialog.TAG) as? ReadingPanelDialog
    if (current != null && !current.isRemoving) {
        current.selectPanel(panel)
    } else {
        ReadingPanelDialog.create(panel).show(manager, ReadingPanelDialog.TAG)
    }
}
