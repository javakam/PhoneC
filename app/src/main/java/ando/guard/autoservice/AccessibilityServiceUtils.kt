package ando.guard.autoservice

import ando.guard.App
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo

/**
 * AccessibilityUtils
 *
 * @author javakam
 */
object AccessibilityUtils {

    private val mContext: Context by lazy { App.INSTANCE }
    private val mAccessibilityManager: AccessibilityManager by lazy {
        mContext.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    /////////////////////// Setting Page ///////////////////////

    /**
     * 跳转到设置页面无障碍服务开启自定义辅助功能服务
     */
    fun jumpToSettingPage(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 判断自定义辅助功能服务是否开启
     */
    fun isSettingOn(context: Context, className: String): Boolean =
        (context.getSystemService(AccessibilityService.ACTIVITY_SERVICE) as? ActivityManager?)?.run {
            @Suppress("DEPRECATION")
            val runningServices = getRunningServices(100)
            if (runningServices.size < 0) {
                return false
            }
            for (i in runningServices.indices) {
                val service = runningServices[i].service
                if (service.className == className) {
                    return true
                }
            }
            false
        } ?: false

    /////////////////////////////////////////////////////////////

    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    private fun checkAccessibilityEnabled(serviceName: String): Boolean {
        val accessibilityServices =
            mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in accessibilityServices) {
            if (info.id == serviceName) {
                return true
            }
        }
        return false
    }

    /**
     * 前往开启辅助服务界面
     */
    fun goAccess() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        mContext.startActivity(intent)
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    fun performViewClick(nodeInfo: AccessibilityNodeInfo?) {
        nodeInfo?.apply {
            var info: AccessibilityNodeInfo? = this
            while (info != null) {
                if (info.isClickable) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    break
                }
                info = info.parent
            }
        }
    }

    /**
     * 模拟返回操作
     */
    fun performBackClick(service: AccessibilityService) {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    /**
     * 模拟下滑操作
     */
    fun performScrollBackward(service: AccessibilityService) {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        service.performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    /**
     * 模拟上滑操作
     */
    fun performScrollForward(service: AccessibilityService) {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        service.performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    /**
     * 获取某个节点的某个子节点
     */
    fun getChildNodeInfo(
        service: AccessibilityService,
        id: String,
        childIndex: Int
    ): AccessibilityNodeInfo? {
        val listChatRecord = findNodesById(service, id)
        if (listChatRecord == null || listChatRecord.isEmpty()) {
            return null
        }
        val parentNode = listChatRecord[0] //该节点
        val count = parentNode.childCount
        Log.i("123", "子节点个数 $count")
        return if (childIndex < count) parentNode.getChild(childIndex) else null
    }

    /**
     * 获取跟节点
     */
    fun getRootNodeInfo(service: AccessibilityService, event: AccessibilityEvent? = null):
            AccessibilityNodeInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            service.rootInActiveWindow
        } else {
            event?.source
        }

    /**
     * 根据text搜索所有符合的节点
     */
    fun findNodesByText(
        service: AccessibilityService,
        text: String?
    ): List<AccessibilityNodeInfo>? {
        val nodeInfo: AccessibilityNodeInfo? = getRootNodeInfo(service)
        if (nodeInfo != null) {
            Log.i("123", "getClassName：" + nodeInfo.className)
            Log.i("123", "getText：" + nodeInfo.text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // 需要在xml文件中声明权限android:accessibilityFlags="flagReportViewIds"
                // 并且版本大于4.3 才能获取到 view 的 ID
                Log.i("123", "getClassName：" + nodeInfo.viewIdResourceName)
            }
            return nodeInfo.findAccessibilityNodeInfosByText(text)
        }
        return null
    }

    fun findNodesById(
        service: AccessibilityService,
        viewId: String?
    ): List<AccessibilityNodeInfo>? =
        getRootNodeInfo(service)?.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return findAccessibilityNodeInfosByViewId(viewId)
            }
            return null
        }

    /**
     * 查找对应文本的View
     *
     * @param text text
     * @return View
     */
    fun findViewByText(
        service: AccessibilityService,
        text: String?,
        clickable: Boolean = false
    ): AccessibilityNodeInfo? {
        val nodeInfo = service.rootInActiveWindow ?: return null
        val list: List<AccessibilityNodeInfo?>? = nodeInfo.findAccessibilityNodeInfosByText(text)

        if (list != null && list.isNotEmpty()) {
            list.forEach {
                it?.apply {
                    if (isClickable == clickable) {
                        return this
                    }
                }
            }
        }
        return null
    }

    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun findViewById(service: AccessibilityService, id: String?): AccessibilityNodeInfo? {
        val accessibilityNodeInfo = service.rootInActiveWindow ?: return null
        val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id)
        if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo
                }
            }
        }
        return null
    }

    fun clickTextViewByText(service: AccessibilityService, text: String?) {
        val accessibilityNodeInfo = service.rootInActiveWindow ?: return
        val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text)
        if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo)
                    break
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun clickTextViewByID(service: AccessibilityService, id: String?) {
        val accessibilityNodeInfo = service.rootInActiveWindow ?: return
        val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id)
        if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo)
                    break
                }
            }
        }
    }

    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    fun inputText(nodeInfo: AccessibilityNodeInfo, text: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val clipboard = mContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", text)
            clipboard.primaryClip = clip
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }
    }

}