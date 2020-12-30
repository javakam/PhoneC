package ando.guard.utils

import ando.guard.App
import ando.guard.R
import ando.guard.common.toastShort
import ando.guard.database.BlockedNumber
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.role.RoleManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.BlockedNumberContract.BlockedNumbers
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

/**
 * Used to get blocked contacts.
 *
 * **Can be used only starting from android N.**
 *
 * Your app should be default dialer to use this, or it will fail with [SecurityException].
 */

const val REQUEST_CODE_SET_DEFAULT_DIALER = 0x10

private val context: Context = App.INSTANCE

private val contentResolver: ContentResolver get() = context.contentResolver

object BlockedContactsManager {

    @TargetApi(Build.VERSION_CODES.M)
    fun isDefaultDialer() =
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager).defaultDialerPackage == context.packageName

    @SuppressLint("QueryPermissionsNeeded")
    @TargetApi(Build.VERSION_CODES.M)
    fun launchSetDefaultDialerIntent(activity: AppCompatActivity) {
        Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
            TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
            activity.packageName
        ).apply {
            if (resolveActivity(activity.packageManager) != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val rm: RoleManager? = activity.getSystemService(RoleManager::class.java)

                    if (rm?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) {
                        @Suppress("DEPRECATION")
                        activity.startActivityForResult(
                            rm.createRequestRoleIntent(RoleManager.ROLE_DIALER),
                            REQUEST_CODE_SET_DEFAULT_DIALER
                        )
                    }
                } else {
                    @Suppress("DEPRECATION")
                    activity.startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                }
            } else {
                activity.toastShort(R.string.no_contacts_found)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun addBlockedNumber(number: String) {
        //检查当前用户是否支持阻止号码。通常，一次仅支持一个用户使用阻止号码。
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return

        ContentValues().apply {
            put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
            try {
                contentResolver.insert(BlockedNumbers.CONTENT_URI, this)
            } catch (e: Exception) {
                //
            }
        }
    }

    //update
    //Updates are not supported. Use Delete, and Insert instead.

    @TargetApi(Build.VERSION_CODES.N)
    fun deleteBlockedNumber(number: String) {
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return

        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        contentResolver.insert(BlockedNumbers.CONTENT_URI, values)?.let { u: Uri ->
            contentResolver.delete(u, null, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun unblockBlockedNumbers(number: String) {
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return
        if (!BlockedNumberContract.isBlocked(context, number)) return

        ThreadUtils.executeByCpu(ThreadTask({
            BlockedNumberContract.unblock(context, number)
        }, {
        }))
    }

    /**
     * Gets all blocked contacts.
     *
     * I feel like it is safe to think, that all numbers in that list are unique, but it is handled on android side.
     *
     * In case of error, it can be handled in [onError], and will return empty list.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun getBlockedNumbersSimplify(onError: (Exception) -> Unit? = {}): List<String> {
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return emptyList()

        return try {
            context.contentResolver.query(
                BlockedNumbers.CONTENT_URI,
                arrayOf(BlockedNumbers.COLUMN_ORIGINAL_NUMBER), null, null, null
            )?.run {
                val result = mutableListOf<String>()
                while (moveToNext()) {
                    getString(getColumnIndex(BlockedNumbers.COLUMN_ORIGINAL_NUMBER))?.let {
                        result.add(it)
                    }
                }
                result
            } ?: emptyList()
        } catch (exception: Exception) {
            onError(exception)
            emptyList()
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun getBlockedNumbers(onError: (Exception) -> Unit? = {}): List<BlockedNumber> {
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return emptyList()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isDefaultDialer()) return emptyList()

        try {
            val uri = BlockedNumbers.CONTENT_URI
            val projection = arrayOf(
                BlockedNumbers.COLUMN_ID,
                BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                BlockedNumbers.COLUMN_E164_NUMBER
            )
            contentResolver.query(uri, projection, null, null, null)?.use { c: Cursor ->
                val blockedNumbers = ArrayList<BlockedNumber>()
                if (c.moveToFirst()) {
                    do {
                        val id = c.getLongValue(BlockedNumbers.COLUMN_ID)
                        val number = c.getStringValue(BlockedNumbers.COLUMN_ORIGINAL_NUMBER) ?: ""
                        val normalizedNumber =
                            c.getStringValue(BlockedNumbers.COLUMN_E164_NUMBER) ?: ""
                        val blockedNumber = BlockedNumber(bid = id, number, normalizedNumber)
                        blockedNumbers.add(blockedNumber)
                    } while (c.moveToNext())
                }
                return blockedNumbers
            }
        } catch (exception: Exception) {
            onError(exception)
            return emptyList()
        }
        return emptyList()
    }

}