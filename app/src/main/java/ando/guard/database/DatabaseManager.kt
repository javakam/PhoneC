package ando.guard.database

import ando.guard.common.DB_BLOCKED_NUMBERS
import org.litepal.LitePal
import org.litepal.LitePal.use
import org.litepal.LitePalDB


/**
 * Title: DatabaseManager
 *
 * Description:
 *
 * @author javakam
 * @date 2020/12/30  14:01
 */
object DatabaseManager {

    fun useDefault() = LitePal.useDefault()

    fun useBlockedNumbers(): LitePalDB {
        val litePalDB = LitePalDB(DB_BLOCKED_NUMBERS, 1)
        litePalDB.addClassName(BlackNumberModel::class.java.name)
        litePalDB.isExternalStorage = true
        use(litePalDB)
        return litePalDB
    }

    fun deleteBlockedNumbers() {
        LitePal.deleteDatabase(DB_BLOCKED_NUMBERS)
    }

}