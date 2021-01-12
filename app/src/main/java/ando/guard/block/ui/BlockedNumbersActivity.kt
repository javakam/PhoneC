package ando.guard.block.ui

import ando.dialog.core.DialogManager
import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.block.BlockedNumbersUtils
import ando.guard.block.db.BlockedNumber
import ando.guard.block.db.BlockedNumbersDaoManager
import ando.guard.common.showAlert
import ando.guard.common.supportImmersion
import ando.guard.utils.*
import ando.guard.block.BlockedNumbersUtils.REQUEST_CODE_SET_DEFAULT_DIALER
import ando.guard.block.BlockedNumbersUtils.isDefaultDialer
import ando.guard.block.work.proceedBlockedNumbersWork
import ando.guard.views.BaseRecyclerAdapter
import ando.guard.views.BaseViewHolder
import ando.guard.views.XRecyclerAdapter
import ando.guard.views.popup.EasyPopup
import ando.guard.views.popup.TriangleDrawable
import ando.guard.views.popup.XGravity
import ando.guard.views.popup.YGravity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class BlockedNumbersActivity : BaseMvcActivity() {

    private val mIvBack: ImageView by lazy { findViewById(R.id.iv_back) }
    private val mTvTitle: TextView by lazy { findViewById(R.id.tv_title) }
    private val mTvEdit: TextView by lazy { findViewById(R.id.tv_edit) }
    private val mIvAdd: ImageView by lazy { findViewById(R.id.iv_add) }
    private val mIvMore: ImageView by lazy { findViewById(R.id.iv_more) }
    private val mRecyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    //
    private var isEditMode: Boolean = false
    private val mAdapter: BlockedNumbersAdapter by lazy { BlockedNumbersAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_blocked_number

    override fun initView(savedInstanceState: Bundle?) {
        supportImmersion()
        mTvTitle.text = getString(R.string.blocked_numbers)
        mTvEdit.text = getString(R.string.edit)
        mTvEdit.gone()
        mIvMore.gone()

        BlockedNumbersDaoManager.useBlockedNumbers()

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

            showLoadingDialog(getString(R.string.str_dialog_loading))
            proceedBlockedNumbersWork(this) {
                reloadData()
                DialogManager.dismiss()
            }
        }
    }

    private fun showLoadingDialog(@Suppress("SameParameterValue") text: String) {
        val width = resources.getDimensionPixelSize(R.dimen.dimen_dialog_loading_width)
        val height = resources.getDimensionPixelSize(R.dimen.dimen_dialog_loading_height)
        DialogManager.with(this, R.style.AndoLoadingDialog)
            .setContentView(R.layout.layout_ando_dialog_loading) { v ->
                v.findViewById<View>(R.id.progressbar_ando_dialog_loading).visibility = View.VISIBLE
                v.findViewById<TextView>(R.id.tv_ando_dialog_loading_text).text = text
            }
            .setSize(width, height)
            .setCancelable(true)
            .setCanceledOnTouchOutside(false)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (isDefaultDialer()) reloadData() else showSetDefaultDialer()
        }
    }

    override fun initListener() {
        mIvBack.setOnClickListener { finish() }
        mTvTitle.setOnLongClickListener {
            BlockedNumbersDaoManager.deleteBlockedNumbers()
            true
        }
        mTvEdit.setOnClickListener {
            isEditMode = !isEditMode

        }
        mIvAdd.setOnClickListener {
            showEditBlockedNumberDialog()
        }
        mIvMore.setOnClickListener {
            //showResetDefaultDialer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogManager.dismiss()
        BlockedNumbersDaoManager.useDefault()
    }

    private fun reloadData() {
        ThreadUtils.executeByCached(ThreadTask({
            BlockedNumbersUtils.getBlockedNumbers()
        }, {
            if (isFinishing || isDestroyed) return@ThreadTask

            mAdapter.refresh(it)
            if (!it.isNullOrEmpty()) {
                //统计
                //DataSourceManager.cacheBlockedNumbers2DB(it){}
                //val isSaved: Boolean = DataSourceManager.cacheBlockedNumbers2Json(it)
                //Log.e("123", "isSaved $isSaved reloadData = ${BlockedNumberDao.queryTotalCount()}")

                mTvTitle.text = String.format(
                    Locale.getDefault(),
                    getString(R.string.blocked_numbers_count),
                    it.size
                )

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
            getString(R.string.set_default_dialer_tip),
            getString(R.string.must_make_default_dialer),
            getString(R.string.set_as_default),
            getString(R.string.cancel),
            false
        ) {
            if (it) {
                BlockedNumbersUtils.launchSetDefaultDialerIntent(this)
            } else finish()
        }
    }

    private val mMorePopup: EasyPopup by lazy {
        EasyPopup.create()
            .setContext(this)
            .setContentView(R.layout.layout_blocked_number_pop)
            .setAnimationStyle(R.style.RightTop2PopAnim)
            .setOnViewListener { view, popup ->
                val arrowView = view.findViewById<View>(R.id.v_arrow)
                @Suppress("DEPRECATION")
                arrowView.background = TriangleDrawable(
                    TriangleDrawable.TOP,
                    resources.getColor(R.color.color_container_bg)
                )
                val resetDefaultDialerView = view.findViewById<TextView>(R.id.tv_reset)
                resetDefaultDialerView.setOnClickListener {
                    popup?.dismiss()
                    BlockedNumbersUtils.launchSetDefaultDialerIntent(this)
                }
            }
            .setFocusAndOutsideEnable(true)
//          .setBackgroundDimEnable(true)
//          .setDimValue(0.5f)
//          .setDimColor(Color.RED)
//          .setDimView(mTitleBar)
            .apply()
    }

    private fun showResetDefaultDialer() {
        val offsetX = dp2px(this, 20) - mIvMore.width / 2
        val offsetY = (mTvTitle.height - mIvMore.height) / 2
        mMorePopup.showAtAnchorView(
            mIvMore,
            YGravity.BELOW,
            XGravity.ALIGN_RIGHT,
            offsetX,
            offsetY
        )
    }

    private fun showEditBlockedNumberDialog(originalNumber: BlockedNumber? = null) {
        BlockedNumberDialog(this, originalNumber) {
            Log.e("123", "showEditBlockedNumberDialog: $it")
            if (originalNumber != null && it?.equals("delete", true) == true) {
                BlockedNumbersUtils.deleteBlockedNumber(originalNumber.number)
            }
            reloadData()
        }
    }

    inner class BlockedNumbersAdapter : BaseRecyclerAdapter<BlockedNumber>() {
        override fun getLayoutId(viewType: Int): Int = R.layout.item_blocked_number
        override fun bindData(holder: BaseViewHolder, position: Int, item: BlockedNumber) {
            holder.setText(R.id.tv_blocked_number, item.number)
            //holder.setVisible(R.id.checkbox_blocked_number, isEditMode)
            //holder.setChecked(R.id.checkbox_blocked_number, isEditMode)
        }
    }

}