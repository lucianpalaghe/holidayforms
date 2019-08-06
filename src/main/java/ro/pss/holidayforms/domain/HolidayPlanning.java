package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

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
//			Optional<HolidayPlanningEntry> existing = entries.stream().filter(p -> p.equals(planningEntry)).findAny();
//			if(existing.isPresent()) {
//				existing.get().setPlanning(null);
			planningEntry.setPlanning(null);
//			}
			entries.remove(planningEntry);
		}
	}
}