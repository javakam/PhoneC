package ando.guard.database

import ando.guard.utils.ThreadTask
import ando.guard.utils.ThreadUtils
import ando.guard.utils.noNull
import org.litepal.LitePal
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.*

data class BlockedNumber(val bid: Long = 0L, val number: String, val normalizedNumber: String)

class BlackNumberModel : LitePalSupport() {
    //避免和 id 字段冲突
    @Column(unique = true, nullable = false)
    var number: String? = null
    var normalizedNumber: String? = null
    var updateData: Date? = null
}

fun BlockedNumber.toModel(): BlackNumberModel = BlackNumberModel().let {
    it.number = number
    it.normalizedNumber = normalizedNumber
    it.updateData = Date()
    return it
}

fun BlackNumberModel.toBlockNumber(): BlockedNumber =
    BlockedNumber(number = number.noNull(), normalizedNumber = normalizedNumber.noNull())

object BlackNumberDao {

    fun saveAll(blockedNumbers: List<BlockedNumber>?, callback: ((Boolean) -> Unit)?) {
        if (blockedNumbers.isNullOrEmpty()) return
        ThreadUtils.executeByCpu(ThreadTask({
            LitePal.saveAll(blockedNumbers.map { it.toModel() })
        }, {
            callback?.invoke(it ?: false)
        }))
    }

    fun saveOrUpdate(blockedNumber: BlockedNumber): Boolean =
        saveOrUpdate(blockedNumber.bid, blockedNumber.number, blockedNumber.normalizedNumber)

    fun saveOrUpdate(
        bid: Long,
        number: String?,
        normalizedNumber: String?
    ): Boolean {
        if (bid < 0 || number.isNullOrBlank() || normalizedNumber.isNullOrBlank()) return false
        val model: BlackNumberModel? =
            LitePal.where("bid = ? and number = ?", bid.toString(), number)
                ?.findFirst(BlackNumberModel::class.java)

        if (model != null && model.isSaved) {
            model.updateData = Date()
            return model.saveOrUpdate()
        }

        val newModel = BlackNumberModel()
        newModel.number = number
        newModel.normalizedNumber = normalizedNumber
        newModel.updateData = Date()
        return newModel.save()
    }

    /**
     * 查询该黑名单号码是否存在
     *
     * @param number 黑名单号码
     * @return true为存在，false为不存在
     */
    fun queryNumber(number: String?): Boolean = number?.run {
        LitePal.where(" number = ? ", number)?.findFirst(BlackNumberModel::class.java)?.isSaved
    } ?: false

    /**
     * 删除黑名单
     *
     * @param number 黑名单号码
     */
    fun delete(number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        return LitePal.where(" number = ? ", number)
            .findFirst(BlackNumberModel::class.java)
            ?.delete() ?: 0 > 0
    }

    /**
     * 查询黑名单总数
     *
     * @return 黑名单的总数
     */
    fun queryTotalCount(): Int = LitePal.count(BlackNumberModel::class.java)

    /**
     * 查询全部的黑名单信息
     *
     * @return 返回保存全部黑名单信息的 List
     */
    private fun queryAll(): List<BlackNumberModel>? =
        LitePal.where("number not null")?.order("updateData")
            ?.find(BlackNumberModel::class.java)

    private fun queryAllAsync(callback: ((List<BlockedNumber>?) -> Unit)?) {
        ThreadUtils.executeByCpu(ThreadTask({
            queryAll()
        }, { l: List<BlackNumberModel>? ->
            l?.apply {
                callback?.invoke(map { m: BlackNumberModel -> m.toBlockNumber() })
            }
        }))
    }

    fun deleteAll(): Boolean {
        return LitePal.deleteAll(BlackNumberModel::class.java) > 0
    }
}