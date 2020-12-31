package ando.guard.database

import ando.guard.App
import ando.guard.R
import ando.guard.common.FILE_BLOCKED_NUMBERS
import ando.guard.utils.*
import android.util.Log
import com.google.gson.reflect.TypeToken
import org.litepal.LitePal
import org.litepal.LitePal.use
import org.litepal.LitePalDB
import java.io.File

/**
 * Title: DatabaseManager
 *
 * Description:
 *
 * @author javakam
 * @date 2020/12/30  14:01
 */
object DataSourceManager {

    private val globalBlockedNumbersFileParentPath: String by lazy {
        val context = App.INSTANCE
        val dbExternalPath = context.getExternalFilesDir(null)
        "$dbExternalPath/${context.getString(R.string.blocked_numbers)}"
    }

    private val globalBlockedNumbersFile: File by lazy {
        File(globalBlockedNumbersFileParentPath, "$FILE_BLOCKED_NUMBERS.json")
    }

    fun useDefault() = LitePal.useDefault()

    fun useBlockedNumbers(): LitePalDB {
        val litePalDB = LitePalDB(FILE_BLOCKED_NUMBERS, 1)
        litePalDB.addClassName(BlockedNumberModel::class.java.name)
        litePalDB.isExternalStorage = true
        use(litePalDB)
        return litePalDB
    }

    fun deleteBlockedNumbers() {
        LitePal.deleteDatabase(FILE_BLOCKED_NUMBERS)
    }

    /**
     * 使用`assets`下的`db`文件
     *
     * 写入数据到`db`中 : BlockedNumberDao.saveAll(it) { }
     */
    fun loadBlockedNumbersFromDB(): File {
        val dbFileName = "$FILE_BLOCKED_NUMBERS.db"
        val dbFile = File(globalBlockedNumbersFileParentPath, dbFileName)
        Log.w("123", "dbFile = ${dbFile.exists()}")

        if (!dbFile.exists()) {
            readAssetsDataFile(
                assetsFileName = dbFileName,
                targetFileParentPath = globalBlockedNumbersFileParentPath,
                targetFileName = dbFileName
            )
        }
        return dbFile
    }

    fun cacheBlockedNumbers2DB(numbers: List<BlockedNumber>, callback: ((Boolean) -> Unit)?) {
        BlockedNumberDao.saveAll(numbers, callback)
    }

    fun isBlockedNumbersFileJsonExist() = globalBlockedNumbersFile.exists()

    fun loadBlockedNumbersFromJson(): List<BlockedNumber> {
        val assetsData: String = readAssetsDataString("$FILE_BLOCKED_NUMBERS.json")
        return if (assetsData.isNotBlank()) {
            GsonUtils.fromJson(
                assetsData,
                object : TypeToken<List<BlockedNumber>>() {}.type
            )
        } else emptyList()
    }

    fun cacheBlockedNumbers2Json(numbers: List<BlockedNumber>): Boolean {
        val json: String? =
            GsonUtils.toJson(numbers, object : TypeToken<List<BlockedNumber>>() {}.type)

        return if (!json.isNullOrBlank()) {
            write2File(
                json.byteInputStream(Charsets.UTF_8),
                globalBlockedNumbersFileParentPath, "$FILE_BLOCKED_NUMBERS.json"
            )
            true
        } else false
    }

    fun removeBlockedNumbersFileJson() {
        deleteFilesButDir(globalBlockedNumbersFile)
    }
}