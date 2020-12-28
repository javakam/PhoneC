package ando.guard

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

var GLOBAL_DIALOG: AlertDialog? = null

fun Context.toastShort(msg: String?) {
    msg?.let {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
}

fun Context.toastLong(msg: String?) {
    msg?.let {
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
    }
}

fun showAlert(context: Context, title: String, msg: String, block: (isPositive: Boolean) -> Unit) {
    GLOBAL_DIALOG?.dismiss()
    GLOBAL_DIALOG = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton("打开") { _, _ ->
            block.invoke(true)
        }
        .setNegativeButton("取消") { _, _ ->
            block.invoke(false)
        }
        .create()

    GLOBAL_DIALOG?.show()
}