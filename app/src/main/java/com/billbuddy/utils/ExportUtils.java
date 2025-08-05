package com.billbuddy.utils;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.billbuddy.model.Bill;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExportUtils {

	public static void exportToCsv(Context context, List<Bill> billList) {
		try {
			String fileName = "BillBuddy_Export_" + System.currentTimeMillis() + ".csv";
			ContentValues values = new ContentValues();
			values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
			values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
			values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BillBuddy");

			OutputStream outputStream = context.getContentResolver().openOutputStream(
					context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values));

			StringBuilder builder = new StringBuilder("ID,Title,Amount,Due Date,Status,Notes\n");

			for (Bill bill : billList) {
				builder.append(bill.getId()).append(",")
						.append(bill.getTitle()).append(",")
						.append(bill.getAmount()).append(",")
						.append(bill.getDueDate()).append(",")
						.append(bill.getStatus()).append(",")
						.append(bill.getNotes().replace(",", " ")).append("\n");
			}

			if (outputStream != null) {
				outputStream.write(builder.toString().getBytes());
				outputStream.close();
				Toast.makeText(context, "CSV Exported to Downloads/BillBuddy", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "CSV Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public static void exportToPdf(Context context, List<Bill> billList) {
		try {
			String fileName = "BillBuddy_Export_" + System.currentTimeMillis() + ".pdf";
			ContentValues values = new ContentValues();
			values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
			values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
			values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BillBuddy");

			OutputStream outputStream = context.getContentResolver().openOutputStream(
					context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values));

			Document document = new Document();
			PdfWriter.getInstance(document, outputStream);
			document.open();
			document.add(new Paragraph("BillBuddy - Exported Bills\n\n"));

			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
			for (Bill bill : billList) {
				String billInfo = String.format(Locale.getDefault(),
						"ID: %d\nTitle: %s\nAmount: %.2f\nDue Date: %s\nStatus: %s\nNotes: %s\n\n",
						bill.getId(), bill.getTitle(), bill.getAmount(), bill.getDueDate(), bill.getStatus(), bill.getNotes());
				document.add(new Paragraph(billInfo));
			}

			document.close();
			Toast.makeText(context, "PDF Exported to Downloads/BillBuddy", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "PDF Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
