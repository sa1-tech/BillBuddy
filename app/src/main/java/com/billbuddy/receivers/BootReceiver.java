package com.billbuddy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.billbuddy.model.Bill;
import com.billbuddy.utils.ReminderUtil;
import com.billbuddy.utils.SharedPrefManager;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null || intent.getAction() == null) {
			Log.w(TAG, "Received null or empty intent");
			return;
		}

		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
				|| Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
			Log.i(TAG, "Boot or update completed. Reinitializing reminders...");

			SharedPrefManager prefManager = new SharedPrefManager(context);
			List<Bill> allBills = prefManager.getAllBills();

			SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
			int reminderHour = prefs.getInt("reminder_hour", 9); // Default: 9 AM

			for (Bill bill : allBills) {
				if (bill.getDueDate() != null && !bill.getDueDate().isEmpty()) {
					// 🛠️ Actually reschedule the reminder
					ReminderUtil.setReminder(context, bill);
					Log.d(TAG, "Reminder restored for bill: " + bill.getTitle());
				}
			}

			Log.i(TAG, "All reminders restored successfully.");
		} else {
			Log.w(TAG, "Unexpected action received: " + intent.getAction());
		}
	}
}
