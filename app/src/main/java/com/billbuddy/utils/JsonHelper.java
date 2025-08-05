package com.billbuddy.utils;

import com.billbuddy.model.Bill;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonHelper {

	private static final Gson gson = new Gson();

	// ✅ Encode a list of bills to JSON
	public static String encodeBills(List<Bill> bills) {
		return gson.toJson(bills);
	}

	// ✅ Decode JSON to a list of bills (null-safe, compatible)
	public static List<Bill> decodeBills(String json) {
		try {
			Type billListType = new TypeToken<List<Bill>>() {
			}.getType();
			List<Bill> list = gson.fromJson(json, billListType);
			return list != null ? list : new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>(); // fallback
		}
	}

	// ✅ Encode a single bill to JSON
	public static String encodeBill(Bill bill) {
		return gson.toJson(bill);
	}

	// ✅ Decode JSON to a single bill (with null check)
	public static Bill decodeBill(String json) {
		try {
			return gson.fromJson(json, Bill.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null; // fallback
		}
	}
}
