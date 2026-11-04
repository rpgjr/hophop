package com.rpgjr.hophop.application;

import com.rpgjr.hophop.status.Status;
import com.rpgjr.hophop.status.StatusController.StatusDto;
import com.rpgjr.hophop.status.StatusRepository;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

	public record ApplicationDto(Long id, String title, String company, BigDecimal salaryMin,
			BigDecimal salaryMax, Currency salaryCurrency, EmploymentType employmentType, Setup setup, String remarks, String link,
			LocalDate appliedDate, StatusDto status) {
		static ApplicationDto from(Application a) {
			Status s = a.getStatus();
			return new ApplicationDto(a.getId(), a.getTitle(), a.getCompany(), a.getSalaryMin(),
					a.getSalaryMax(), a.getSalaryCurrency(), a.getEmploymentType(), a.getSetup(), a.getRemarks(), a.getLink(),
					a.getAppliedDate(), new StatusDto(s.getId(), s.getName(), s.getColor()));
		}
	}

	public record StatusUpdate(@NotNull Long statusId) {
	}

	private final ApplicationRepository applications;
	private final StatusRepository statuses;

	/** Applications with applied_date between from and to (inclusive). */
	@GetMapping
	public List<ApplicationDto> list(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return applications.findByAppliedDateBetweenOrderByAppliedDateAscIdAsc(from, to).stream()
				.map(ApplicationDto::from).toList();
	}

	@PatchMapping("/{id}/status")
	@Transactional
	public ApplicationDto updateStatus(@PathVariable Long id, @RequestBody StatusUpdate body) {
		Application app = applications.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
		Status status = statuses.findById(body.statusId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status"));
		app.setStatus(status);
		return ApplicationDto.from(app);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (!applications.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		applications.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
