package com.punchlist.app.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.punchlist.app.data.repository.StorageRepository
import com.punchlist.app.util.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// Queued when the device is offline at item creation time.
// WorkManager will retry when connectivity is restored (requires NETWORK constraint).

@HiltWorker
class PhotoUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val storageRepository: StorageRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val localUri = inputData.getString(KEY_LOCAL_URI)?.let { Uri.parse(it) }
            ?: return Result.failure()
        val projectId = inputData.getString(KEY_PROJECT_ID) ?: return Result.failure()
        val itemId = inputData.getString(KEY_ITEM_ID) ?: return Result.failure()
        val isCompletion = inputData.getBoolean(KEY_IS_COMPLETION, false)

        return when (storageRepository.uploadPhoto(localUri, projectId, itemId, isCompletion)) {
            is com.punchlist.app.util.Result.Success -> Result.success()
            is com.punchlist.app.util.Result.Error -> if (runAttemptCount < 3) Result.retry() else Result.failure()
            com.punchlist.app.util.Result.Loading -> Result.retry()
        }
    }

    companion object {
        const val KEY_LOCAL_URI = "local_uri"
        const val KEY_PROJECT_ID = "project_id"
        const val KEY_ITEM_ID = "item_id"
        const val KEY_IS_COMPLETION = "is_completion"

        fun buildRequest(localUri: Uri, projectId: String, itemId: String, isCompletion: Boolean): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<PhotoUploadWorker>()
                .setInputData(
                    workDataOf(
                        KEY_LOCAL_URI to localUri.toString(),
                        KEY_PROJECT_ID to projectId,
                        KEY_ITEM_ID to itemId,
                        KEY_IS_COMPLETION to isCompletion
                    )
                )
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, java.util.concurrent.TimeUnit.SECONDS)
                .build()
    }
}
