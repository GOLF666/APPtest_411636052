package com.example.reminders

import java.util.*

object ReminderScheduler {
    /**
     * 根據 Reminder 內容計算下一次觸發的時間（以毫秒為單位）
     *
     * 若有指定 dayOfWeek（0 表示周日），則計算下一次該星期的日期；
     * 若未指定，則若當日已過則安排明天。
     */
    fun calculateNextTriggerTime(reminder: Reminder): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = System.currentTimeMillis()
        var triggerTime = calendar.timeInMillis

        if (reminder.dayOfWeek != null && reminder.dayOfWeek in 0..6) {
            // 調整至下次符合指定星期的日期
            while (calendar.get(Calendar.DAY_OF_WEEK) != convertToCalendarDay(reminder.dayOfWeek)) {
                calendar.add(Calendar.DATE, 1)
            }
            triggerTime = calendar.timeInMillis
            if (triggerTime <= now) {
                // 若時間已過，安排下一週同一天
                calendar.add(Calendar.DATE, 7)
                triggerTime = calendar.timeInMillis
            }
        } else {
            // 若未指定星期，若當日已過則安排明天
            if (triggerTime <= now) {
                calendar.add(Calendar.DATE, 1)
                triggerTime = calendar.timeInMillis
            }
        }
        return triggerTime
    }

    /**
     * 將 Reminder 的 dayOfWeek（0=周日, 1=周一, …）轉換為 Calendar.DAY_OF_WEEK
     */
    private fun convertToCalendarDay(day: Int): Int {
        return when (day) {
            0 -> Calendar.SUNDAY
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }
    }
}
