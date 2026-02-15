package com.spazoodle.guardian.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.spazoodle.guardian.GuardianApp
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.platform.reliability.GuardianDiagnosticTags
import com.spazoodle.guardian.platform.reliability.GuardianFeatureFlags
import com.spazoodle.guardian.runtime.GuardianRuntime
import com.spazoodle.guardian.receiver.AlarmActionReceiver
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver
import com.spazoodle.guardian.ui.ringing.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmRingingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var wasSpeakerphoneOnBefore: Boolean = false
    private var wasModeBefore: Int = AudioManager.MODE_NORMAL
    private var routeWarningActive: Boolean = false
    private var currentAlarmId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = getSystemService(Vibrator::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, -1L) ?: -1L
        currentAlarmId = alarmId
        val title = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_ALARM_TITLE).orEmpty()
        val description = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_ALARM_DESCRIPTION).orEmpty()
        val vibrateEnabled = intent?.getBooleanExtra(AlarmTriggerReceiver.EXTRA_ALARM_VIBRATE_ENABLED, true) ?: true
        val ringtoneUri = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_ALARM_RINGTONE_URI)
        val triggerKind = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_TRIGGER_KIND).orEmpty()
        val primaryActionType = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_TYPE)
        val primaryActionValue = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_VALUE)
        val primaryActionLabel = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_LABEL)
        val fullScreenReady = intent?.getBooleanExtra(AlarmTriggerReceiver.EXTRA_FULL_SCREEN_READY, true) ?: true

        acquireWakeLock()

        startForeground(
            NOTIFICATION_ID,
            buildForegroundNotification(
                alarmId = alarmId,
                title = title,
                description = description,
                triggerKind = triggerKind,
                fullScreenReady = fullScreenReady,
                primaryActionType = primaryActionType,
                primaryActionValue = primaryActionValue,
                primaryActionLabel = primaryActionLabel
            )
        )

        launchRingingUi(
            alarmId = alarmId,
            title = title,
            description = description,
            routeWarning = routeWarningActive,
            primaryActionType = primaryActionType,
            primaryActionValue = primaryActionValue,
            primaryActionLabel = primaryActionLabel
        )

        startRingingLoop(
            vibrateEnabled = vibrateEnabled,
            ringtoneUriString = ringtoneUri
        )
        return START_STICKY
    }

    override fun onDestroy() {
        stopRingingLoop()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRingingLoop(
        vibrateEnabled: Boolean,
        ringtoneUriString: String?
    ) {
        requestAudioFocus()
        attemptSpeakerRoute()
        routeWarningActive = shouldShowRouteWarning()

        val toneUri = resolveToneUri(ringtoneUriString)

        mediaPlayer?.release()
        mediaPlayer = createMediaPlayer(toneUri)
            ?: createMediaPlayer(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )

        if (mediaPlayer == null) {
            stopSelf()
            return
        }
        emitRouteWarningIfNeeded()

        if (vibrateEnabled) startVibration()
    }

    private fun stopRingingLoop() {
        mediaPlayer?.run {
            runCatching { stop() }
            release()
        }
        mediaPlayer = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.cancel()
        } else {
            @Suppress("DEPRECATION")
            vibrator?.cancel()
        }

        abandonAudioFocus()
        restoreAudioRoute()
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 700, 450, 700)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun attemptSpeakerRoute() {
        runCatching {
            wasSpeakerphoneOnBefore = audioManager.isSpeakerphoneOn
            wasModeBefore = audioManager.mode
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = true
        }
    }

    private fun restoreAudioRoute() {
        runCatching {
            audioManager.isSpeakerphoneOn = wasSpeakerphoneOnBefore
            audioManager.mode = wasModeBefore
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun acquireWakeLock() {
        val manager = getSystemService(PowerManager::class.java)
        wakeLock?.release()
        wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            runCatching {
                if (lock.isHeld) lock.release()
            }
        }
        wakeLock = null
    }

    private fun launchRingingUi(
        alarmId: Long,
        title: String,
        description: String,
        routeWarning: Boolean,
        primaryActionType: String?,
        primaryActionValue: String?,
        primaryActionLabel: String?
    ) {
        val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_DESCRIPTION, description)
            putExtra(AlarmTriggerReceiver.EXTRA_AUDIO_ROUTE_WARNING, routeWarning)
            putExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_TYPE, primaryActionType)
            putExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_VALUE, primaryActionValue)
            putExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_LABEL, primaryActionLabel)
        }
        runCatching { startActivity(activityIntent) }
    }

    private fun buildForegroundNotification(
        alarmId: Long,
        title: String,
        description: String,
        triggerKind: String,
        fullScreenReady: Boolean,
        primaryActionType: String?,
        primaryActionValue: String?,
        primaryActionLabel: String?
    ): Notification {
        val displayTitle = if (title.isBlank()) "Guardian alarm" else title
        val fullScreenIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            Intent(this, AlarmRingingActivity::class.java).apply {
                putExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmTriggerReceiver.EXTRA_ALARM_TITLE, displayTitle)
                putExtra(AlarmTriggerReceiver.EXTRA_ALARM_DESCRIPTION, description)
                putExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_TYPE, primaryActionType)
                putExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_VALUE, primaryActionValue)
                putExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_LABEL, primaryActionLabel)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getBroadcast(
            this,
            ("stop_$alarmId").hashCode(),
            Intent(this, AlarmActionReceiver::class.java).apply {
                action = AlarmActionReceiver.ACTION_STOP
                putExtra(AlarmActionReceiver.EXTRA_ALARM_ID, alarmId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = PendingIntent.getBroadcast(
            this,
            ("snooze10_$alarmId").hashCode(),
            Intent(this, AlarmActionReceiver::class.java).apply {
                action = AlarmActionReceiver.ACTION_SNOOZE
                putExtra(AlarmActionReceiver.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmActionReceiver.EXTRA_SNOOZE_MINUTES, 10)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, GuardianApp.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(displayTitle)
            .setContentText(
                if (fullScreenReady) {
                    "Trigger: $triggerKind"
                } else {
                    "Trigger: $triggerKind • Full-screen blocked, running heads-up fallback"
                }
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(fullScreenIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 10", snoozeIntent)

        if (!primaryActionValue.isNullOrBlank()) {
            val doIntent = PendingIntent.getBroadcast(
                this,
                ("do_$alarmId").hashCode(),
                Intent(this, AlarmActionReceiver::class.java).apply {
                    action = AlarmActionReceiver.ACTION_DO
                    putExtra(AlarmActionReceiver.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmActionReceiver.EXTRA_PRIMARY_ACTION_TYPE, primaryActionType)
                    putExtra(AlarmActionReceiver.EXTRA_PRIMARY_ACTION_VALUE, primaryActionValue)
                    putExtra(AlarmActionReceiver.EXTRA_PRIMARY_ACTION_LABEL, primaryActionLabel)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_view,
                primaryActionLabel.takeUnless { it.isNullOrBlank() } ?: "Do",
                doIntent
            )
        }

        return builder.build()
    }

    private fun resolveToneUri(uriString: String?): Uri? {
        val custom = runCatching {
            uriString?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        }.getOrNull()
        return custom
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    private fun shouldShowRouteWarning(): Boolean {
        if (!GuardianFeatureFlags.audioRouteWarning) return false
        @Suppress("DEPRECATION")
        val wiredHeadsetOn = audioManager.isWiredHeadsetOn
        val isExternalRouteLikely = audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn || wiredHeadsetOn
        return shouldWarnExternalRoute(
            isExternalRouteLikely = isExternalRouteLikely,
            isSpeakerphoneOn = audioManager.isSpeakerphoneOn
        )
    }

    private fun emitRouteWarningIfNeeded() {
        if (!routeWarningActive) return
        val alarmId = currentAlarmId
        if (alarmId <= 0L) return
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                GuardianRuntime.recordFireEventUseCase(this@AlarmRingingService).invoke(
                    alarmId = alarmId,
                    triggerKind = TriggerKind.MAIN,
                    outcome = AlarmEventOutcome.FIRED,
                    deliveryState = DeliveryState.FIRED,
                    detail = GuardianDiagnosticTags.AUDIO_ROUTE_EXTERNAL_WARNING
                )
            }
        }
    }

    private fun createMediaPlayer(toneUri: Uri?): MediaPlayer? {
        if (toneUri == null) return null
        return runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setDataSource(this@AlarmRingingService, toneUri)
                prepare()
                start()
            }
        }.getOrNull()
    }

    companion object {
        const val NOTIFICATION_ID = 1001

        private const val WAKE_LOCK_TAG = "guardian:alarm_wake_lock"
        private const val WAKE_LOCK_TIMEOUT_MS = 2 * 60 * 1000L

        fun shouldWarnExternalRoute(
            isExternalRouteLikely: Boolean,
            isSpeakerphoneOn: Boolean
        ): Boolean {
            return isExternalRouteLikely && !isSpeakerphoneOn
        }
    }
}
