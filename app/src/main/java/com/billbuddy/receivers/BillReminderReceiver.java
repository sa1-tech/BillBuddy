package com.billbuddy.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.billbuddy.R;

public class BillReminderReceiver extends BroadcastReceiver {

	private static final String TAG = "BillReminderReceiver";
	private static final String CHANNEL_ID = "rem_chan";
	private static final String CHANNEL_NAME = "Bill Reminders";
	private static final String CHANNEL_DESC = "Notifications for due bills";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Reminder triggered!");

		long billId = intent.getLongExtra("billId", -1);
		String billTitle = intent.getStringExtra("billTitle");

		if (billTitle == null || billTitle.trim().isEmpty()) {
			billTitle = "Your bill is due today!";
		}

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Create notification channel (required for Android 8.0+)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
			channel.setDescription(CHANNEL_DESC);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(channel);
			}
		}

		// Build notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_reminder) // ✅ make sure this drawable exists
				.setContentTitle("📅 Bill Due Reminder").setContentText("Reminder: \"" + billTitle + "\" is due today.").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true);

		// Show the notification
		if (notificationManager != null) {
			notificationManager.notify((int) billId, builder.build());
		} else {
			Log.e(TAG, "NotificationManager is null");
		}
	}
}
