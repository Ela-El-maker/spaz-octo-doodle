# permissions rationale

## purpose

This document explains why Guardian requests each Android permission and what user value it enables.

## permissions

### POST_NOTIFICATIONS

- reason: deliver pre-alerts, reliability warnings, and alarm notifications.
- user value: user receives reminders and actionable alerts.
- behavior if denied: alarms may still fire via service/full-screen path, but notification-based guidance and pre-alert UX degrades.

### RECEIVE_BOOT_COMPLETED

- reason: Android clears scheduled alarms on reboot.
- user value: enabled plans are rescheduled automatically after restart.
- behavior if denied/unavailable: future alarms may be lost after reboot.

### SCHEDULE_EXACT_ALARM

- reason: far-future plans require exact trigger delivery.
- user value: alerts fire at intended date/time instead of delayed batching.
- behavior if denied: app enters degraded mode and prompts user via reliability dashboard.

### WAKE_LOCK

- reason: stabilize ringing startup and avoid immediate sleep during critical action window.
- user value: alarm playback/interaction is reliable.
- behavior if denied/unavailable: higher risk of interrupted alert experience.

### FOREGROUND_SERVICE

- reason: maintain alarm playback lifecycle under background execution limits.
- user value: ringing remains active until explicit user action.
- behavior if denied/unavailable: service execution may be killed, reducing delivery reliability.

### VIBRATE

- reason: provide tactile alert channel.
- user value: detectable alerts in noisy or muted environments.

## policy notes

- permissions are requested only when needed for reliability-critical paths.
- rationale text should be presented before settings deep-link actions.
- denied permissions must map to clear fallback messaging in reliability dashboard.
