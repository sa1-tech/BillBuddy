package com.billbuddy.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.billbuddy.model.Bill;
import com.billbuddy.receivers.BillReminderReceiver;

import java.util.Calendar;

public class ReminderUtil {

	// ✅ Set a one-time reminder for a bill's due date
	public static void setReminder(Context context, Bill bill) {
		long reminderTime = getReminderTimeInMillis(bill.getDueDate());

		if (reminderTime <= System.currentTimeMillis()) return; // Skip past-due

		Intent intent = new Intent(context, BillReminderReceiver.class);
		intent.putExtra("billId", bill.getId());
		intent.putExtra("title", bill.getTitle());

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context,
				(int) bill.getId(), // Unique ID per bill
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (alarmManager != null) {
			alarmManager.setExact(
					AlarmManager.RTC_WAKEUP,
					reminderTime,
					pendingIntent
			);
		}
	}

	// ✅ Helper to convert date string to milliseconds
	private static long getReminderTimeInMillis(String dueDate) {
		try {
			String[] parts = dueDate.split("-"); // Expected format: yyyy-MM-dd
			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based
			int day = Integer.parseInt(parts[2]);

			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month, day, 9, 0, 0); // Reminder at 9 AM

			return calendar.getTimeInMillis();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// Optional: Add cancelReminder(), rescheduleAll(), etc.
	public static void scheduleDailyReminder(Context context, int hourOfDay, int minute) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, BillReminderReceiver.class);
		intent.putExtra("title", "BillBuddy Daily Reminder");

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context,
				1001, // unique request code
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);

		// If time has already passed for today, schedule for tomorrow
		if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		if (alarmManager != null) {
			alarmManager.setRepeating(
					AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(),
					AlarmManager.INTERVAL_DAY,
					pendingIntent
			);
		}
	}

}
