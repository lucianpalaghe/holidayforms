package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
public class HolidayPlanningEntry {
	@Id
	@GeneratedValue
	@Getter
	private Long id;

	@Getter
	@Setter
	private LocalDate dateFrom;

	@Getter
	@Setter
	private LocalDate dateTo;

	@ManyToOne
	@Setter
	@Getter
	private HolidayPlanning planning;

	@Transient
	private int numberOfDays;

	public HolidayPlanningEntry(LocalDate dateFrom, LocalDate dateTo) {
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
	}

	public int getNumberOfDays() {
		return DateUtils.getWorkingDays(dateFrom, dateTo);
	}
}
