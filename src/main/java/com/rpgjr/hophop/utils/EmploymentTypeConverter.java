package com.rpgjr.hophop.utils;

import com.rpgjr.hophop.enums.EmploymentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmploymentTypeConverter implements AttributeConverter<EmploymentType, String> {

	@Override
	public String convertToDatabaseColumn(EmploymentType type) {
		return type == null ? null : type.label();
	}

	@Override
	public EmploymentType convertToEntityAttribute(String label) {
		return label == null ? null : EmploymentType.fromLabel(label);
	}
}
