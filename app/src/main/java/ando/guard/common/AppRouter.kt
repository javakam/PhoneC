package ando.guard.common

import ando.guard.base.BaseActivity
import ando.guard.block.ui.BlockedNumbersActivity
import ando.guard.contact.ContactActivity
import android.content.Intent

/**
 * Title: AppRouter
 * <p>
 * Description:
 * </p>
 * @author javakam
 * @date 2020/12/29  15:28
 */
object AppRouter {

    fun toContactActivity(activity: BaseActivity) {
        activity.startActivity(Intent(activity, ContactActivity::class.java))
    }

    fun toBlockedNumbersActivity(activity: BaseActivity) {
        activity.startActivity(Intent(activity, BlockedNumbersActivity::class.java))
    }
}