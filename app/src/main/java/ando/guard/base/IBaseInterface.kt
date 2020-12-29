package ando.guard.base

import android.os.Bundle
import android.view.View
import android.widget.Space

interface IBaseInterface {
    fun initView(savedInstanceState: Bundle?)
    fun initListener() {}
    fun initData() {}

    fun getLayoutView(): View = View(null)
    fun getLayoutId(): Int = -1
}