package com.billbuddy.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.billbuddy.R;
import com.billbuddy.activities.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		showReminderNotification(context);
	}

	private void showReminderNotification(Context context) {
		String channelId = "reminder_channel";
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// 🔊 Create channel if needed
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_HIGH);
			channel.setDescription("Reminder to review your bills");
			if (manager != null) manager.createNotificationChannel(channel);
		}

		// 🔔 Intent to open app
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		// 📣 Build notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId).setSmallIcon(R.drawable.bell).setContentTitle("💸 BillBuddy Reminder").setContentText("Don’t forget to review your pending bills today.").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setContentIntent(pendingIntent);

		if (manager != null) {
			manager.notify(2001, builder.build());
		}
	}
}
