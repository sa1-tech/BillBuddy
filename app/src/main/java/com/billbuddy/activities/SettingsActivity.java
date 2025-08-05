package com.billbuddy.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.billbuddy.R;
import com.billbuddy.model.Bill;
import com.billbuddy.utils.BackupUtils;
import com.billbuddy.utils.ExportUtils;
import com.billbuddy.utils.ReminderUtil;
import com.billbuddy.utils.SharedPrefManager;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

	private SharedPreferences prefs;
	private SharedPrefManager prefManager;
	private final ActivityResultLauncher<String> backupLauncher = registerForActivityResult(
			new ActivityResultContracts.CreateDocument("application/json"),
			uri -> {
				if (uri != null) {
					List<Bill> bills = prefManager.getAllBills();
					if (BackupUtils.exportToJson(this, uri, bills)) {
						Toast.makeText(this, "✅ Backup saved", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "❌ Backup failed", Toast.LENGTH_SHORT).show();
					}
				}
			});
	private final ActivityResultLauncher<String[]> restoreLauncher = registerForActivityResult(
			new ActivityResultContracts.OpenDocument(),
			uri -> {
				if (uri != null) {
					List<Bill> importedBills = BackupUtils.importFromJson(this, uri);
					if (importedBills != null) {
						for (Bill b : importedBills) prefManager.saveBill(b);
						Toast.makeText(this, "✅ Restore successful", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "❌ Restore failed", Toast.LENGTH_SHORT).show();
					}
				}
			});
	private Switch privacySwitch, timeFormatSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		prefs = getSharedPreferences("settings", MODE_PRIVATE);
		prefManager = new SharedPrefManager(this);

		privacySwitch = findViewById(R.id.switchPrivacy);
		timeFormatSwitch = findViewById(R.id.switchTimeFormat); // newly added

		Button exportCsvBtn = findViewById(R.id.btnExportCsv);
		Button exportPdfBtn = findViewById(R.id.btnExportPdf);
		Button backupBtn = findViewById(R.id.btnBackup);
		Button restoreBtn = findViewById(R.id.btnRestore);
		Button resetButton = findViewById(R.id.btnResetSettings);
		Button reminderTimeBtn = findViewById(R.id.btnReminderTime);

		// Load switches
		privacySwitch.setChecked(prefs.getBoolean("privacy_mode", false));
		timeFormatSwitch.setChecked(prefs.getBoolean("is_24_hour", true)); // default is 24hr

		// Listeners
		privacySwitch.setOnCheckedChangeListener((btn, checked) -> {
			prefs.edit().putBoolean("privacy_mode", checked).apply();
			Toast.makeText(this, checked ? "🔐 Privacy mode enabled" : "🔓 Privacy mode disabled", Toast.LENGTH_SHORT).show();
		});

		timeFormatSwitch.setOnCheckedChangeListener((btn, checked) -> {
			prefs.edit().putBoolean("is_24_hour", checked).apply();
			Toast.makeText(this, checked ? "🕒 24-hour format enabled" : "🕕 AM/PM format enabled", Toast.LENGTH_SHORT).show();
		});

		exportCsvBtn.setOnClickListener(v ->
				ExportUtils.exportToCsv(this, prefManager.getAllBills()));

		exportPdfBtn.setOnClickListener(v ->
				ExportUtils.exportToPdf(this, prefManager.getAllBills()));

		backupBtn.setOnClickListener(v ->
				backupLauncher.launch("billbuddy_backup.json"));

		restoreBtn.setOnClickListener(v ->
				restoreLauncher.launch(new String[]{"application/json"}));

		resetButton.setOnClickListener(v -> {
			prefs.edit().clear().apply();
			prefManager.clearAllBills();
			Toast.makeText(this, "🔄 Settings and bills reset", Toast.LENGTH_SHORT).show();
			restartApp();
		});

		reminderTimeBtn.setOnClickListener(v -> showTimePicker());
	}

	private void showTimePicker() {
		int hour = prefs.getInt("reminder_hour", 9);
		int minute = prefs.getInt("reminder_minute", 0);
		boolean is24Hour = prefs.getBoolean("is_24_hour", true);

		TimePickerDialog dialog = new TimePickerDialog(
				this,
				(view, hourOfDay, minuteOfHour) -> {
					prefs.edit()
							.putInt("reminder_hour", hourOfDay)
							.putInt("reminder_minute", minuteOfHour)
							.apply();

					// 🔔 Schedule alarm
					ReminderUtil.scheduleDailyReminder(SettingsActivity.this, hourOfDay, minuteOfHour);


					String formattedTime = String.format(
							is24Hour ? "%02d:%02d" : "%02d:%02d %s",
							is24Hour ? hourOfDay : (hourOfDay % 12 == 0 ? 12 : hourOfDay % 12),
							minuteOfHour,
							is24Hour ? "" : (hourOfDay < 12 ? "AM" : "PM")
					);

					Toast.makeText(this, "⏰ Reminder time set to " + formattedTime, Toast.LENGTH_SHORT).show();
				},
				hour, minute, is24Hour
		);
		dialog.show();
	}


	private void restartApp() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finishAffinity();
	}
}
