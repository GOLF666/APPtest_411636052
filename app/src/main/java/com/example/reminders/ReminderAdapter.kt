package com.example.reminders

import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit

class ReminderAdapter(
    private val reminders: List<Reminder>,
    private val onReminderDelete: (Reminder) -> Unit,
    private val onReminderTimeChanged: (Reminder) -> Unit,
    private val onReminderEdit: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder)
    }

    override fun getItemCount(): Int = reminders.size

    override fun onViewRecycled(holder: ReminderViewHolder) {
        super.onViewRecycled(holder)
        holder.clearCountdown()
    }

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvCountdown: TextView = itemView.findViewById(R.id.tvCountdown)
        private val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        private val imgCategoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)

        private var countDownTimer: CountDownTimer? = null

        fun bind(reminder: Reminder) {
            tvTime.text = reminder.getFormattedTime()
            tvCategory.text = reminder.getReminderText()

            // 根據提醒類別設定背景色
            val backgroundColor = when (reminder.category) {
                MainActivity.Category.BLOOD_PRESSURE.value -> Color.parseColor("#FFCDD2")
                MainActivity.Category.WEIGHT.value -> Color.parseColor("#E0E0E0")
                MainActivity.Category.WATER.value -> Color.parseColor("#BBDEFB")
                else -> Color.WHITE
            }
            itemView.setBackgroundColor(backgroundColor)

            // 設定圖示（請確認對應 drawable 存在）
            val iconRes = when (reminder.category) {
                MainActivity.Category.BLOOD_PRESSURE.value -> R.drawable.ic_blood_pressure
                MainActivity.Category.WEIGHT.value -> R.drawable.ic_scale
                MainActivity.Category.WATER.value -> R.drawable.ic_water
                else -> R.drawable.ic_other
            }
            imgCategoryIcon.setImageResource(iconRes)

            tvDayOfWeek.text = reminder.getFormattedDayOfWeek()

            // 啟動倒數計時
            startCountdown(reminder)

            btnDelete.setOnClickListener {
                onReminderDelete(reminder)
            }

            btnEdit.setOnClickListener {
                onReminderEdit(reminder)
            }
        }

        private fun startCountdown(reminder: Reminder) {
            clearCountdown()
            val nextTriggerTime = ReminderScheduler.calculateNextTriggerTime(reminder)
            val timeRemaining = nextTriggerTime - System.currentTimeMillis()
            if (timeRemaining > 0) {
                countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                        tvCountdown.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    }
                    override fun onFinish() {
                        tvCountdown.text = "Reminder!"
                        tvCountdown.setTextColor(Color.RED)
                    }
                }.start()
            } else {
                tvCountdown.text = "Reminder!"
                tvCountdown.setTextColor(Color.RED)
            }
        }

        fun clearCountdown() {
            countDownTimer?.cancel()
            countDownTimer = null
        }
    }
}
