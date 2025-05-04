package com.example.reminders

import java.time.LocalDate

data class Reminder(
    val id: Int,
    var hour: Int,                   // 24 小時制（可修改，用於調整時間）
    var minute: Int,                 // 分鐘（可修改）
    val category: String,            // 提醒類別（例如血壓、體重等）
    val dayOfWeek: Int? = null,      // 指定星期提醒（0 表示周日，1 表示周一……，null 表示每天）
    val date: LocalDate? = null,     // 指定日期提醒（選填）
    val isRepeat: Boolean = false,   // 是否重複提醒
    val weekDays: List<String> = listOf(), // 多選星期（選填）
    val repeatInterval: RepeatInterval? = null, // 重複間隔
    var title: String = "提醒時間到了", // 通知標題（可修改）
    var content: String = "請打開APP",    // 通知內容（可修改）
    val priority: Priority = Priority.MEDIUM, // 提醒優先級
    val status: ReminderStatus = ReminderStatus.PENDING // 提醒狀態
) {
    // 格式化時間，例如 "08:30"
    fun getFormattedTime(): String = String.format("%02d:%02d", hour, minute)

    // 根據類別取得顯示文字（移除吃藥相關判斷）
    fun getReminderText(): String {
        return when (category) {
            MainActivity.Category.BLOOD_PRESSURE.value -> "請測量血壓！"
            MainActivity.Category.WEIGHT.value -> "請測量體重！"
            else -> "提醒！"
        }
    }

    // 依據 dayOfWeek 取得格式化文字
    fun getFormattedDayOfWeek(): String {
        return if (dayOfWeek == null || dayOfWeek == -1) "每天" else when (dayOfWeek) {
            0 -> "周日"
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            else -> "每天"
        }
    }

    // 重複間隔枚舉
    enum class RepeatInterval {
        DAILY, WEEKLY, MONTHLY
    }

    // 優先級枚舉
    enum class Priority {
        HIGH, MEDIUM, LOW
    }

    // 提醒狀態枚舉
    enum class ReminderStatus {
        PENDING, COMPLETED, SKIPPED
    }
}
