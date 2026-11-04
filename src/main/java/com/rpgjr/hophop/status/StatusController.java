package com.rpgjr.hophop.status;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
public class StatusController {

	public record StatusDto(Long id, String name, String color) {
		static StatusDto from(Status s) {
			return new StatusDto(s.getId(), s.getName(), s.getColor());
		}
	}

	private final StatusRepository statuses;

	@GetMapping
	public List<StatusDto> list() {
		return statuses.findAllByOrderByNameAsc().stream().map(StatusDto::from).toList();
	}
}
