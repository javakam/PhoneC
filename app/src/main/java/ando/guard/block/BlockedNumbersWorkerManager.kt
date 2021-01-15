package ando.guard.block

import ando.guard.BuildConfig
import ando.guard.base.BaseActivity
import ando.guard.block.db.BlockedNumber
import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.coroutineScope

private val WORK_KEY_BLOCK: String by lazy { "block" }

fun proceedBlockedNumbersWork(activity: BaseActivity, callBack: () -> Unit) {
    val request = OneTimeWorkRequestBuilder<BlockedNumbersDatabaseWorker>()
        //.addTag(WORK_KEY_BLOCK)
        .build()

    WorkManager.getInstance(activity)
        .getWorkInfoByIdLiveData(request.id)
        .observe(activity, {
            if (it.state == WorkInfo.State.SUCCEEDED) {
                //val resultValue: String = it.outputData.getString(WORK_KEY_BLOCK) ?: ""
                callBack.invoke()
            } else {
                Log.e("123", "Result: ${it.outputData.getString(WORK_KEY_BLOCK)}")
            }
        })

    WorkManager.getInstance(activity).enqueue(request)
}

/**
 * sunflower
 *
 * https://blog.csdn.net/yingaizhu/article/details/105392459
 */
internal class BlockedNumbersDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            if (BuildConfig.DEBUG) {
                Log.e(
                    "123",
                    "isBlockedNumbersWritten=${BlockedNumbersDataManager.isBlockedNumbersWritten()}"
                )
            }

            if (!BlockedNumbersDataManager.isBlockedNumbersWritten()) {
                BlockedNumbersDataManager.loadFromJson().run {
                    if (isNotEmpty()) {
                        forEach { n: BlockedNumber ->
                            BlockedNumbersUtils.addBlockedNumber(n.number)
                        }
                        //BlockedNumbersDataManager.cacheToJson(this)
                        //BlockedNumbersDaoManager.removeBlockedNumbersFileJson()
                        BlockedNumbersDataManager.markBlockedNumbersWritten()
                        buildResult("ok", true)
                    } else buildResult("load from json file failed", false)
                }
            } else buildResult("exist", true)
        } catch (ex: Exception) {
            buildResult(ex.toString(), false)
        }
    }

    private fun buildResult(value: String, isSuccess: Boolean): Result =
        if (isSuccess)
            Result.success(Data.Builder().putString(WORK_KEY_BLOCK, value).build())
        else
            Result.failure(Data.Builder().putString(WORK_KEY_BLOCK, value).build())

}