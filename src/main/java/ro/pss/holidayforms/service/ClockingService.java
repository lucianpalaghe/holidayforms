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
import java.time.LocalDate;
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
		u.ifPresentOrElse(user -> {
			log.info(String.format("Adding clocking for UID: %s", uid));
			saveClocking(user, LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
		}, () -> log.warn(String.format("User not found for UID: %s", uid)));
	}

	private void saveClocking(User user, LocalDateTime timestamp) {
		ClockingRecord r = new ClockingRecord();
		r.setEmployee(user);
		r.setDateTime(timestamp);
		clockingRepo.save(r);
	}

	public List<EmployeeClockingDay> getClockingDays() {
		Iterable<ClockingRecord> all = clockingRepo.findAll();
		List<ClockingRecord> records = new ArrayList<>();
		all.forEach(records::add);
		Map<User, Map<LocalDate, List<ClockingRecord>>> collect =
				records.stream().collect(Collectors.groupingBy(ClockingRecord::getEmployee,
															   Collectors.groupingBy(clockingRecord -> clockingRecord.getDateTime().toLocalDate())));
		List<EmployeeClockingDay> clockingDays = new ArrayList<>();
		for (User u : collect.keySet()) {
			Map<LocalDate, List<ClockingRecord>> localDateTimeListMap = collect.get(u);
			for (LocalDate dt : localDateTimeListMap.keySet()) {
				EmployeeClockingDay employeeClockingDay = new EmployeeClockingDay(u, dt, localDateTimeListMap.get(dt));
				clockingDays.add(employeeClockingDay);
			}
		}

		return clockingDays;
	}
}
