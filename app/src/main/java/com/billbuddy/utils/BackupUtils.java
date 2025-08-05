package com.billbuddy.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.billbuddy.model.Bill;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BackupUtils {

	private static final Gson gson = new Gson();

	public static boolean exportToJson(Context context, Uri uri, List<Bill> bills) {
		try {
			OutputStream out = context.getContentResolver().openOutputStream(uri);
			if (out != null) {
				String json = gson.toJson(bills);
				out.write(json.getBytes());
				out.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static List<Bill> importFromJson(Context context, Uri uri) {
		try {
			InputStream in = context.getContentResolver().openInputStream(uri);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				jsonBuilder.append(line);
			}
			reader.close();
			Type type = new TypeToken<List<Bill>>() {
			}.getType();
			return gson.fromJson(jsonBuilder.toString(), type);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			return new ArrayList<>();
		}
	}
}
