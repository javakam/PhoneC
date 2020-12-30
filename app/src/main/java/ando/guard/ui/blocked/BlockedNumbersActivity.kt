package ando.guard.ui.blocked

import ando.guard.R
import ando.guard.base.BaseMvcActivity
import ando.guard.common.DB_BLOCKED_NUMBERS
import ando.guard.common.showAlert
import ando.guard.common.supportImmersion
import ando.guard.database.BlackNumberDao
import ando.guard.database.BlockedNumber
import ando.guard.database.DatabaseManager
import ando.guard.utils.*
import ando.guard.utils.BlockedContactsManager.isDefaultDialer
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
import java.io.File
import java.util.*

class BlockedNumbersActivity : BaseMvcActivity() {

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

        DatabaseManager.useBlockedNumbers()
        val dbPath = "$DB_BLOCKED_NUMBERS.db"
        val dbFile = File("${externalCacheDir}/${getString(R.string.blocked_numbers)}", dbPath)
        Log.e("123", "dbFile = ${dbFile.exists()}")
        if (!dbFile.exists()) {
            readAssetsDataFile(
                assetsFileName = dbPath,
                targetFileParentPath = "${externalCacheDir}/${getString(R.string.blocked_numbers)}",
                targetFilePath = dbPath
            )
        }

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
        mTvTitle.setOnLongClickListener {
            DatabaseManager.deleteBlockedNumbers()
            true
        }
        mTvEdit.setOnClickListener {
            isEditMode = !isEditMode

        }
        mIvAdd.setOnClickListener {
            showEditBlockedNumberDialog()
        }
        mIvMore.setOnClickListener {
            TODO()
            //showResetDefaultDialer()
        }
    }

    override fun onDestroy() {
        DatabaseManager.useDefault()
        super.onDestroy()
    }

    private fun reloadData() {
        ThreadUtils.executeByCpu(ThreadTask({
            BlockedContactsManager.getBlockedNumbers().asReversed()
        }, {
            mAdapter.refresh(it)
            if (!it.isNullOrEmpty()) {
                //统计
                //BlackNumberDao.saveAll(it) {}
                Log.e("123", "reloadData =")
                mTvTitle.text = String.format(
                    Locale.getDefault(),
                    getString(R.string.blocked_numbers_count),
                    BlackNumberDao.queryTotalCount()
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
                BlockedContactsManager.launchSetDefaultDialerIntent(this)
            } else finish()
        }
    }


    private val mMorePopup: EasyPopup by lazy {
        EasyPopup.create()
            .setContext(this)
            .setContentView(R.layout.layout_pop_right)
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
                    BlockedContactsManager.launchSetDefaultDialerIntent(this)
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
            reloadData()
        }
    }

    inner class BlockedNumbersAdapter : BaseRecyclerAdapter<BlockedNumber>() {
        override fun getLayoutId(viewType: Int): Int = R.layout.item_manage_blocked_number
        override fun bindData(holder: BaseViewHolder, position: Int, item: BlockedNumber) {
            holder.setText(R.id.tv_manage_blocked_number_title, item.number)
        }
    }

}