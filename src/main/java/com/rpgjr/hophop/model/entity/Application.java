package com.rpgjr.hophop.model.entity;

import com.rpgjr.hophop.enums.Currency;
import com.rpgjr.hophop.enums.EmploymentType;
import com.rpgjr.hophop.enums.Setup;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "applications")
public class Application {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "status_id")
	@Setter
	private Status status;

	private String title;

	private String company;

	private BigDecimal salaryMin;

	private BigDecimal salaryMax;

	@Enumerated(EnumType.STRING)
	private Currency salaryCurrency;

	private EmploymentType employmentType;

	@Enumerated(EnumType.STRING)
	private Setup setup;

	private String remarks;

	private String link;

	private LocalDate appliedDate;
}
