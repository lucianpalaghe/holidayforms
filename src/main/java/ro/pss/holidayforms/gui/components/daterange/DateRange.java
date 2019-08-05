package ro.pss.holidayforms.gui.components.daterange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DateRange {
	private LocalDate dateFrom;
	private LocalDate dateTo;

	public boolean isRangeValid() {
		return dateFrom != null && dateTo != null;
	}

}
