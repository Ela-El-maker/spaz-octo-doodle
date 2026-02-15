package com.spazoodle.guardian.domain.model

data class TriggerExecutionKey(
    val alarmId: Long,
    val kind: TriggerKind,
    val index: Int,
    val scheduledAtUtcMillis: Long
) {
    fun asString(): String {
        return "$alarmId:${kind.name}:$index:$scheduledAtUtcMillis"
    }

    companion object {
        fun fromString(raw: String): TriggerExecutionKey? {
            val parts = raw.split(":")
            if (parts.size != 4) return null
            val alarmId = parts[0].toLongOrNull() ?: return null
            val kind = runCatching { TriggerKind.valueOf(parts[1]) }.getOrNull() ?: return null
            val index = parts[2].toIntOrNull() ?: return null
            val scheduledAt = parts[3].toLongOrNull() ?: return null
            return TriggerExecutionKey(alarmId, kind, index, scheduledAt)
        }
    }
}
