package ando.guard.ui

import ando.guard.R
import ando.guard.utils.PermissionManager
import ando.guard.utils.TelecomTestUtils
import ando.guard.utils.ThreadTask
import ando.guard.utils.ThreadUtils
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BlockedNumberContract
import android.provider.BlockedNumberContract.BlockedNumbers
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var btBlockedAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btBlockedAdd = findViewById(R.id.bt_blocked_write)
        btBlockedAdd.setOnClickListener {
            PermissionManager.requestContactsPermission(this) {
                if (it) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        insertBlockedNumbers()
                    }
                }
            }
        }

    }

    //java.lang.SecurityException: Caller must be system, default dialer or default SMS app
    //insert
    /**
     * 检查当前用户是否支持阻止号码。
     * 通常，一次仅支持一个用户使用阻止号码。
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun insertBlockedNumbers() {
        val result = BlockedNumberContract.canCurrentUserBlockNumbers(this)
        Log.e("123", "insertBlockedNumbers $result  ")
        if (!result) return

        //TelecomTestUtils.setDefaultDialer(this, packageName + "1234")

        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, "1234567890")
        //values.put(BlockedNumbers.COLUMN_E164_NUMBER, "+11234567890")
        //values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, "12345@abdcde.com")
        val uri = contentResolver.insert(BlockedNumbers.CONTENT_URI, values)
    }

    //update
    //Updates are not supported. Use Delete, and Insert instead.

    //delete
    @RequiresApi(Build.VERSION_CODES.N)
    private fun deleteBlockedNumbers(number: String) {
        // if (!BlockedNumberContract.canCurrentUserBlockNumbers(this))return

        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, "1234567890")
        contentResolver.insert(BlockedNumbers.CONTENT_URI, values)?.let { u: Uri ->
            contentResolver.delete(u, null, null)
        }
    }

    //query
    @RequiresApi(Build.VERSION_CODES.N)
    private fun queryBlockedNumbers() {
        // if (!BlockedNumberContract.canCurrentUserBlockNumbers(this))return

        val c: Cursor? = contentResolver.query(
            BlockedNumbers.CONTENT_URI, arrayOf(
                BlockedNumbers.COLUMN_ID,
                BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                BlockedNumbers.COLUMN_E164_NUMBER
            ), null, null, null
        )
        c?.close()
    }

    //unblock
    @RequiresApi(Build.VERSION_CODES.N)
    private fun unblockBlockedNumbers(number: String) {
        // if (!BlockedNumberContract.canCurrentUserBlockNumbers(this))return
        // if (!BlockedNumberContract.isBlocked(this,number))return

        ThreadUtils.executeByCpu(ThreadTask({
            BlockedNumberContract.unblock(this, "1234567890")
        }, {

        }))
    }


}