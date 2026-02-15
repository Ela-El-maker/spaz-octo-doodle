package com.spazoodle.guardian.qa

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.spazoodle.guardian.worker.GuardianRetentionWorker
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RetentionWorkerRegistrationTest {

    @Test
    fun retentionWorker_isRegisteredAtStartup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(GuardianRetentionWorker.WORK_NAME)
            .get()

        assertTrue(
            "Expected periodic retention worker to be registered",
            workInfos.isNotEmpty()
        )
    }
}
