package com.example.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ExistingWorkPolicy
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // 從 InputData 還原 Reminder 物件
        val reminder = getReminderFromInputData() ?: run {
            Log.e("ReminderWorker", "無法還原 Reminder 物件。")
            return Result.failure()
        }

        // 呼叫工具方法：喚醒螢幕、震動、播放音效
        ReminderUtils.wakeUpScreen(applicationContext)
        ReminderUtils.triggerVibration(applicationContext)
        ReminderUtils.playSoundFeedback(applicationContext)

        // 顯示通知
        showNotification(reminder)

        // 若提醒需要重複，則安排下一次提醒
        if (reminder.isRepeat) {
            scheduleNextReminder(reminder)
        }
        return Result.success()
    }

    private fun scheduleNextReminder(reminder: Reminder) {
        val nextTriggerTime = ReminderScheduler.calculateNextTriggerTime(reminder)
        val initialDelay = nextTriggerTime - System.currentTimeMillis()
        if (initialDelay <= 0) {
            Log.e("ReminderWorker", "初始延遲時間非正數，跳過排程。")
            return
        }

        val workData = workDataOf(
            "id" to reminder.id,
            "hour" to reminder.hour,
            "minute" to reminder.minute,
            "category" to reminder.category,
            "isRepeat" to reminder.isRepeat,
            "dayOfWeek" to (reminder.dayOfWeek ?: -1)
        )

        val nextWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        val uniqueWorkName = "reminder_${reminder.id}"
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            nextWorkRequest
        )
    }

    private fun showNotification(reminder: Reminder) {
        val channelId = "reminder_channel"
        val channelName = "Reminder Notifications"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, content) = getNotificationContent(reminder)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminder.id, notification)
    }

    private fun getNotificationContent(reminder: Reminder): Pair<String, String> {
        return when (reminder.category) {
            MainActivity.Category.BLOOD_PRESSURE.value -> Pair("血壓提醒", "該測量血壓了！")
            MainActivity.Category.WEIGHT.value -> Pair("體重提醒", "該測量體重了！")
            MainActivity.Category.WATER.value -> Pair("喝水提醒", "該喝水了!")
            MainActivity.Category.OTHER.value -> Pair("其他提醒", "請完成您的任務！")
            else -> Pair("提醒時間到了", "請查看詳細信息。")
        }
    }

    private fun getReminderFromInputData(): Reminder? {
        val id = inputData.getInt("id", System.currentTimeMillis().toInt())
        val hour = inputData.getInt("hour", -1)
        val minute = inputData.getInt("minute", -1)
        val category = inputData.getString("category") ?: "其他" // 預設為「其他」
        val isRepeat = inputData.getBoolean("isRepeat", false)
        val dayOfWeekRaw = inputData.getInt("dayOfWeek", -1)
        val dayOfWeek = if (dayOfWeekRaw == -1) null else dayOfWeekRaw

        if (hour == -1 || minute == -1) {
            Log.e("ReminderWorker", "無效的 hour 或 minute 輸入資料。")
            return null
        }

        return Reminder(
            id = id,
            hour = hour,
            minute = minute,
            category = category,
            dayOfWeek = dayOfWeek,
            isRepeat = isRepeat
        )
    }

}
