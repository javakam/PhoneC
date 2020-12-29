package ando.guard.ui.blocked

import ando.guard.R
import ando.guard.utils.BlockedNumber
import ando.guard.views.BaseRecyclerAdapter
import ando.guard.views.BaseViewHolder

/**
 * Title: BlockedNumbersAdapter
 * <p>
 * Description:
 * </p>
 * @author javakam
 * @date 2020/12/29  15:38
 */
class BlockedNumbersAdapter : BaseRecyclerAdapter<BlockedNumber>() {

    override fun getLayoutId(viewType: Int): Int = R.layout.item_manage_blocked_number

    override fun bindData(holder: BaseViewHolder, position: Int, item: BlockedNumber) {
        holder.setText(R.id.tv_manage_blocked_number_title, item.number)
    }

}