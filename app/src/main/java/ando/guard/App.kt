package ando.guard

import ando.file.FileOperator
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Resources
import android.os.Process
import org.litepal.LitePal

/**
 * Title: Application
 * <p>
 * Description:
 * </p>
 * @author javakam
 * @date 2020/12/25  14:44
 */
class App : Application() {

    companion object {
        lateinit var INSTANCE: App

        fun exit() {
            Process.killProcess(Process.myPid())
            System.exit(0)
        }

        /**
         * 触发`Home`事件, 模拟用户退出到桌面, 并没有真正退出应用
         *
         */
        fun exitHome(activity: Activity) {
            val backHome = Intent(Intent.ACTION_MAIN)
            backHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            backHome.addCategory(Intent.CATEGORY_HOME)
            activity.startActivity(backHome)
        }

    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        LitePal.initialize(this)

        FileOperator.init(this, BuildConfig.DEBUG)
    }

    /**
     * 禁止app字体大小跟随系统字体大小调节
     *
     * 重写 getResource 方法，防止系统字体影响
     *
     * https://www.jianshu.com/p/5effff3db399
     */
    override fun getResources(): Resources? {
        val resources = super.getResources()
        if (resources != null && resources.configuration.fontScale != 1.0f) {
            val configuration = resources.configuration
            configuration.fontScale = 1.0f
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return resources
    }

}