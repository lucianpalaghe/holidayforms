package ro.pss.holidayforms.gui.components.daterange.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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

	public static Map<Month, Integer> getEmptyMonthsMap() {
		return Stream.of(new Object[][]{
				{Month.JANUARY, 0},
				{Month.FEBRUARY, 0},
				{Month.MARCH, 0},
				{Month.APRIL, 0},
				{Month.MAY, 0},
				{Month.JUNE, 0},
				{Month.JULY, 0},
				{Month.AUGUST, 0},
				{Month.SEPTEMBER, 0},
				{Month.OCTOBER, 0},
				{Month.NOVEMBER, 0},
				{Month.DECEMBER, 0},
		}).collect(toMap(data -> (Month) data[0], data -> (Integer) data[1]));
	}

}
