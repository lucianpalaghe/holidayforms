package ro.pss.holidayforms.gui.dashboard;

import be.ceau.chart.LineChart;
import be.ceau.chart.color.Color;
import be.ceau.chart.data.LineData;
import be.ceau.chart.dataset.LineDataset;
import be.ceau.chart.options.Legend;
import be.ceau.chart.options.LineOptions;
import com.syndybat.chartjs.ChartJs;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

//import ro.pss.holidayforms.domain.repo.HolidayPlanningRepository;

@Route(value = "", layout = HolidayAppLayout.class)
public class DashboardView extends HorizontalLayout implements AfterNavigationObserver {
	private final HolidayRequestRepository requestRepository;
	private final UserRepository userRepository;
	//	private final HolidayPlanningRepository planningRepository;
	private H2 remainingDaysHeader = new H2();
	private ChartJs holidaysChart;
	private LineDataset chartPlannedDays;
	private LineDataset chartHolidays;
	private LineData chartLineData;
	private LineOptions chartLineOptions;
	private String email = "lucian.palaghe@pss.ro";

	public DashboardView(HolidayRequestRepository requestRepository, UserRepository userRepository) {//}, HolidayPlanningRepository planningRepository) {
		this.requestRepository = requestRepository;
		this.userRepository = userRepository;
//		this.planningRepository = planningRepository;

		VerticalLayout container = new VerticalLayout();
		container.add(remainingDaysHeader);

		User user = userRepository.findById(email).get();
		List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail(email);
		refreshHeader(user, requests);

		initializeChart();

		holidaysChart = getUpdatedChart(requests);
		Div chartContainer = new Div();
		chartContainer.setWidthFull();
		chartContainer.add(holidaysChart);

		container.add(chartContainer);
		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();
	}

	private void refreshHeader(User user, List<HolidayRequest> requests) {
		int sumDaysTaken = requests.stream()
				.filter(HolidayRequest::isCO)
				.mapToInt(r -> DateUtils.getWorkingDays(r.getDateFrom(), r.getDateTo()))
				.sum();

		int days = user.getRegularVacationDays() - sumDaysTaken;//requestRepository.getRemainingDaysByUserEmail("lucian.palaghe@pss.ro");
		remainingDaysHeader.setText(String.format(MessageRetriever.get("remainingDaysHeader"), days));
	}

	private void initializeChart() {
		chartPlannedDays = new LineDataset()
				.setLabel(MessageRetriever.get("chartPlannedDaysLbl"))
				.setBackgroundColor(Color.TRANSPARENT)
				.setBorderColor(Color.LIGHT_BLUE)
				.addPointBackgroundColor(Color.LIGHT_BLUE)
				.setLineTension(0f);

		chartHolidays = new LineDataset()
				.setLabel(MessageRetriever.get("chartHolidaysLbl"))
				.setBackgroundColor(Color.TRANSPARENT)
				.setBorderColor(Color.CRIMSON)
				.addPointBackgroundColor(Color.CRIMSON)
				.setLineTension(0f);

		chartLineData = new LineData()
				.addLabels(MessageRetriever.get("monthsNamesLong").split(","))
				.addDataset(chartHolidays)
				.addDataset(chartPlannedDays);

		chartLineOptions = new LineOptions()
				.setResponsive(true)
				.setLegend(new Legend().setDisplay(true));
	}

	private ChartJs getUpdatedChart(List<HolidayRequest> requests) {
		Map<Month, Integer> holidaysGroupedByMonth = requests.stream()
				.collect(groupingBy(HolidayRequest::getStartingMonthOfHoliday,
						summingInt(HolidayRequest::getNumberOfDays)));

		Map<Month, Integer> emptyMonthsMap = DateUtils.getEmptyMonthsMap();
		emptyMonthsMap.putAll(holidaysGroupedByMonth);

		TreeMap<Month, Integer> monthIntegerTreeMap = new TreeMap<>(emptyMonthsMap);// treemap sorts contents by key

		int[] daysArray = monthIntegerTreeMap.values().stream().mapToInt(i -> i).toArray();
		chartPlannedDays.setData(0, 1, 0, 3, 2, 5, 5, 0, 0, 1, 0, 6);
		chartHolidays.setData(daysArray);

		return new ChartJs(new LineChart(chartLineData, chartLineOptions).toJson());
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		User user = userRepository.findById(email).get();
		List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail(email);

		refreshHeader(user, requests);
		holidaysChart = getUpdatedChart(requests);
	}
}
