package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.HolidayRequest;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRequestRepository extends JpaRepository<HolidayRequest, Long> {
	public List<HolidayRequest> findAllByRequesterEmail(String email);
	public List<HolidayRequest> findAllByRequesterEmailAndDateFromBetween(String email, LocalDate from, LocalDate to);
}