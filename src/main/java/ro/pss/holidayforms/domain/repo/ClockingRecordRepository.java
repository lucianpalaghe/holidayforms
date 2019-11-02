package ro.pss.holidayforms.domain.repo;

import org.springframework.data.repository.CrudRepository;
import ro.pss.holidayforms.domain.ClockingRecord;

public interface ClockingRecordRepository extends CrudRepository<ClockingRecord, Long> {
}
