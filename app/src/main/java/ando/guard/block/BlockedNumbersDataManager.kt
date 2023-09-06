package ando.guard.block

import ando.file.core.FileUtils
import ando.file.core.FileUtils.deleteFilesNotDir
import ando.guard.App
import ando.guard.BuildConfig
import ando.guard.R
import ando.guard.block.db.BlockedNumber
import ando.guard.block.db.BlockedNumberDao
import ando.guard.block.db.BlockedNumberModel
import ando.guard.common.FILE_BLOCKED_NUMBERS
import ando.guard.utils.*
import android.net.Uri
import android.util.Log
import com.google.gson.reflect.TypeToken
import org.litepal.LitePal
import org.litepal.LitePal.use
import org.litepal.LitePalDB
import java.io.*

object BlockedNumbersDataManager {

    fun markBlockedNumbersWritten() {
        SPUtils.get().put("blocked_code", BuildConfig.VERSION_CODE)
        SPUtils.get().put("blocked_written", true)
    }

    fun isBlockedNumbersWritten(): Boolean {
        val isAppUpdated = (SPUtils.get().getInt("blocked_code", -1) != BuildConfig.VERSION_CODE)
        val isWritten = SPUtils.get().getBoolean("blocked_written", false)
        return !isAppUpdated && isWritten
    }

    /////////////////////////

    // `/mnt/sdcard/Android/data/ando.guard/files/黑名单/blockednumbers.json`
    private val blackParentPath: String by lazy {
        val context = App.INSTANCE
        val externalPath = context.getExternalFilesDir(null)
        "$externalPath/${context.getString(R.string.blocked_numbers)}"
    }
    private val blackFile: File by lazy {
        File(blackParentPath, "$FILE_BLOCKED_NUMBERS.json")
    }

    fun useDefaultDB() = LitePal.useDefault()

    fun useBlockedNumbersDB(): LitePalDB {
        val litePalDB = LitePalDB(FILE_BLOCKED_NUMBERS, 1)
        litePalDB.addClassName(BlockedNumberModel::class.java.name)
        litePalDB.isExternalStorage = true
        use(litePalDB)
        return litePalDB
    }

    fun deleteBlockedNumbersDB() {
        LitePal.deleteDatabase(FILE_BLOCKED_NUMBERS)
    }

    /**
     * 使用`assets`下的`db`文件
     *
     * 写入数据到`db`中 : BlockedNumberDao.saveAll(it) { }
     */
    fun loadFromDB(): File {
        val dbFileName = "$FILE_BLOCKED_NUMBERS.db"
        val dbFile = File(blackParentPath, dbFileName)
        Log.w("123", "dbFile = ${dbFile.exists()}")

        if (!dbFile.exists()) {
            readAssetsDataFile(
                assetsFileName = dbFileName,
                targetFileParentPath = blackParentPath,
                targetFileName = dbFileName
            )
        }
        return dbFile
    }

    fun cacheToDatabase(numbers: List<BlockedNumber>, callback: ((Boolean) -> Unit)?) {
        BlockedNumberDao.saveAll(numbers, callback)
    }

    fun isFileJsonExist() = blackFile.exists()

    fun loadFromJson(): List<BlockedNumber> {
        val assetsData: String = readAssetsDataString("$FILE_BLOCKED_NUMBERS.json")
        return if (assetsData.isNotBlank()) {
            GsonUtils.fromJson(
                assetsData,
                object : TypeToken<List<BlockedNumber>>() {}.type
            )
        } else emptyList()
    }

    fun cacheToJson(
        numbers: List<BlockedNumber>,
        parentPath: String = blackParentPath,
        fileName: String = "$FILE_BLOCKED_NUMBERS.json"
    ): Boolean {
        val json: String? =
            GsonUtils.toJson(numbers, object : TypeToken<List<BlockedNumber>>() {}.type)

        return if (!json.isNullOrBlank()) {
            FileUtils.write2File(
                json.byteInputStream(Charsets.UTF_8),
                parentPath, fileName
            )
            true
        } else false
    }

    fun removeBlockedNumbersFileJson() {
        deleteFilesNotDir(blackFile)
    }

    //Export
    // `/mnt/sdcard/Android/data/ando.guard/cache`
    private val blackExportCacheParentPath: String by lazy {
        val context = App.INSTANCE
        val externalPath = context.externalCacheDir
        "$externalPath/$FILE_BLOCKED_NUMBERS/export"
    }
    private val blackExportCacheFile: File by lazy {
        File(blackExportCacheParentPath, "$FILE_BLOCKED_NUMBERS.json")
    }

    fun removeBlockedNumbersCacheJson() {
        deleteFilesNotDir(blackExportCacheFile)
    }

    fun export(block: (File) -> Unit) {
        ThreadUtils.executeByCached(ThreadTask({
            BlockedNumbersUtils.getBlockedNumbers()
        }, {
            deleteFilesNotDir(blackExportCacheFile)
            it?.apply {
                val result: Boolean =
                    cacheToJson(this, blackExportCacheParentPath, blackExportCacheFile.name)
                if (result && blackExportCacheFile.exists()) block.invoke(blackExportCacheFile)
            }
        }))
    }

    fun import(uri: Uri, block: (Boolean) -> Unit) {
        ThreadUtils.executeByCached(ThreadTask({
            FileUtils.readFileText(uri)
        }, { s ->
            if (s.isNullOrBlank()) {
                block.invoke(false)
                return@ThreadTask
            }
            val list: List<BlockedNumber>? =
                GsonUtils.fromJson(s, object : TypeToken<List<BlockedNumber>>() {}.type)

            if (list.isNullOrEmpty()) {
                block.invoke(false)
                return@ThreadTask
            }

            list.onEach { n: BlockedNumber ->
                BlockedNumbersUtils.addBlockedNumber(n.number)
            }
            block.invoke(true)

        }))
    }

}