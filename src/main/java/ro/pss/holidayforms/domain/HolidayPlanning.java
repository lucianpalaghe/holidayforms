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
	@OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<HolidayPlanningEntry> entries;

	public void addPlanningEntry(HolidayPlanningEntry planningEntry) {
		if (planningEntry != null) {
			entries.add(planningEntry);
			planningEntry.setPlanning(this);
		}
	}
}