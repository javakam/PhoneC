package ando.guard.ui

import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.common.AppRouter
import ando.guard.utils.PermissionManager
import android.os.Bundle
import android.widget.Button

class MainActivity : BaseMvcActivity() {

    private lateinit var mBtBlockedNumbers: Button

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initView(savedInstanceState: Bundle?) {
        PermissionManager.requestContactsPermission(this)

        mBtBlockedNumbers = findViewById(R.id.bt_blocked_write)
        mBtBlockedNumbers.setOnClickListener {
            PermissionManager.requestContactsPermission(this) {
                if (it) AppRouter.toBlockedNumbersActivity(this)
            }
        }
    }

}