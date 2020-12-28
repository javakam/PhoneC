package ando.guard.ui

import ando.guard.R
import ando.guard.utils.PermissionManager
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * Title: $
 * <p>
 * Description:
 * </p>
 * @author javakam
 * @date 2020/12/28  15:07
 */
class PickContactActivity :AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        PermissionManager.requestContactsPermission(this) {
            if (it) {
                // 打开通讯录
//                val intent = Intent()
//                intent.action = "android.intent.action.PICK"
//                intent.addCategory("android.intent.category.DEFAULT")
//                intent.type = "vnd.android.cursor.dir/phone_v2"
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                @Suppress("DEPRECATION")
                startActivityForResult(intent, 1)
            }
        }
    }


    private var username: String? = null
    private var usernumber: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val contactData: Uri? = data?.data
            Log.d("123", data.toString())
            //Uri contactData2 = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            val cursor: Cursor? = managedQuery(contactData, null, null, null, null)
            cursor?.moveToFirst()
            username =
                cursor?.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val contactId: String? =
                cursor?.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                null,
                null
            )?.use { c: Cursor? ->
                Log.e(
                    "123",
                    data.toString() + "username" + username + "contactId" + contactId + "phone" + c
                )
                while (c?.moveToNext() == true) {
                    usernumber =
                        c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    Log.w("123", "$username=====$usernumber")
                    Log.e(
                        "123",
                        data.toString() + "username" + username + "contactId" + contactId + "phone" + c + "usernumber" + usernumber
                    )
                }
                cursor?.close()
            }
        }
    }


}