package ro.pss.holidayforms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.clocking.ClockingRecord;
import ro.pss.holidayforms.domain.clocking.EmployeeClockingDay;
import ro.pss.holidayforms.domain.repo.ClockingRecordRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.*;

@Service
@Slf4j
public class ClockingService {
	@Autowired
	private ClockingRecordRepository clockingRepo;
	@Autowired
	private UserRepository userRepo;

	public void addClocking(String uid, Long timestamp) {
		Optional<User> u = userRepo.findByClockingUid(uid);
		Instant instant = Instant.ofEpochSecond(timestamp);
		u.ifPresentOrElse(user -> saveClocking(user, LocalDateTime.ofInstant(instant, ZoneId.systemDefault())),
				() -> log.warn(String.format("User not found for UID: %s", uid)));
	}

	private void saveClocking(User user, LocalDateTime timestamp) {
		ClockingRecord r = new ClockingRecord();
		r.setEmployee(user);
		r.setDateTime(timestamp);
		clockingRepo.save(r);
	}

	public List<EmployeeClockingDay> getClockingDays() { // TODO: replace with streams
		Iterable<ClockingRecord> all = clockingRepo.findAll();
		List<ClockingRecord> records = new ArrayList<>();
		all.forEach(records::add);
		ArrayList<EmployeeClockingDay> days = new ArrayList<>();
//		records.stream().collect(Collectors.groupingBy(ClockingRecord::getEmployee)).collect(Collectors.groupingBy(ClockingRecord::getDateTime));
		Map<User, Map<LocalDateTime, List<ClockingRecord>>> collect = records.stream().collect(Collectors.groupingBy(ClockingRecord::getEmployee, Collectors.groupingBy(ClockingRecord::getDateTime)));


		for (var i = 0; i < records.size(); i++) {
			var clockingRecord = records.get(i);
			var day = new EmployeeClockingDay();
			days.add(day);
			day.addRecord(clockingRecord);
			for (var j = 0; j < records.size(); j++) {
				var candidate = records.get(i + 1);
				if (clockingRecord.getDateTime().toLocalDate().isEqual(candidate.getDateTime().toLocalDate())) {
					day.addRecord(candidate);
				}
			}
		}
		return days;
	}
}
