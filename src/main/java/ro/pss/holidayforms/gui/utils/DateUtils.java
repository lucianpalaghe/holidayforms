package ro.pss.holidayforms.gui.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;

public class DateUtils {
	private static final Set<LocalDate> HOLIDAYS;

	static {
		int currentYear = Year.now().getValue();
		List<LocalDate> dates = Arrays.asList(
				LocalDate.of(currentYear, 1, 1),
				LocalDate.of(currentYear, 1, 2),
				LocalDate.of(currentYear, 1, 24),
				LocalDate.of(currentYear, 4, 26),
				LocalDate.of(currentYear, 4, 28),
				LocalDate.of(currentYear, 4, 29),
				LocalDate.of(currentYear, 4, 4),
				LocalDate.of(currentYear, 5, 1),
				LocalDate.of(currentYear, 6, 1),
				LocalDate.of(currentYear, 6, 16),
				LocalDate.of(currentYear, 8, 15),
				LocalDate.of(currentYear, 11, 30),
				LocalDate.of(currentYear, 12, 1),
				LocalDate.of(currentYear, 12, 25),
				LocalDate.of(currentYear, 12, 26)
		);
		HOLIDAYS = Collections.unmodifiableSet(new HashSet<>(dates));
	}

	public static int getWorkingDays(LocalDate startInclusive, LocalDate endExclusive) {
		if (startInclusive.isAfter(endExclusive)) {
			String msg = "Start date " + startInclusive + " must be earlier than end date " + endExclusive;
			throw new IllegalArgumentException(msg);
		}
		int workingDays = 0;
		LocalDate d = startInclusive;
		while (d.isBefore(endExclusive)) {
			if (isWorkingDay(d)) {
				workingDays++;
			}
			d = d.plusDays(1);
		}

		if (isWorkingDay(d)) { // also check the last day
			workingDays++;
		}

		return workingDays;
	}

	private static boolean isWorkingDay(LocalDate d) {
		DayOfWeek dw = d.getDayOfWeek();
		if (!HOLIDAYS.contains(d)
				&& dw != DayOfWeek.SATURDAY
				&& dw != DayOfWeek.SUNDAY) {
			return true;
		}
		return false;
	}
}
