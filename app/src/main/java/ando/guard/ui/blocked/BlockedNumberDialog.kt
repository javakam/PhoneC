package ando.guard.ui.blocked

import ando.guard.R
import ando.guard.base.BaseActivity
import ando.guard.database.BlockedNumber
import ando.guard.utils.BlockedContactsManager
import ando.guard.utils.hideSoftInput
import ando.guard.utils.showKeyboard
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class BlockedNumberDialog(
    activity: BaseActivity,
    private val originalNumber: BlockedNumber? = null,
    private val callback: (number:String?) -> Unit
) {
    init {
        val view =
            activity.layoutInflater.inflate(R.layout.dialog_add_blocked_number, null, false).apply {
                if (originalNumber != null) {
                    this.findViewById<EditText>(R.id.edt_blocked_number)
                        .setText(originalNumber.number)
                }
            }

        val editText = view.findViewById<EditText>(R.id.edt_blocked_number)

        AlertDialog.Builder(activity)
            .setView(view)
            .setCancelable(true)
            .setPositiveButton(
                R.string.ok
            ) { dialog, _ ->
                val newBlockedNumber = editText.text.toString()
                if (originalNumber != null && newBlockedNumber != originalNumber.number) {
                    BlockedContactsManager.deleteBlockedNumber(originalNumber.number)
                }
                if (newBlockedNumber.isNotEmpty()) {
                    BlockedContactsManager.addBlockedNumber(newBlockedNumber)
                }
                callback.invoke(newBlockedNumber)
                activity.hideSoftInput(activity)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                activity.hideSoftInput(activity)
                dialog.dismiss()
            }
            .create()
            .show()

        showKeyboard(activity.window, editText)
    }
}