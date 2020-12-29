package ando.guard

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun Context.toastShort(msg: Int) = toastShort(getString(msg))

fun Context.toastShort(msg: String?) {
    msg?.let {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
}

fun Context.toastLong(msg: Int) = toastLong(getString(msg))

fun Context.toastLong(msg: String?) {
    msg?.let {
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
    }
}

var GLOBAL_DIALOG: AlertDialog? = null
fun showAlert(context: Context, title: String, msg: String, block: (isPositive: Boolean) -> Unit) {
    showAlert(context, title, msg, "确定", "取消",true, block)
}

fun showAlert(
    context: Context,
    title: String,
    msg: String,
    positiveText: String,
    negativeText: String,
    cancelable:Boolean,
    block: (isPositive: Boolean) -> Unit
) {
    GLOBAL_DIALOG?.dismiss()
    GLOBAL_DIALOG = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(msg)
        .setCancelable(cancelable)
        .setPositiveButton(positiveText) { _, _ ->
            block.invoke(true)
        }
        .setNegativeButton(negativeText) { _, _ ->
            block.invoke(false)
        }
        .create()

    GLOBAL_DIALOG?.show()
}