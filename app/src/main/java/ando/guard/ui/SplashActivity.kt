package ando.guard.ui

import ando.guard.R
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Title: $
 * <p>
 * Description:
 * </p>
 * @author javakam
 * @date 2020/12/28  15:51
 */
class SplashActivity : AppCompatActivity() {

    private val timer: CountDownTimer = object : CountDownTimer(2000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timer.start()

        val view = FrameLayout(this)
        val textView = TextView(this)
        textView.text = "(づ￣3￣)づ╭❤～"
        textView.textSize = 16F
        view.addView(textView)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        params.gravity = Gravity.CENTER
        view.layoutParams = params

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            view.setBackgroundColor(resources.getColor(R.color.color_container_bg, theme))
        }
        setContentView(view)
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }
}