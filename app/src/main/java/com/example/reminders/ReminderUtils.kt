package com.example.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat

object ReminderUtils {
    private const val TAG = "ReminderUtils"
    private const val CHANNEL_ID = "reminder_channel"
    private const val CHANNEL_NAME = "提醒通知"

    /**
     * 播放通知音效
     */
    fun playSoundFeedback(context: Context) {
        try {
            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(context, notificationSound)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "播放音效時發生錯誤", e)
        }
    }

    /**
     * 喚醒螢幕（改用 PARTIAL_WAKE_LOCK 配合通知實作）
     */
    fun wakeUpScreen(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::ReminderWakeLock"
            )

            wakeLock?.acquire(5000)
            wakeLock?.release()
        } catch (e: Exception) {
            Log.e(TAG, "喚醒螢幕失敗", e)
        }
    }

    /**
     * 觸發震動反饋（使用 VibratorManager）
     */
    fun triggerVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (it.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE)
                        it.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(2000)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "震動發生錯誤", e)
        }
    }

    /**
     * 顯示通知
     */
    fun showNotification(context: Context, reminder: Reminder) {
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
                setSound(
                    notificationSound, AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminder.title)
            .setContentText(reminder.content)
            .setSound(notificationSound)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminder.id, notification)
    }
}
