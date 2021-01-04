package ando.guard.block.ui

import ando.guard.R
import ando.guard.base.BaseActivity
import ando.guard.block.db.BlockedNumber
import ando.guard.block.BlockedNumbersUtils
import ando.guard.utils.hideSoftInput
import ando.guard.utils.showKeyboard
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class BlockedNumberDialog(
    activity: BaseActivity,
    private val originalNumber: BlockedNumber? = null,
    private val callback: (number: String?) -> Unit
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
            .setNeutralButton(
                "移出黑名单"
            ) { dialog, _ ->
                dialog.dismiss()
                activity.hideSoftInput(activity)
                callback.invoke("delete")
            }
            .setPositiveButton(
                R.string.ok
            ) { dialog, _ ->
                dialog.dismiss()
                activity.hideSoftInput(activity)

                val newBlockedNumber = editText.text.toString()
                if (originalNumber != null && newBlockedNumber != originalNumber.number) {
                    BlockedNumbersUtils.deleteBlockedNumber(originalNumber.number)
                }
                if (newBlockedNumber.isNotEmpty()) {
                    BlockedNumbersUtils.addBlockedNumber(newBlockedNumber)
                }
                callback.invoke(newBlockedNumber)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                activity.hideSoftInput(activity)
            }
            .create()
            .show()

        showKeyboard(activity.window, editText)
    }
}