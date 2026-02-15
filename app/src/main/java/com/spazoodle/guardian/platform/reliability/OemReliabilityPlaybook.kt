package com.spazoodle.guardian.platform.reliability

import android.content.Context
import android.content.Intent
import android.provider.Settings

data class OemStep(
    val title: String,
    val detail: String
)

interface OemReliabilityPlaybook {
    fun supports(manufacturer: String): Boolean
    fun steps(): List<OemStep>
    fun settingsIntents(context: Context): List<Intent>
}

object OemReliabilityPlaybooks {
    fun resolve(manufacturer: String): OemReliabilityPlaybook? {
        return ALL.firstOrNull { it.supports(manufacturer) }
    }

    private val samsung = object : OemReliabilityPlaybook {
        override fun supports(manufacturer: String): Boolean = manufacturer.contains("samsung", true)
        override fun steps(): List<OemStep> = listOf(
            OemStep("Allow background activity", "Settings > Apps > Guardian > Battery > Unrestricted"),
            OemStep("Disable sleeping apps", "Settings > Battery > Background usage limits > Never sleeping apps")
        )

        override fun settingsIntents(context: Context): List<Intent> {
            return listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
        }
    }

    private val xiaomi = object : OemReliabilityPlaybook {
        override fun supports(manufacturer: String): Boolean {
            val m = manufacturer.lowercase()
            return m.contains("xiaomi") || m.contains("redmi") || m.contains("poco")
        }

        override fun steps(): List<OemStep> = listOf(
            OemStep("Autostart", "Security app > Permissions > Autostart > enable Guardian"),
            OemStep("No battery restriction", "Settings > Apps > Guardian > Battery saver > No restrictions")
        )

        override fun settingsIntents(context: Context): List<Intent> {
            return listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
        }
    }

    private val tecnoInfinix = object : OemReliabilityPlaybook {
        override fun supports(manufacturer: String): Boolean {
            val m = manufacturer.lowercase()
            return m.contains("tecno") || m.contains("infinix")
        }

        override fun steps(): List<OemStep> = listOf(
            OemStep("Allow auto-start", "Phone Master/Settings > Startup manager > enable Guardian"),
            OemStep("Allow background", "Settings > Apps > Guardian > Battery > No restrictions")
        )

        override fun settingsIntents(context: Context): List<Intent> {
            return listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
        }
    }

    private val oppoVivo = object : OemReliabilityPlaybook {
        override fun supports(manufacturer: String): Boolean {
            val m = manufacturer.lowercase()
            return m.contains("oppo") || m.contains("vivo")
        }

        override fun steps(): List<OemStep> = listOf(
            OemStep("Auto launch", "Settings > Apps > Auto launch > enable Guardian"),
            OemStep("Battery unrestricted", "Settings > Battery > App battery management > allow background")
        )

        override fun settingsIntents(context: Context): List<Intent> {
            return listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
        }
    }

    private val ALL: List<OemReliabilityPlaybook> = listOf(
        samsung,
        xiaomi,
        tecnoInfinix,
        oppoVivo
    )
}
