package ando.guard

import android.app.Application
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
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        LitePal.initialize(this)

    }

}