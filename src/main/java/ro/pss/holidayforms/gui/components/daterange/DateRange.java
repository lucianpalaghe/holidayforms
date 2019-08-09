package ro.pss.holidayforms.gui.components.daterange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DateRange {
	private LocalDate dateFrom;
	private LocalDate dateTo;

	public boolean isObjectValid() {
		return dateFrom != null && dateTo != null;
	}

	public int getNumberOfDays() {
		return DateUtils.getWorkingDays(dateFrom, dateTo);
	}

	public boolean hasWorkingDays() {
		return getNumberOfDays() > 0;
	}
}
