package com.billbuddy.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.billbuddy.R;
import com.billbuddy.activities.BillDetailActivity;

public class NotificationUtil {

	private static final String TAG = "NotificationUtil";
	private static final String CHANNEL_ID = "bill_reminders";
	private static final String CHANNEL_NAME = "Bill Reminders";
	private static final String CHANNEL_DESCRIPTION = "Notifications for upcoming or due bills";

	public static void showReminderNotification(Context context, long billId, String billTitle) {
		NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);

		if (notificationManager == null) {
			Log.e(TAG, "NotificationManager is null. Cannot show notification.");
			return;
		}

		// ✅ Create notification channel (Android O+)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					CHANNEL_NAME,
					NotificationManager.IMPORTANCE_HIGH
			);
			channel.setDescription(CHANNEL_DESCRIPTION);
			notificationManager.createNotificationChannel(channel);
		}

		// 📲 Open BillDetailActivity when user taps notification
		Intent intent = new Intent(context, BillDetailActivity.class);
		intent.putExtra("billId", billId);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				context,
				(int) billId, // Unique per bill
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		// 🔔 Build the notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.bell) // ✅ Ensure "bell.png" or "bell.xml" exists in /res/drawable
				.setContentTitle("📅 Bill Due Today")
				.setContentText("Reminder: \"" + billTitle + "\" is due. Tap to review.")
				.setContentIntent(pendingIntent)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setCategory(NotificationCompat.CATEGORY_REMINDER)
				.setAutoCancel(true);

		// 🚀 Show it
		notificationManager.notify((int) billId, builder.build());

		Log.d(TAG, "Notification shown for bill ID: " + billId);
	}
}
