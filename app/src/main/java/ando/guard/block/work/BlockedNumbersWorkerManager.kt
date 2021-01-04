package ando.guard.block.work

import ando.guard.base.BaseActivity
import ando.guard.block.BlockedNumbersUtils
import ando.guard.block.db.BlockedNumber
import ando.guard.block.db.BlockedNumbersDaoManager
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
                Log.e("123", it.outputData.getString(WORK_KEY_BLOCK) ?: "")
            }
        })

    WorkManager.getInstance(activity).enqueue(request)
}

/**
 * sunflower
 *
 * https://blog.csdn.net/yingaizhu/article/details/105392459
 */
class BlockedNumbersDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val isJsonFileExist = BlockedNumbersDaoManager.isBlockedNumbersFileJsonExist()
            Log.e(
                "123", "Thread111 =${Thread.currentThread()} isJsonFileExist=$isJsonFileExist"
            )

            if (!isJsonFileExist) {
                BlockedNumbersDaoManager.loadBlockedNumbersFromJson().run {
                    if (isNotEmpty()) {
                        forEach { n: BlockedNumber ->
                            BlockedNumbersUtils.addBlockedNumber(n.number)
                        }
                        BlockedNumbersDaoManager.removeBlockedNumbersFileJson()
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