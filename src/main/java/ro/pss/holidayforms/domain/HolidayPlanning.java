package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
public class HolidayPlanning {
	@Id
	@GeneratedValue
	@Getter
	private Long id;

	@Getter
	@Setter
	@ManyToOne
	private User employee;

	@Getter
	@Setter
	@OneToMany(mappedBy = "planning", cascade = CascadeType.MERGE, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<HolidayPlanningEntry> entries;

	public HolidayPlanning(User employee, List<HolidayPlanningEntry> entries) {
		this.employee = employee;
		this.entries = entries;
	}

	public void addPlanningEntry(HolidayPlanningEntry planningEntry) {
		if (planningEntry != null) {
			entries.add(planningEntry);
			planningEntry.setPlanning(this);
		}
	}

	public void removePlanningEntry(HolidayPlanningEntry planningEntry) {
		if (planningEntry != null) {
			planningEntry.setPlanning(null);
			entries.remove(planningEntry);
		}
	}
}