package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.repo.HolidayPlanningRepository;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import java.util.List;

public class DashboardService {
	@Autowired
	private HolidayRequestRepository requestRepository;
	@Autowired
	private HolidayPlanningRepository planningRepository;
	@Autowired
	UserRepository userRepository;

	public Integer getRemainingVacationDays(String email) {
		List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail(email);
		int sumDaysTaken = requests.stream()
				.filter(HolidayRequest::isCO)
				.mapToInt(r -> DateUtils.getWorkingDays(r.getDateFrom(), r.getDateTo()))
				.sum();

		return userRepository.findById(email).orElseThrow().getAvailableVacationDays() - sumDaysTaken;
	}
}
