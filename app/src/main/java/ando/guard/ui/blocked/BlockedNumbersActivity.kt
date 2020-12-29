package ando.guard.ui.blocked

import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.showAlert
import ando.guard.utils.*
import ando.guard.utils.BlockedContactsManager.isDefaultDialer
import ando.guard.views.BaseViewHolder
import ando.guard.views.XRecyclerAdapter
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BlockedNumbersActivity : BaseMvcActivity() {

    private val mTvTitle: TextView by lazy { findViewById(R.id.tv_title) }
    private val mTvEdit: TextView by lazy { findViewById(R.id.tv_edit) }
    private val mIvAdd: ImageView by lazy { findViewById(R.id.iv_add) }
    private val mIvMore: ImageView by lazy { findViewById(R.id.iv_more) }
    private val mRecyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    //
    private val isEditMode: Boolean = false
    private val mAdapter: BlockedNumbersAdapter by lazy { BlockedNumbersAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_blocked_number

    override fun initView(savedInstanceState: Bundle?) {
        mTvTitle.text = getString(R.string.blocked_numbers)
        mTvEdit.text = getString(R.string.edit)

        if (!isDefaultDialer()) {
            showSetDefaultDialer()
        } else {
            mRecyclerView.visible()
            mRecyclerView.setHasFixedSize(true)
            mRecyclerView.itemAnimator = null
            mRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            mRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.set(0, 1, 0, 1)
                }
            })
            mRecyclerView.adapter = mAdapter
            reloadData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (isDefaultDialer()) reloadData() else showSetDefaultDialer()
        }
    }

    override fun initListener() {
        mTvEdit.setOnClickListener {

        }
        mIvAdd.setOnClickListener {
            showEditBlockedNumberDialog()
        }
        mIvMore.setOnClickListener {

        }
    }

    private fun reloadData() {
        ThreadUtils.executeByCpu(ThreadTask({
            BlockedContactsManager.getBlockedNumbers().asReversed()
        }, {
            mAdapter.refresh(it)
            if (!it.isNullOrEmpty()) {
                mAdapter.setOnItemClickListener(object : XRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(viewHolder: BaseViewHolder?, position: Int) {
                        showEditBlockedNumberDialog(it[position])
                    }
                })
            }
        }))
    }

    private fun showSetDefaultDialer() {
        showAlert(
            this,
            "设为默认拨号程序",
            getString(R.string.must_make_default_dialer),
            getString(R.string.set_as_default),
            "取消",
            false
        ) {
            if (it) {
                BlockedContactsManager.launchSetDefaultDialerIntent(this)
            } else finish()
        }
    }

    private fun showEditBlockedNumberDialog(originalNumber: BlockedNumber? = null) {
        BlockedNumberDialog(this, originalNumber) {
            Log.e("123", "showEditBlockedNumberDialog: $it")
            reloadData()
        }
    }

}