package com.billbuddy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.billbuddy.R;
import com.billbuddy.activities.BillDetailActivity;
import com.billbuddy.model.Bill;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {
	private Context context;
	private List<Bill> bills;
	private boolean isPrivacyMode;

	// ✅ Modified constructor to include isPrivacyMode flag
	public BillAdapter(Context context, List<Bill> bills, boolean isPrivacyMode) {
		this.context = context;
		this.bills = bills;
		this.isPrivacyMode = isPrivacyMode;
	}

	@Override
	public BillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
		return new BillViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(BillViewHolder holder, int position) {
		Bill bill = bills.get(position);

		// ✅ Apply masking if privacy mode is enabled
		if (isPrivacyMode) {
			holder.title.setText("••••••••");
			holder.amount.setText("₹•••");
			holder.dueDate.setText("•••");
			holder.status.setText("Hidden");
		} else {
			holder.title.setText(bill.getTitle());
			holder.amount.setText("₹" + bill.getAmount());
			holder.dueDate.setText("Due: " + bill.getDueDate());
			holder.status.setText(bill.getStatus());
		}

		holder.itemView.setOnClickListener(v -> {
			Intent intent = new Intent(context, BillDetailActivity.class);
			intent.putExtra("billId", bill.getId());
			context.startActivity(intent);
		});
	}

	@Override
	public int getItemCount() {
		return bills.size();
	}

	public void updateBills(List<Bill> bills) {
		this.bills = bills;
		notifyDataSetChanged();
	}

	// ✅ Optional: allow updating privacy mode dynamically
	public void setPrivacyMode(boolean privacyMode) {
		this.isPrivacyMode = privacyMode;
		notifyDataSetChanged();
	}

	public static class BillViewHolder extends RecyclerView.ViewHolder {
		public TextView title, amount, dueDate, status;

		public BillViewHolder(View view) {
			super(view);
			title = view.findViewById(R.id.billTitle);
			amount = view.findViewById(R.id.billAmount);
			dueDate = view.findViewById(R.id.billDueDate);
			status = view.findViewById(R.id.billStatus);
		}
	}
}
