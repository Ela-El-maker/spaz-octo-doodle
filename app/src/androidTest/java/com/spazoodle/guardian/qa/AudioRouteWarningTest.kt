package com.spazoodle.guardian.qa

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.service.AlarmRingingService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioRouteWarningTest {

    @Test
    fun externalRouteWithoutSpeaker_showsWarning() {
        assertTrue(
            AlarmRingingService.shouldWarnExternalRoute(
                isExternalRouteLikely = true,
                isSpeakerphoneOn = false
            )
        )
    }

    @Test
    fun speakerOrNoExternalRoute_noWarning() {
        assertFalse(
            AlarmRingingService.shouldWarnExternalRoute(
                isExternalRouteLikely = true,
                isSpeakerphoneOn = true
            )
        )
        assertFalse(
            AlarmRingingService.shouldWarnExternalRoute(
                isExternalRouteLikely = false,
                isSpeakerphoneOn = false
            )
        )
    }
}
