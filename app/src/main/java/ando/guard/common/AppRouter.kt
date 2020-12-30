package ando.guard.common

import ando.guard.base.BaseActivity
import ando.guard.contact.ContactManager
import ando.guard.ui.blocked.BlockedNumbersActivity
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

    fun toContactManagerActivity(activity: BaseActivity) {
        activity.startActivity(Intent(activity, ContactManager::class.java))
    }

    fun toBlockedNumbersActivity(activity: BaseActivity) {
        activity.startActivity(Intent(activity, BlockedNumbersActivity::class.java))
    }
}