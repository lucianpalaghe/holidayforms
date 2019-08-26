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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.service.DashboardService;

@Route(value = "", layout = HolidayAppLayout.class)
@SpringComponent
@UIScope
public class DashboardView extends HorizontalLayout implements AfterNavigationObserver, HasDynamicTitle {
	private DashboardService service;
	private final H2 remainingDaysHeader = new H2();
	private ChartJs holidaysChart;
	private LineDataset chartPlannedDays;
	private LineDataset chartHolidays;
	private LineData chartLineData;
	private LineOptions chartLineOptions;

	@Autowired
	public DashboardView(DashboardService service) {
		this.service = service;
		VerticalLayout container = new VerticalLayout();
		container.add(remainingDaysHeader);

		Div chartContainer = new Div();
		chartContainer.setWidthFull();

		initializeChart();
		refreshDashboardData();
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

	private void refreshDashboardData() {
		DashboardData dashboardData = service.getDashboardData(SecurityUtils.getLoggedInUser().getEmail());
		remainingDaysHeader.setText(String.format(MessageRetriever.get("remainingDaysHeader"), dashboardData.getRemainingVacationDays()));

		holidaysChart.updateChart(getUpdatedChartJson(dashboardData.getChartVacationDays(), dashboardData.getChartPlannedDays()));
	}


	private void initializeChart() {
		holidaysChart = new ChartJs("");
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

	private String getUpdatedChartJson(int[] vacationDays, int[] plannedDays) {
		chartHolidays.setData(vacationDays);
		chartPlannedDays.setData(plannedDays);
		return new LineChart(chartLineData, chartLineOptions).toJson();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		refreshDashboardData();
	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titleDashboard");
	}
}
