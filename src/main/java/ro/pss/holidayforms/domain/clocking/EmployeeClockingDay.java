package ro.pss.holidayforms.domain.clocking;

import lombok.Getter;
import ro.pss.holidayforms.domain.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EmployeeClockingDay {
	@Getter
	private List<ClockingRecord> records;
	private LocalDate clockingDate;
	private User employee;

	public EmployeeClockingDay(User employee, LocalDate clockingDate, List<ClockingRecord> records) {
		this.employee = employee;
		this.clockingDate = clockingDate;
		this.records = records;
	}

	public LocalDate getClockingDate() {
		if (clockingDate == null) {
			clockingDate = records.stream().min(Comparator.comparing(ClockingRecord::getDateTime)).get().getDateTime().toLocalDate();
		}
		return clockingDate;
	}

	public User getEmployee() {
		if (employee == null) {
			employee = records.stream().min(Comparator.comparing(ClockingRecord::getDateTime)).get().getEmployee();
		}
		return employee;
	}

	public Optional<LocalTime> getClockInTime() {
		Optional<ClockingRecord> optional = records.stream().min(Comparator.comparing(ClockingRecord::getDateTime));
		return optional.map(clockingRecord -> clockingRecord.getDateTime().toLocalTime());
	}

	public Optional<LocalTime> getClockOutTime() {
		if (records.size() <= 1) {
			return Optional.empty();
		}
		Optional<ClockingRecord> optional = records.stream().max(Comparator.comparing(ClockingRecord::getDateTime));
		return optional.map(clockingRecord -> clockingRecord.getDateTime().toLocalTime());
	}

	public void addRecord(ClockingRecord r) {
		if (records == null) {
			records = new ArrayList<>();
		}

		records.add(r);
	}

	public Optional<Duration> getDuration() {
		if (getClockInTime().isEmpty() || getClockOutTime().isEmpty()) {
			return Optional.empty();
		}

		Duration between = Duration.between(getClockInTime().get(), getClockOutTime().get());
		return Optional.of(between);
	}
}
