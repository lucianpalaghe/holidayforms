package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@NoArgsConstructor
public class HolidayPlanningEntry implements Comparable<HolidayPlanningEntry> {
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

    @Setter
    @Getter
    @ManyToOne
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolidayPlanningEntry that = (HolidayPlanningEntry) o;
        return dateFrom.equals(that.dateFrom) &&
                dateTo.equals(that.dateTo) &&
                planning.getId().equals(that.planning.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateFrom, dateTo, planning.getId());
    }

    @Override
    public int compareTo(HolidayPlanningEntry o) {
        int ret = 0;
        if (dateFrom.isBefore(o.getDateFrom())) {
            ret = -1;
        } else if (dateFrom.isAfter(o.getDateFrom())) {
            ret = 1;
        }
        return ret;
    }
	public enum EntryValidityStatus {
		VALID, NO_WORKING_DAYS, RANGE_CONFLICT, EXCEEDED_DAYS
	}
}