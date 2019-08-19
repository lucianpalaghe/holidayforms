package ro.pss.holidayforms.gui.dashboard;

import lombok.Data;

@Data
public class DashboardData {
	private int remainingVacationDays;
	private int[] chartVacationDays;
	private int[] chartPlannedDays;
}
