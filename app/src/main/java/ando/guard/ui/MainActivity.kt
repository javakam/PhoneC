package ando.guard.ui

import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.common.AppRouter
import ando.guard.common.supportImmersion
import ando.guard.utils.PermissionManager
import android.os.Bundle
import android.widget.Button

class MainActivity : BaseMvcActivity() {

    private lateinit var mBtContacts: Button
    private lateinit var mBtBlockedNumbers: Button

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initView(savedInstanceState: Bundle?) {
        supportImmersion()
        //PermissionManager.requestStoragePermission(this)
        PermissionManager.requestContactsPermission(this)

        mBtContacts = findViewById(R.id.bt_contacts)
        mBtBlockedNumbers = findViewById(R.id.bt_blocked_numbers)

        mBtContacts.setOnClickListener {
            PermissionManager.requestContactsPermission(this) {
                if (it) AppRouter.toContactManagerActivity(this)
            }
        }
        mBtBlockedNumbers.setOnClickListener {
            PermissionManager.requestContactsPermission(this) {
                if (it) AppRouter.toBlockedNumbersActivity(this)
            }
        }
    }

}