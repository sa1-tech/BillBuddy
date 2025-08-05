package com.billbuddy.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.billbuddy.R;
import com.billbuddy.model.Bill;
import com.billbuddy.receivers.BillReminderReceiver;
import com.billbuddy.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddBillActivity extends AppCompatActivity {

	private final String[] categories = {"Utilities", "Entertainment", "Groceries", "Rent", "Miscellaneous"};

	private EditText titleInput, amountInput, peopleInput, notesInput;
	private TextView dueDateText, attachmentNameText;
	private Spinner categorySpinner;
	private Button saveBtn, pickAttachmentBtn;
	private ImageView thumbnailView;

	private Calendar calendar = Calendar.getInstance();
	private String dueDateStr = "";
	private Uri attachmentUri = null;
	private final ActivityResultLauncher<Intent> attachmentPickerLauncher =
			registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
				if (result.getResultCode() == RESULT_OK && result.getData() != null) {
					attachmentUri = result.getData().getData();
					if (attachmentUri != null) {
						getContentResolver().takePersistableUriPermission(
								attachmentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
						handleAttachmentPreview(attachmentUri);
					}
				}
			});
	private SharedPrefManager prefManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_bill);

		initUI();
		setupListeners();
	}

	private void initUI() {
		titleInput = findViewById(R.id.inputTitle);
		amountInput = findViewById(R.id.inputAmount);
		peopleInput = findViewById(R.id.inputPeople);
		notesInput = findViewById(R.id.inputNotes);
		dueDateText = findViewById(R.id.dueDateText);
		attachmentNameText = findViewById(R.id.attachmentName);
		categorySpinner = findViewById(R.id.spinnerCategory);
		saveBtn = findViewById(R.id.btnSave);
		pickAttachmentBtn = findViewById(R.id.btnPickAttachment);
		thumbnailView = findViewById(R.id.thumbnailView);

		prefManager = new SharedPrefManager(this);

		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, categories);
		categorySpinner.setAdapter(categoryAdapter);
	}

	private void setupListeners() {
		dueDateText.setOnClickListener(v -> showDatePicker());
		pickAttachmentBtn.setOnClickListener(v -> openAttachmentPicker());
		thumbnailView.setOnClickListener(v -> openAttachmentExternally());
		saveBtn.setOnClickListener(v -> saveBill());
	}

	private void showDatePicker() {
		DatePickerDialog datePicker = new DatePickerDialog(this,
				(view, year, month, dayOfMonth) -> {
					calendar.set(year, month, dayOfMonth);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
					dueDateStr = sdf.format(calendar.getTime());
					dueDateText.setText("Due: " + dueDateStr);
				},
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));
		datePicker.show();
	}

	private void openAttachmentPicker() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		attachmentPickerLauncher.launch(intent);
	}

	private void handleAttachmentPreview(Uri uri) {
		String mimeType = getContentResolver().getType(uri);
		if (mimeType == null) {
			Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
			return;
		}

		if (mimeType.startsWith("image/")) {
			thumbnailView.setImageURI(uri);
		} else if (mimeType.equals("application/pdf")) {
			renderPdfThumbnail(uri);
		}

		thumbnailView.setVisibility(ImageView.VISIBLE);
		attachmentNameText.setText(getFileName(uri));
		attachmentNameText.setVisibility(TextView.VISIBLE);
	}

	private void renderPdfThumbnail(Uri uri) {
		try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
			if (pfd != null) {
				PdfRenderer renderer = new PdfRenderer(pfd);
				if (renderer.getPageCount() > 0) {
					PdfRenderer.Page page = renderer.openPage(0);
					Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
					page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
					thumbnailView.setImageBitmap(bitmap);
					page.close();
				}
				renderer.close();
			}
		} catch (Exception e) {
			Toast.makeText(this, "Failed to preview PDF", Toast.LENGTH_SHORT).show();
		}
	}

	private void openAttachmentExternally() {
		if (attachmentUri != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(attachmentUri, getContentResolver().getType(attachmentUri));
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(intent);
		}
	}

	private void saveBill() {
		String title = titleInput.getText().toString().trim();
		String amountStr = amountInput.getText().toString().trim();
		String peopleStr = peopleInput.getText().toString().trim();
		String notes = notesInput.getText().toString().trim();
		String category = (String) categorySpinner.getSelectedItem();

		if (title.isEmpty() || amountStr.isEmpty() || peopleStr.isEmpty()) {
			Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
			return;
		}

		if (dueDateStr.isEmpty()) {
			dueDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
					.format(Calendar.getInstance().getTime());
		}

		double amount;
		try {
			amount = Double.parseDouble(amountStr);
		} catch (NumberFormatException e) {
			Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
			return;
		}

		List<String> people = new ArrayList<>();
		for (String name : peopleStr.split(",")) {
			if (!name.trim().isEmpty()) {
				people.add(name.trim());
			}
		}

		long id = System.currentTimeMillis();
		String attachmentPath = attachmentUri != null ? attachmentUri.toString() : null;

		Bill bill = new Bill(id, title, amount, dueDateStr, notes, people, category, attachmentPath);
		prefManager.saveBill(bill);

		scheduleReminder(calendar, title);

		Toast.makeText(this, "Bill saved & reminder set!", Toast.LENGTH_SHORT).show();
		finish();
	}

	private void scheduleReminder(Calendar calendar, String title) {
		long triggerAtMillis = calendar.getTimeInMillis();
		if (triggerAtMillis <= System.currentTimeMillis()) {
			return; // Avoid setting past reminders
		}

		Intent intent = new Intent(this, BillReminderReceiver.class);
		intent.putExtra("reminderTitle", title);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		if (alarmManager != null) {
			alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
		}
	}

	private String getFileName(Uri uri) {
		String result = "Attachment";
		try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
			if (cursor != null && cursor.moveToFirst()) {
				int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				if (idx >= 0) {
					result = cursor.getString(idx);
				}
			}
		}
		return result;
	}
}
