package ando.guard

import org.litepal.LitePal
import java.util.*

object BlackNumberDao {

    private fun saveOrUpdate(number: String?, mode: String?, isSave: Boolean): Boolean {
        if (number.isNullOrBlank() || mode.isNullOrBlank()) return false

        val model: BlackNumberModel? =
            LitePal.where("number = ?", number)?.findFirst(BlackNumberModel::class.java)
        if (isSave) if (model != null && model.isSaved) return true else model?.delete()

        val newModel = BlackNumberModel()
        newModel.number = number
        newModel.mode = mode
        newModel.updateData = Date()
        return newModel.save()
    }

    /**
     * 增加黑名单到数据库
     *
     * @param number 黑名单号码
     * @param mode   拦截模式
     */
    fun save(number: String?, mode: String?): Boolean = saveOrUpdate(number, mode, true)

    /**
     * 修改黑马单
     *
     * @param number 黑名单号码
     * @param mode   拦截模式
     */
    fun update(number: String?, mode: String?): Boolean = saveOrUpdate(number, mode, false)

    /**
     * 查询该黑名单号码是否存在
     *
     * @param number 黑马单号码
     * @return true为存在，false为不存在
     */
    fun queryNumber(number: String?): Boolean = number?.run {
        LitePal.where(" number = ? ", number)?.findFirst(BlackNumberModel::class.java)?.isSaved
    } ?: false

    /**
     * 查询拦截模式
     *
     * @param number 黑马单号码
     * @return 返回对应的拦截模式，Null为没有
     */
    fun queryMode(number: String?): String = number?.apply {
        LitePal.where(" number = ? ", number)?.findFirst(BlackNumberModel::class.java)?.mode
    } ?: ""

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

    fun queryAllAsync(callback: ((List<BlackNumberModel>?) -> Unit)?) {
        ThreadUtils.executeByCpu(ThreadTask({
            queryAll()
        }, {
            callback?.invoke(it)
        }))
    }

    fun deleteAll(): Boolean {
        return LitePal.deleteAll(BlackNumberModel::class.java) > 0
    }
}