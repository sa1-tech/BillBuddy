package com.billbuddy.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.billbuddy.R;
import com.billbuddy.model.Bill;
import com.billbuddy.utils.ReminderUtil;
import com.billbuddy.utils.SharedPrefManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BillDetailActivity extends AppCompatActivity {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	private EditText editTitle, editAmount, editNotes;
	private TextView editDueDate, attachmentName;
	private LinearLayout peopleLayout, attachmentPreviewLayout;
	private ImageView attachmentThumbnail;
	private Button btnUpdateBill, btnPickAttachment;

	private SharedPrefManager prefManager;
	private long billId;
	private Bill bill;
	private Calendar calendar;
	private Uri attachmentUri;

	private final ActivityResultLauncher<String[]> filePickerLauncher =
			registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
				if (uri != null) {
					getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
					attachmentUri = uri;
					showAttachmentPreview(uri);
				}
			});

	private List<CheckBox> personCheckboxes = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bill_detail);

		// Initialize views
		editTitle = findViewById(R.id.editTitle);
		editAmount = findViewById(R.id.editAmount);
		editDueDate = findViewById(R.id.editDueDate);
		editNotes = findViewById(R.id.editNotes);
		peopleLayout = findViewById(R.id.peopleLayout);
		btnUpdateBill = findViewById(R.id.btnUpdateBill);
		btnPickAttachment = findViewById(R.id.btnPickAttachment);
		attachmentPreviewLayout = findViewById(R.id.attachmentPreviewLayout);
		attachmentThumbnail = findViewById(R.id.attachmentThumbnail);
		attachmentName = findViewById(R.id.attachmentName);

		prefManager = new SharedPrefManager(this);
		calendar = Calendar.getInstance();

		billId = getIntent().getLongExtra("billId", -1);
		bill = prefManager.getBillById(billId);

		if (bill != null) {
			showDetails();
		} else {
			Toast.makeText(this, "Bill not found", Toast.LENGTH_SHORT).show();
			finish();
		}

		editDueDate.setOnClickListener(v -> showDatePicker());
		btnPickAttachment.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"application/pdf", "image/*"}));
		btnUpdateBill.setOnClickListener(v -> {
			if (validateInputs()) {
				updateBillData();
				Toast.makeText(this, "Bill updated successfully!", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}

	private void showDetails() {
		editTitle.setText(bill.getTitle());
		editAmount.setText(String.valueOf(bill.getAmount()));
		editDueDate.setText(bill.getDueDate());
		editNotes.setText(bill.getNotes());

		if (bill.getAttachmentPath() != null && !bill.getAttachmentPath().isEmpty()) {
			attachmentUri = Uri.parse(bill.getAttachmentPath());
			showAttachmentPreview(attachmentUri);
		}

		peopleLayout.removeAllViews();
		personCheckboxes.clear();

		LayoutInflater inflater = LayoutInflater.from(this);
		for (String person : bill.getPeople()) {
			View itemView = inflater.inflate(R.layout.item_person_checkbox, peopleLayout, false);
			CheckBox checkBox = itemView.findViewById(R.id.personCheckbox);
			TextView statusText = itemView.findViewById(R.id.paymentStatusText);

			String trimmedPerson = person.trim();
			boolean isPaid = bill.isPersonPaid(trimmedPerson);

			checkBox.setText(trimmedPerson);
			checkBox.setChecked(isPaid);
			updateStatusText(statusText, isPaid);

			checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
				updateStatusText(statusText, isChecked);
				bill.updatePaymentStatus(trimmedPerson, isChecked);
			});

			peopleLayout.addView(itemView);
			personCheckboxes.add(checkBox);
		}
	}

	private void showAttachmentPreview(Uri uri) {
		attachmentPreviewLayout.setVisibility(View.VISIBLE);
		String name = uri.getLastPathSegment();
		attachmentName.setText(name);

		String mimeType = getContentResolver().getType(uri);
		if (mimeType != null && mimeType.startsWith("image/")) {
			attachmentThumbnail.setImageURI(uri);
		} else {
			attachmentThumbnail.setImageResource(R.drawable.pdf); // fallback PDF icon
		}
	}

	private void updateStatusText(TextView statusText, boolean isPaid) {
		statusText.setText(isPaid ? "Paid" : "Not Paid");
		statusText.setTextColor(isPaid ? Color.parseColor("#4CAF50") : Color.parseColor("#FF5722"));
	}

	private void showDatePicker() {
		try {
			String dueDateStr = bill.getDueDate();
			if (!TextUtils.isEmpty(dueDateStr)) {
				calendar.setTime(dateFormat.parse(dueDateStr));
			}
		} catch (ParseException e) {
			Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
		}

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		new DatePickerDialog(this, (view, y, m, d) -> {
			calendar.set(y, m, d);
			String formattedDate = dateFormat.format(calendar.getTime());
			editDueDate.setText(formattedDate);
			bill.setDueDate(formattedDate);
		}, year, month, day).show();
	}

	private boolean validateInputs() {
		boolean valid = true;

		if (TextUtils.isEmpty(editTitle.getText())) {
			editTitle.setError("Title required");
			valid = false;
		}

		if (TextUtils.isEmpty(editAmount.getText())) {
			editAmount.setError("Amount required");
			valid = false;
		}

		if (TextUtils.isEmpty(editDueDate.getText())) {
			editDueDate.setError("Due date required");
			valid = false;
		}

		return valid;
	}

	private void updateBillData() {
		bill.setTitle(editTitle.getText().toString());

		try {
			double amount = Double.parseDouble(editAmount.getText().toString());
			bill.setAmount(amount);
		} catch (NumberFormatException e) {
			Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
			return;
		}

		bill.setDueDate(editDueDate.getText().toString());
		bill.setNotes(editNotes.getText().toString());

		if (attachmentUri != null) {
			bill.setAttachmentPath(attachmentUri.toString());
		}

		for (CheckBox cb : personCheckboxes) {
			String person = cb.getText().toString();
			bill.updatePaymentStatus(person, cb.isChecked());
		}

		ReminderUtil.setReminder(this, bill);
		prefManager.updateBill(bill);
	}
}
