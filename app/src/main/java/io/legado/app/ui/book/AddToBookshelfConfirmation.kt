package io.legado.app.ui.book

import android.content.Context
import io.legado.app.R
import io.legado.app.lib.dialogs.AndroidAlertBuilder

internal fun Context.showAddToBookshelfConfirmation(
    bookName: String,
    onAdd: () -> Unit,
    onDecline: () -> Unit
) {
    AndroidAlertBuilder(this, emphasizePositiveAction = true).apply {
        setTitle(R.string.add_to_bookshelf)
        setMessage(getString(R.string.check_add_bookshelf, bookName))
        okButton { onAdd() }
        noButton { onDecline() }
    }.show()
}
