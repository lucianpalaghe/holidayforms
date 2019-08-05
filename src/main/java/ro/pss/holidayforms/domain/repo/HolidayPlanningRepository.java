package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.HolidayPlanning;

import java.util.List;

public interface HolidayPlanningRepository extends JpaRepository<HolidayPlanning, Long> {
//	public List<HolidayPlanning> findAllByUserEmail(String email);
}