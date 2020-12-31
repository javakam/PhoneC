package ando.guard.ui

import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.common.supportImmersion
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Title: SplashActivity
 * <p>
 * Description:
 * </p>
 * @author javakam
 * @date 2020/12/28  15:51
 */
class SplashActivity : BaseMvcActivity() {

    override fun initActivityConfig() {
        setTskRoot()
        super.initActivityConfig()
    }

    override fun getLayoutView(): View {
        supportImmersion()
        timer.start()
        val view = FrameLayout(this)
        val textView = TextView(this)
        textView.text = "(づ￣3￣)づ╭❤～"
        textView.setTextColor(Color.WHITE)
        textView.textSize = 20F
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        params.gravity = Gravity.CENTER
        view.layoutParams = params

        val paramsTextView = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        paramsTextView.gravity = Gravity.CENTER
        textView.layoutParams = paramsTextView
        view.addView(textView)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            view.setBackgroundColor(resources.getColor(R.color.color_main_theme, theme))
        }
        return view
    }

    private val timer: CountDownTimer = object : CountDownTimer(1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }
}