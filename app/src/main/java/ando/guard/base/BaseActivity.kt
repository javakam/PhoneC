package ando.guard.base

import ando.guard.App.Companion.exit
import ando.guard.common.toastShort
import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Title:BaseActivity
 *
 * Description:
 *
 * @author javakam
 * @date 2019/3/17 13:17
 */
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var mView: View  //系统DecorView的根View
    protected var isExit = false        //是否退出App

    override fun onCreate(savedInstanceState: Bundle?) {
        initActivityConfig()
        super.onCreate(savedInstanceState)
    }

    protected open fun initActivityConfig() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * 重写 getResource 方法，防止app字体大小受系统字体大小影响
     *
     * https://www.jianshu.com/p/5effff3db399
     */
    override fun getResources(): Resources {
        val resources = super.getResources()
        if (resources != null && resources.configuration.fontScale != 1.0f) {
            val configuration = resources.configuration
            configuration.fontScale = 1.0f
            resources.updateConfiguration(configuration, resources.displayMetrics)
            // createConfigurationContext(configuration)
        }
        return resources
    }

    protected open fun setTskRoot(){
        if (!this.isTaskRoot) {
            intent?.apply {
                if (hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == action) {
                    finish()
                    return
                }
            }
        }
    }
}

abstract class BaseMvcActivity : BaseActivity(), IBaseInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = getLayoutId()
        if (layoutId > 0) {
            setContentView(layoutId)
        } else {
            setContentView(getLayoutView())
        }
        mView = findViewById(R.id.content)
        initView(savedInstanceState)
        initListener()
        initData()
    }

    //连续点击两次退出App
    @SuppressLint("CheckResult")
    protected fun exitBy2Click(delay: Long, @StringRes text: Int) {
        if (!isExit) {
            isExit = true
            toastShort(text)
            // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
            Flowable
                .timer(delay, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { isExit = false }
        } else {
            exit()
        }
    }
}
