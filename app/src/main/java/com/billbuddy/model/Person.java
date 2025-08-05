package com.billbuddy.model;

public class Person {

	private String name;
	private boolean hasPaid;

	public Person(String name, boolean hasPaid) {
		this.name = name;
		this.hasPaid = hasPaid;
	}

	public String getName() {
		return name;
	}

	public boolean isHasPaid() {
		return hasPaid;
	}

	public void setHasPaid(boolean hasPaid) {
		this.hasPaid = hasPaid;
	}
}
