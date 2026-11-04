package com.rpgjr.hophop.application;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EmploymentType {
	FULL_TIME("Full-time"), PART_TIME("Part-time"), CONTRACTUAL("Contractual");

	private final String label;

	EmploymentType(String label) {
		this.label = label;
	}

	/** The value stored in MySQL and sent to the UI. */
	@JsonValue
	public String label() {
		return label;
	}

	public static EmploymentType fromLabel(String label) {
		for (EmploymentType t : values()) {
			if (t.label.equals(label)) return t;
		}
		throw new IllegalArgumentException("Unknown employment type: " + label);
	}
}
