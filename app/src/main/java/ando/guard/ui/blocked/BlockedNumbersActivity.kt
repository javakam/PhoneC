package ando.guard.ui.blocked

import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.common.showAlert
import ando.guard.common.supportImmersion
import ando.guard.database.BlockedNumber
import ando.guard.database.DataSourceManager
import ando.guard.database.DataSourceManager.loadBlockedNumbersFromJson
import ando.guard.utils.*
import ando.guard.utils.BlockedNumbersManager.REQUEST_CODE_SET_DEFAULT_DIALER
import ando.guard.utils.BlockedNumbersManager.isDefaultDialer
import ando.guard.views.BaseRecyclerAdapter
import ando.guard.views.BaseViewHolder
import ando.guard.views.popup.TriangleDrawable
import ando.guard.views.XRecyclerAdapter
import ando.guard.views.popup.EasyPopup
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

        DataSourceManager.useBlockedNumbers()

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

            Log.e(
                "123",
                "isBlockedNumbersFileJsonExist=${DataSourceManager.isBlockedNumbersFileJsonExist()}"
            )
            if (!DataSourceManager.isBlockedNumbersFileJsonExist()) {
                loadBlockedNumbersFromJson().apply {
                    if (isNotEmpty()) {
                        forEach { n: BlockedNumber ->
                            BlockedNumbersManager.addBlockedNumber(n.number)
                        }
                        reloadData()
                        DataSourceManager.removeBlockedNumbersFileJson()
                    }
                }
            } else reloadData()
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
        mIvBack.setOnClickListener { finish() }
        mTvTitle.setOnLongClickListener {
            DataSourceManager.deleteBlockedNumbers()
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
        DataSourceManager.useDefault()
    }

    private fun reloadData() {
        ThreadUtils.executeByCached(ThreadTask({
            /*
            todo 2020年12月31日17:08:41

I/Choreographer: Skipped 97 frames!  The application may be doing too much work on its main thread.

I/OpenGLRenderer: Davey! duration=1590ms; Flags=1, IntendedVsync=69462718327689,
Vsync=69464282843793, OldestInputEvent=9223372036854775807, NewestInputEvent=0,
HandleInputStart=69464283743968, AnimationStart=69464283786000,
PerformTraversalsStart=69464284336312, DrawStart=69464299854802, SyncQueued=69464301261729,
SyncStart=69464301530270, IssueDrawCommandsStart=69464301572510, SwapBuffers=69464308538291,
FrameCompleted=69464309077458, DequeueBufferDuration=74000, QueueBufferDuration=273000,

I/OpenGLRenderer: Davey! duration=1592ms; Flags=1, IntendedVsync=69462718327689,
Vsync=69464282843793, OldestInputEvent=9223372036854775807, NewestInputEvent=0,
HandleInputStart=69464283743968, AnimationStart=69464283786000,
PerformTraversalsStart=69464284336312, DrawStart=69464309547510, SyncQueued=69464309594958,
SyncStart=69464309818031, IssueDrawCommandsStart=69464309850583, SwapBuffers=69464310332718,
FrameCompleted=69464310699281, DequeueBufferDuration=67000, QueueBufferDuration=153000,
             */
            Log.w("123", "Thread111 =${Thread.currentThread()}")
            BlockedNumbersManager.getBlockedNumbers()
        }, {
            Log.w("123", "Thread222 =${Thread.currentThread()}")

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
                BlockedNumbersManager.launchSetDefaultDialerIntent(this)
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
                    BlockedNumbersManager.launchSetDefaultDialerIntent(this)
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
                BlockedNumbersManager.deleteBlockedNumber(originalNumber.number)
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