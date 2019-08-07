package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@NoArgsConstructor
public class HolidayPlanning {
	@GeneratedValue
	@Getter
	@Id
	private Long id;

	@Getter
	@Setter
	@OneToOne
	private User employee;

	@Getter
	@Setter
	@OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<HolidayPlanningEntry> entries;

	public HolidayPlanning(User employee, Set<HolidayPlanningEntry> entries) {
		this.employee = employee;
		this.entries = entries;
	}

	public HolidayPlanningEntry.EntryValidityStatus addPlanningEntry(HolidayPlanningEntry planningEntry) {
		HolidayPlanningEntry.EntryValidityStatus validEntry = isPlanningEntryValid(planningEntry);
		if (planningEntry != null) {
			if(validEntry.equals(HolidayPlanningEntry.EntryValidityStatus.VALID)) {
				entries.add(planningEntry);
			}
		}
		return validEntry;
	}

	public void removePlanningEntry(HolidayPlanningEntry planningEntry) {
		if (planningEntry != null) {
//			Optional<HolidayPlanningEntry> existing = entries.stream().filter(p -> p.equals(planningEntry)).findAny();
//			if(existing.isPresent()) {
//				existing.get().setPlanning(null);
//			planningEntry.setPlanning(null);

//			}
			entries.remove(planningEntry);
		}
	}

	private HolidayPlanningEntry.EntryValidityStatus isPlanningEntryValid(HolidayPlanningEntry newEntry) {
		LocalDate dateFrom = newEntry.getDateFrom();
		LocalDate dateTo = newEntry.getDateTo();
		// check that selected days are greater than 0
		if(DateUtils.getWorkingDays(dateFrom, dateTo) == 0) {
			return HolidayPlanningEntry.EntryValidityStatus.NO_WORKING_DAYS;
		}
		// check if remaining holiday days are equal or greater than 0
		int usedDays = entries.stream().mapToInt(HolidayPlanningEntry::getNumberOfDays).sum();
		if(employee.getRegularVacationDays() - newEntry.getNumberOfDays() - usedDays < 0) {
			return HolidayPlanningEntry.EntryValidityStatus.EXCEEDED_DAYS;
		}
		// check that the selected range is not in conflict with previous ranges
		for (HolidayPlanningEntry entry : entries) {
			LocalDate eDateFrom = entry.getDateFrom();
			LocalDate eDateTo = entry.getDateTo();
			if((eDateFrom.isBefore(dateTo)) && (eDateTo.isAfter(dateFrom)) || dateFrom.isEqual(eDateTo) || dateTo.isEqual(eDateFrom)) {
				return HolidayPlanningEntry.EntryValidityStatus.RANGE_CONFLICT;
			}
		}
		return HolidayPlanningEntry.EntryValidityStatus.VALID;
	}

}