package com.billbuddy.model;

import java.util.ArrayList;
import java.util.List;

public class Bill {

	private long id;
	private String title;
	private String category;
	private double amount;
	private String dueDate;
	private String notes;
	private List<String> people;
	private List<Boolean> payments;
	private String attachmentPath; // ✅ Image or PDF URI (stored as String)

	// ============================
	// Constructors
	// ============================
	public Bill(long id, String title, double amount, String dueDate, String notes, List<String> people) {
		this(id, title, amount, dueDate, notes, people, null, null);
	}

	public Bill(long id, String title, double amount, String dueDate, String notes, List<String> people, String category) {
		this(id, title, amount, dueDate, notes, people, category, null);
	}

	public Bill(long id, String title, double amount, String dueDate, String notes, List<String> people, String category, String attachmentPath) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.dueDate = dueDate;
		this.notes = notes;
		this.people = people != null ? new ArrayList<>(people) : new ArrayList<>();
		this.category = category;
		this.attachmentPath = attachmentPath;
		this.payments = new ArrayList<>();

		// 🔁 Ensure payments list size matches people list
		for (int i = 0; i < this.people.size(); i++) {
			payments.add(false); // Initialize as unpaid
		}
	}

	// ============================
	// Getter & Setter Methods
	// ============================
	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<String> getPeople() {
		return people;
	}

	public void setPeople(List<String> people) {
		this.people = new ArrayList<>(people);
		this.payments = new ArrayList<>();
		for (int i = 0; i < people.size(); i++) {
			payments.add(false);
		}
	}

	public List<Boolean> getPayments() {
		return payments;
	}

	public void setPayments(List<Boolean> payments) {
		this.payments = payments;
	}

	public String getAttachmentPath() {
		return attachmentPath;
	}

	public void setAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
	}

	// ============================
	// Business Logic
	// ============================
	public String getStatus() {
		if (people == null || people.isEmpty()) return "Unassigned";
		int paidCount = 0;
		for (Boolean paid : payments) {
			if (paid) paidCount++;
		}
		if (paidCount == people.size()) return "Cleared";
		else if (paidCount > 0) return "Partial";
		else return "Pending";
	}

	public void updatePaymentStatus(String person, boolean isPaid) {
		int index = people.indexOf(person);
		if (index != -1 && index < payments.size()) {
			payments.set(index, isPaid);
		}
	}

	public boolean isPersonPaid(String person) {
		int index = people.indexOf(person);
		return index != -1 && index < payments.size() && payments.get(index);
	}
}
