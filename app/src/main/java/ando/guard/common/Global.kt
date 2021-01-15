package ando.guard.common

import ando.dialog.core.DialogManager
import ando.guard.R
import ando.guard.utils.StatusBarUtils
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

const val FILE_BLOCKED_NUMBERS = "blockednumbers"

fun Activity.supportImmersion() {
    StatusBarUtils.setStatusBarColor(
        window, if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getColor(R.color.color_main_theme)
        } else {
            resources.getColor(R.color.color_main_theme)
        }, 1
    )
}

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
    showAlert(context, title, msg, "确定", "取消", true, block)
}

fun showAlert(
    context: Context,
    title: String,
    msg: String,
    positiveText: String,
    negativeText: String,
    cancelable: Boolean,
    block: (isPositive: Boolean) -> Unit
) {
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
        .apply {
            GLOBAL_DIALOG?.dismiss()
            GLOBAL_DIALOG = null
        }
        .create()
        .apply {
            show()
        }
}


fun showLoadingDialog(context: Context, stringResId: Int = R.string.str_dialog_loading) =
    showLoadingDialog(context, context.getString(stringResId))

fun showLoadingDialog(context: Context, text: String) {
    val width = context.resources.getDimensionPixelSize(R.dimen.dimen_dialog_loading_width)
    val height = context.resources.getDimensionPixelSize(R.dimen.dimen_dialog_loading_height)
    DialogManager.with(context, R.style.CustomLoadingDialog)
        .setContentView(R.layout.layout_ando_dialog_loading) { v ->
            v.findViewById<View>(R.id.progressbar_ando_dialog_loading).visibility = View.VISIBLE
            v.findViewById<TextView>(R.id.tv_ando_dialog_loading_text).text = text
        }
        .setSize(width, height)
        .setCancelable(true)
        .setCanceledOnTouchOutside(false)
        .show()
}
