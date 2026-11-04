package com.rpgjr.hophop.status;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {

	List<Status> findAllByOrderByNameAsc();
}
