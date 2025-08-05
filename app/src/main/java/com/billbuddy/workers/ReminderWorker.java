package com.billbuddy.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.billbuddy.R;

public class ReminderWorker extends Worker {

	public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
		super(context, params);
	}

	@NonNull
	@Override
	public Result doWork() {
		String title = getInputData().getString("title");
		String dueDate = getInputData().getString("dueDate");

		showNotification("Bill Reminder", "Bill \"" + title + "\" is due on " + dueDate);
		return Result.success();
	}

	private void showNotification(String title, String message) {
		NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		String channelId = "bill_reminder";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(channelId, "Bill Reminders", NotificationManager.IMPORTANCE_HIGH);
			manager.createNotificationChannel(channel);
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(R.drawable.bell) // Use your own icon
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true);

		manager.notify((int) System.currentTimeMillis(), builder.build());
	}
}
