package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.HolidayPlanning;
import ro.pss.holidayforms.domain.HolidayPlanningEntry;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayPlanningRepository;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;
import ro.pss.holidayforms.gui.dashboard.DashboardData;

import java.time.Month;
import java.util.*;

import static java.util.stream.Collectors.*;

@Service
public class DashboardService {
	@Autowired
	private HolidayRequestRepository requestRepository;
	@Autowired
	private HolidayPlanningRepository planningRepository;
	@Autowired
	UserRepository userRepository;

	public DashboardData getDashboardData(String userEmail) {
		DashboardData dashboardData = new DashboardData();

		List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail(userEmail);
		User user = userRepository.findById(userEmail).orElseThrow();

		int sumDaysTaken = requests.stream()
				.filter(HolidayRequest::isCO)
				.mapToInt(r -> DateUtils.getWorkingDays(r.getDateFrom(), r.getDateTo()))
				.sum();

		dashboardData.setRemainingVacationDays(user.getAvailableVacationDays() - sumDaysTaken);

		Map<Month, Integer> holidaysGroupedByMonth = requests.stream()
				.collect(groupingBy(HolidayRequest::getStartingMonthOfHoliday,
						summingInt(HolidayRequest::getNumberOfDays)));

		Map<Month, Integer> emptyMonthsMap = DateUtils.getEmptyMonthsMap();
		emptyMonthsMap.putAll(holidaysGroupedByMonth);

		TreeMap<Month, Integer> monthIntegerTreeMap = new TreeMap<>(emptyMonthsMap);// treemap sorts contents by key

		int[] daysArray = monthIntegerTreeMap.values().stream().mapToInt(i -> i).toArray();
		dashboardData.setChartVacationDays(daysArray);

		Optional<HolidayPlanning> byEmployeeEmail = planningRepository.findByEmployeeEmail(user.getEmail());
		List<HolidayPlanningEntry> entries = new ArrayList<>();
		byEmployeeEmail.ifPresent(holidayPlanning -> entries.addAll(holidayPlanning.getEntries()));

		Map<Month, Integer> planningsGroupedByMonth = entries.stream()
				.collect(groupingBy(HolidayPlanningEntry::getStartingMonthOfPlanning,
						summingInt(HolidayPlanningEntry::getNumberOfDays)));

		Map<Month, Integer> emptyMonthsMap2 = DateUtils.getEmptyMonthsMap();
		emptyMonthsMap2.putAll(planningsGroupedByMonth);

		TreeMap<Month, Integer> monthIntegerTreeMap2 = new TreeMap<>(emptyMonthsMap2);// treemap sorts contents by key

		int[] daysArray2 = monthIntegerTreeMap2.values().stream().mapToInt(i -> i).toArray();
		dashboardData.setChartPlannedDays(daysArray2);

		return dashboardData;
	}
}
