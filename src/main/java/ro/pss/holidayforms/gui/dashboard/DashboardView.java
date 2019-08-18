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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.HolidayPlanning;
import ro.pss.holidayforms.domain.HolidayPlanningEntry;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.service.DashboardService;

import javax.annotation.PostConstruct;
import java.time.Month;
import java.util.*;

import static java.util.stream.Collectors.*;

@Route(value = "", layout = HolidayAppLayout.class)
public class DashboardView extends HorizontalLayout implements AfterNavigationObserver {
	@Autowired
	private DashboardService service;
	private final H2 remainingDaysHeader = new H2();
	private ChartJs holidaysChart;
	private LineDataset chartPlannedDays;
	private LineDataset chartHolidays;
	private LineData chartLineData;
	private LineOptions chartLineOptions;

	public DashboardView() {
		VerticalLayout container = new VerticalLayout();
		container.add(remainingDaysHeader);

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

	@PostConstruct
	private void postConstruct() {
		User user = ((CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
		remainingDaysHeader.setText(String.format(MessageRetriever.get("remainingDaysHeader"), service.getRemainingVacationDays(user.getEmail())));

		initializeChart();
		Optional<HolidayPlanning> byEmployeeEmail = planningRepository.findByEmployeeEmail(user.getEmail());
		List<HolidayPlanningEntry> entries = new ArrayList<>();
		byEmployeeEmail.ifPresent(holidayPlanning -> entries.addAll(holidayPlanning.getEntries()));
		holidaysChart = getUpdatedChart(requests, entries);
	}

	private void initializeChart() {
		chartPlannedDays = new LineDataset()
				.setLabel(MessageRetriever.get("chartPlannedDaysLbl"))
				.setBackgroundColor(Color.TRANSPARENT)
				.setBorderColor(Color.LIGHT_BLUE)
				.addPointBackgroundColor(Color.LIGHT_BLUE)
				.setLineTension(0.25f);

		chartHolidays = new LineDataset()
				.setLabel(MessageRetriever.get("chartHolidaysLbl"))
				.setBackgroundColor(Color.TRANSPARENT)
				.setBorderColor(Color.CRIMSON)
				.addPointBackgroundColor(Color.CRIMSON)
				.setLineTension(0.25f);

		chartLineData = new LineData()
				.addLabels(MessageRetriever.get("monthsNamesLong").split(","))
				.addDataset(chartHolidays)
				.addDataset(chartPlannedDays);

		chartLineOptions = new LineOptions()
				.setResponsive(true)
				.setLegend(new Legend().setDisplay(true));
	}

	private ChartJs getUpdatedChart(List<HolidayRequest> requests, List<HolidayPlanningEntry> planningEntries) {
		Map<Month, Integer> holidaysGroupedByMonth = requests.stream()
				.collect(groupingBy(HolidayRequest::getStartingMonthOfHoliday,
						summingInt(HolidayRequest::getNumberOfDays)));

		Map<Month, Integer> emptyMonthsMap = DateUtils.getEmptyMonthsMap();
		emptyMonthsMap.putAll(holidaysGroupedByMonth);

		TreeMap<Month, Integer> monthIntegerTreeMap = new TreeMap<>(emptyMonthsMap);// treemap sorts contents by key

		int[] daysArray = monthIntegerTreeMap.values().stream().mapToInt(i -> i).toArray();
		chartHolidays.setData(daysArray);

		Map<Month, Integer> planningsGroupedByMonth = planningEntries.stream()
				.collect(groupingBy(HolidayPlanningEntry::getStartingMonthOfPlanning,
						summingInt(HolidayPlanningEntry::getNumberOfDays)));

		Map<Month, Integer> emptyMonthsMap2 = DateUtils.getEmptyMonthsMap();
		emptyMonthsMap2.putAll(planningsGroupedByMonth);

		TreeMap<Month, Integer> monthIntegerTreeMap2 = new TreeMap<>(emptyMonthsMap2);// treemap sorts contents by key

		int[] daysArray2 = monthIntegerTreeMap2.values().stream().mapToInt(i -> i).toArray();
		chartHolidays.setData(daysArray);

		chartPlannedDays.setData(daysArray2);

		return new ChartJs(new LineChart(chartLineData, chartLineOptions).toJson());
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		User user = ((CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
		List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail(user.getEmail());
		Optional<HolidayPlanning> byEmployeeEmail = planningRepository.findByEmployeeEmail(user.getEmail());
		List<HolidayPlanningEntry> entries = new ArrayList<>();
		byEmployeeEmail.ifPresent(holidayPlanning -> entries.addAll(holidayPlanning.getEntries()));

		refreshHeader(user.getAvailableVacationDays(), requests);
		holidaysChart = getUpdatedChart(requests, entries);
	}
}
