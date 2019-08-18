package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.pss.holidayforms.domain.HolidayPlanning;

import java.util.Optional;

@Repository
public interface HolidayPlanningRepository extends JpaRepository<HolidayPlanning, Long> {
	Optional<HolidayPlanning> findByEmployeeEmail(String email);
}