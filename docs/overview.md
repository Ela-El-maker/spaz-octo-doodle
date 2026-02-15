# Guardian Overview

## Purpose

Guardian is a reliability-first Android alarm app for exact, future date-time plans.
It is built to reduce missed alarms caused by common Android lifecycle behavior and OEM background restrictions.

## What It Does

- Creates one-time exact alarms for a specific date and time.
- Supports pre-alerts before the main trigger.
- Uses a foreground ringing flow with stop/snooze and optional primary action.
- Tracks outcomes in history (fired, recovered-late, missed, dismissed, snoozed).
- Shows reliability risk and setup guidance through a dashboard.

## Reliability Boundaries

- Swiping app from recents should still allow alarms to fire.
- Force-stop from Android settings blocks delivery until the app is opened again.
- Clearing app data removes schedules.
- Device powered off at trigger time cannot ring until next boot.

## Current Focus

Current implementation prioritizes reliability, state correctness, and diagnostics over feature breadth.
Advanced features (NLP, cloud sync, location alarms) are intentionally deferred.
