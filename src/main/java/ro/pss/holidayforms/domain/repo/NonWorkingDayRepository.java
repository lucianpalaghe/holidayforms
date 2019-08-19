package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.pss.holidayforms.domain.NonWorkingDay;

@Repository
public interface NonWorkingDayRepository extends JpaRepository<NonWorkingDay, Long> {
}