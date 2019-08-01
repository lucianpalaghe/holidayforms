package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.HolidayRequest;

public interface HolidayRequestRepository extends JpaRepository<HolidayRequest, Long> {
}