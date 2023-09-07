package ando.guard.ui

import ando.guard.R
import ando.guard.autoservice.AccessibilityUtils
import ando.guard.autoservice.BilibiliAccessibility
import ando.guard.base.BaseMvcActivity
import ando.guard.common.AppRouter
import ando.guard.common.showAlert
import ando.guard.common.supportImmersion
import ando.guard.common.toastShort
import ando.guard.utils.PermissionManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : BaseMvcActivity() {

    private lateinit var mBtContacts: Button
    private lateinit var mBtFlavorBilibiliOpen: Button
    private lateinit var mBtFlavorBilibiliClose: Button
    private lateinit var mBtFlavorBlockedNumbers: Button

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initView(savedInstanceState: Bundle?) {
        supportImmersion()
        //PermissionManager.requestStoragePermission(this)
        PermissionManager.requestContactsPermission(this) {
            if (it) {
                proceedAccessibilityService()
            } else {
                //finish()
                toastShort("没有联系人权限!")
            }
        }

        mBtContacts = findViewById(R.id.bt_contacts)
        mBtFlavorBlockedNumbers = findViewById(R.id.bt_blocked_numbers)
        mBtFlavorBilibiliOpen = findViewById(R.id.bt_bilibili_open)
        mBtFlavorBilibiliClose = findViewById(R.id.bt_bilibili_close)
    }

    override fun initListener() {
        mBtContacts.setOnClickListener {
            PermissionManager.requestContactsPermission(this) {
                if (it) AppRouter.toContactActivity(this)
            }
        }

        mBtFlavorBlockedNumbers.setOnClickListener {
            PermissionManager.requestContactsPermission(this) {
                if (it) AppRouter.toBlockedNumbersActivity(this)
            }
        }

        //注: 开启无障碍后会自动开启服务, 无需 startService
        mBtFlavorBilibiliOpen.setOnClickListener {
            proceedAccessibilityService {
                val i = Intent(this@MainActivity, BilibiliAccessibility::class.java)
                startService(i)
                toastShort("服务已开启")
            }
        }
        mBtFlavorBilibiliClose.setOnClickListener {
            proceedAccessibilityService {
//                val i = Intent(this@MainActivity, BilibiliAccessibility::class.java)
//                stopService(i)
                toastShort("服务已关闭")
            }
        }
    }

    private fun proceedAccessibilityService(block: () -> Unit = {}) {
        if (!AccessibilityUtils.isSettingOn(
                this,
                BilibiliAccessibility::class.java.name
            )
        ) {
            showAlert(
                this, "请在设置中打开启无障碍模式",
                "${getString(R.string.app_name)}需要在无障碍模式下才能正常工作",
                "去设置",
                "取消",
                true
            ) { ok ->
                if (ok) AccessibilityUtils.jumpToSettingPage(this)
            }
        } else block.invoke()
    }

}