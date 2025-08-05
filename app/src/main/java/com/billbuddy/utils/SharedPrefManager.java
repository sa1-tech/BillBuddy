package com.billbuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.billbuddy.model.Bill;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefManager {
	private static final String PREF_NAME = "BillBuddyPrefs";
	private static final String KEY_BILLS = "bills";

	private final SharedPreferences sharedPreferences;
	private final SharedPreferences.Editor editor;
	private final Gson gson;

	public SharedPrefManager(Context context) {
		this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		this.editor = sharedPreferences.edit();
		this.gson = new Gson();
	}

	/**
	 * Saves a new bill. If a bill with the same ID already exists, it updates it.
	 */
	public void saveBill(Bill bill) {
		if (bill == null) return;

		List<Bill> billList = getAllBills();
		boolean updated = false;

		for (int i = 0; i < billList.size(); i++) {
			if (billList.get(i).getId() == bill.getId()) {
				billList.set(i, bill);
				updated = true;
				break;
			}
		}

		if (!updated) {
			billList.add(bill);
		}

		saveAllBills(billList);
	}

	/**
	 * Retrieves the entire list of saved bills.
	 */
	public List<Bill> getAllBills() {
		String billsJson = sharedPreferences.getString(KEY_BILLS, "[]");
		Type type = new TypeToken<List<Bill>>() {
		}.getType();
		List<Bill> list = gson.fromJson(billsJson, type);
		return list != null ? list : new ArrayList<>();
	}

	/**
	 * Saves a full bill list (used during backup/restore or resets).
	 */
	public void saveAllBills(List<Bill> billList) {
		if (billList == null) billList = new ArrayList<>();
		String billsJson = gson.toJson(billList);
		editor.putString(KEY_BILLS, billsJson);
		editor.apply(); // Apply is async, good for performance
	}

	/**
	 * Updates an existing bill by matching ID.
	 */
	public void updateBill(Bill updatedBill) {
		if (updatedBill == null) return;

		List<Bill> billList = getAllBills();
		for (int i = 0; i < billList.size(); i++) {
			if (billList.get(i).getId() == updatedBill.getId()) {
				billList.set(i, updatedBill);
				saveAllBills(billList);
				return;
			}
		}
	}

	/**
	 * Returns a bill by its ID.
	 */
	public Bill getBillById(long billId) {
		for (Bill bill : getAllBills()) {
			if (bill.getId() == billId) {
				return bill;
			}
		}
		return null;
	}

	/**
	 * Deletes a bill by its ID.
	 */
	public void deleteBillById(long billId) {
		List<Bill> billList = getAllBills();
		for (int i = 0; i < billList.size(); i++) {
			if (billList.get(i).getId() == billId) {
				billList.remove(i);
				break;
			}
		}
		saveAllBills(billList);
	}

	/**
	 * Checks if there are no bills stored.
	 */
	public boolean isBillListEmpty() {
		return getAllBills().isEmpty();
	}

	/**
	 * Clears all stored bill data.
	 */
	public void clearAllBills() {
		editor.remove(KEY_BILLS);
		editor.apply();
	}

	/**
	 * Checks whether a bill with the given ID exists.
	 */
	public boolean containsBill(long billId) {
		for (Bill bill : getAllBills()) {
			if (bill.getId() == billId) {
				return true;
			}
		}
		return false;
	}
}
