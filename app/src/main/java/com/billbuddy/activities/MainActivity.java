package com.billbuddy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.billbuddy.R;
import com.billbuddy.adapter.BillAdapter;
import com.billbuddy.model.Bill;
import com.billbuddy.utils.SharedPrefManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

	private RecyclerView recyclerView;
	private TextView emptyView, totalAmountTextView, averageAmountTextView;
	private ImageView addBillBtn, settingsBtn;
	private BillAdapter adapter;
	private SharedPrefManager prefManager;
	private boolean isPrivacyMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		ThemeUtils.applySettings(this);
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
		isPrivacyMode = prefs.getBoolean("privacy_mode", false);

		if (isPrivacyMode) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_SECURE,
					WindowManager.LayoutParams.FLAG_SECURE
			);
		}

		setContentView(R.layout.activity_main);

		// Init views
		recyclerView = findViewById(R.id.recyclerView);
		emptyView = findViewById(R.id.emptyText);
		totalAmountTextView = findViewById(R.id.totalAmountTextView);
		averageAmountTextView = findViewById(R.id.averageAmountTextView);
		addBillBtn = findViewById(R.id.btnAddBill);
		settingsBtn = findViewById(R.id.btnSettings);

		prefManager = new SharedPrefManager(this);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		// Load data
		loadBills();

		// Click listeners
		addBillBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddBillActivity.class)));

		settingsBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
	}

	private void loadBills() {
		List<Bill> billList = prefManager.getAllBills();
		if (billList.isEmpty()) {
			emptyView.setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);
			adapter = new BillAdapter(this, billList, isPrivacyMode);
			recyclerView.setAdapter(adapter);
			showAnalytics(billList);
		}
	}

	private void showAnalytics(List<Bill> billList) {
		double total = 0;
		for (Bill bill : billList) {
			total += bill.getAmount();
		}

		double avg = billList.size() > 0 ? total / billList.size() : 0;

		if (isPrivacyMode) {
			totalAmountTextView.setText("Total Expenses: ****");
			averageAmountTextView.setText("Average: ****");
		} else {
			totalAmountTextView.setText(String.format("Total Expenses: ₹%.2f", total));
			averageAmountTextView.setText(String.format("Average: ₹%.2f", avg));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
		boolean updatedPrivacyMode = prefs.getBoolean("privacy_mode", false);

		if (updatedPrivacyMode != isPrivacyMode) {
			isPrivacyMode = updatedPrivacyMode;

			if (adapter != null) {
				adapter.setPrivacyMode(isPrivacyMode);
			}
		}

		// Optional: re-apply FLAG_SECURE if needed
		if (isPrivacyMode) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
					WindowManager.LayoutParams.FLAG_SECURE);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
		}

		// Refresh data when returning from AddBill or Settings
		loadBills();
	}
}
