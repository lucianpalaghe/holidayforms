package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.clocking.ClockingDay;
import ro.pss.holidayforms.domain.clocking.ClockingRecord;
import ro.pss.holidayforms.domain.repo.ClockingRecordRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClockingService {
	@Autowired
	private ClockingRecordRepository clockingRepo;
	@Autowired
	private UserRepository userRepo;

	public void addClocking(String uid) {
//		ClockingRecord r = new ClockingRecord();
//		r.setEmployee(userRepo.findByClockingUid(uid).get());
//		r.setDateTime(LocalDateTime.now());
//		clockingRepo.save(r);
	}

	public List<ClockingDay> getClockingDays() { // TODO: replace with streams
		Iterable<ClockingRecord> all = clockingRepo.findAll();
		List<ClockingRecord> records = new ArrayList<>();
		all.forEach(records::add);
		ArrayList<ClockingDay> days = new ArrayList<>();
		for (var i = 0; i < records.size(); i++) {
			var clockingRecord = records.get(i);
			var day = new ClockingDay();
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
