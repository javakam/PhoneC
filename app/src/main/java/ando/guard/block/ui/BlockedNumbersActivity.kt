package ando.guard.block.ui

import ando.dialog.core.DialogManager
import ando.file.core.FileOpener
import ando.file.core.FileUri
import ando.file.selector.FileSelectCallBack
import ando.file.selector.FileSelectResult
import ando.file.selector.FileSelector
import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.block.BlockedNumbersDataManager
import ando.guard.block.BlockedNumbersUtils
import ando.guard.block.BlockedNumbersUtils.REQUEST_CODE_SET_DEFAULT_DIALER
import ando.guard.block.BlockedNumbersUtils.isDefaultDialer
import ando.guard.block.db.BlockedNumber
import ando.guard.block.proceedBlockedNumbersWork
import ando.guard.common.showAlert
import ando.guard.common.showLoadingDialog
import ando.guard.common.supportImmersion
import ando.guard.common.toastLong
import ando.guard.utils.*
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

    private var mFileSelector: FileSelector? = null

    override fun getLayoutId(): Int = R.layout.activity_blocked_number

    override fun initView(savedInstanceState: Bundle?) {
        supportImmersion()
        mTvTitle.text = getString(R.string.blocked_numbers)
        mTvEdit.text = getString(R.string.edit)
        mTvEdit.gone()

        initRecyclerView()

        BlockedNumbersDataManager.useBlockedNumbersDB()

        if (isDefaultDialer()) {
            fetchData()
        } else {
            showSetDefaultDialer()
        }
    }

    private fun initRecyclerView() {
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, resultData)
        mFileSelector?.obtainResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (isDefaultDialer()) fetchData() else showSetDefaultDialer()
        }
    }

    override fun initListener() {
        mIvBack.setOnClickListener { finish() }
        mTvTitle.setOnLongClickListener {
            BlockedNumbersDataManager.deleteBlockedNumbersDB()
            true
        }
        mTvEdit.setOnClickListener {
            isEditMode = !isEditMode

        }
        mIvAdd.setOnClickListener {
            showEditBlockedNumberDialog()
        }
        mIvMore.setOnClickListener {
            showMoreMenu()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogManager.dismiss()
        BlockedNumbersDataManager.useDefaultDB()
    }

    private fun fetchData() {
        showLoadingDialog(this)
        proceedBlockedNumbersWork(this) {
            //延迟500毫秒, 显示效果好一点(#^.^#)
            doAsyncDelay({
                reloadData()
                DialogManager.dismiss()
            }, 200L)
        }
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
            if (it) BlockedNumbersUtils.launchSetDefaultDialerIntent(this)
            else finish()
        }
    }

    private val mMorePopup: EasyPopup by lazy {
        EasyPopup.create()
            .setContext(this)
            .setContentView(R.layout.layout_blocked_number_pop)
            .setAnimationStyle(R.style.RightTop2PopAnim)
            .setOnViewListener { v, popup ->
                val arrowView = v.findViewById<View>(R.id.v_arrow)
                arrowView.background = TriangleDrawable(
                    TriangleDrawable.TOP,
                    resources.getColor(R.color.white)
                )
                //重置
                /*v.findViewById<TextView>(R.id.tv_reset).setOnClickListener {
                    popup?.dismiss()
                    BlockedNumbersUtils.launchSetDefaultDialerIntent(this)
                }*/
                //导出
                v.findViewById<TextView>(R.id.tv_export).setOnClickListener {
                    popup?.dismiss()
                    BlockedNumbersDataManager.export {
                        //打开文件(json)
                        //FileOpener.openFileBySystemChooser(this, FileUri.getUriByFile(it), "application/json")

                        //分享文件(json)
                        FileUri.getUriByFile(it)?.apply {
                            FileOpener.openShare(this@BlockedNumbersActivity, this)
                        }
                    }
                }
                //导入
                v.findViewById<TextView>(R.id.tv_import).setOnClickListener {
                    popup?.dismiss()

                    mFileSelector = FileSelector.with(this)
                        .setMimeTypes("application/json")
                        .callback(object : FileSelectCallBack {
                            override fun onError(e: Throwable?) {
                                Log.e("123", e?.toString())
                                toastLong("导入失败!")
                            }

                            override fun onSuccess(results: List<FileSelectResult>?) {
                                results?.firstOrNull()?.apply {
                                    uri?.apply {
                                        BlockedNumbersDataManager.import(this) {
                                            reloadData()
                                            toastLong("导入成功!")
                                        }
                                    }
                                }
                            }
                        })
                        .choose()
                }
            }
            .setFocusAndOutsideEnable(true)
            .setBackgroundDimEnable(true)
            .setDimValue(0.3f)
//          .setDimColor(Color.RED)
//          .setDimView(mTitleBar)
            .apply()
    }

    private fun showMoreMenu() {
        val offsetX = resources.getDimensionPixelSize(R.dimen.dp_20) - mIvMore.width / 2
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