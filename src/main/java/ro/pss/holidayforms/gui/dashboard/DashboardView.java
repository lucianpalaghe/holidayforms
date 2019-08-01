package ro.pss.holidayforms.gui.dashboard;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import ro.pss.holidayforms.gui.HolidayAppLayout;

@Route(value = "", layout = HolidayAppLayout.class)
public class DashboardView extends VerticalLayout {
	public DashboardView() {
		Chart myChart = new Chart(ChartType.LINE);
		XAxis xaxis = new XAxis();
		xaxis.setCategories("Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie");
		ListSeries ls = new ListSeries();
		ls.setName("Planificat");
		ls.setData(0, 1, 0, 3, 2, 5, 5, 0, 0, 1, 0, 6);
		myChart.getConfiguration().addSeries(ls);

		ListSeries ls2 = new ListSeries();
		ls2.setName("Executat");
		ls2.setData(0, 1, 0, 5, 5, 1, 1, 0, 0, 1, 3, 6);
		myChart.getConfiguration().addSeries(ls2);

		YAxis yaxis = new YAxis();
		yaxis.setTitle("Zile");
		myChart.getConfiguration().addxAxis(xaxis);
		myChart.getConfiguration().addyAxis(yaxis);
		PlotOptionsColumn plotOptions = new PlotOptionsColumn();
		plotOptions.setStacking(Stacking.NORMAL);
		myChart.getConfiguration().setPlotOptions(plotOptions);
		plotOptions.setAllowPointSelect(true);

		add(myChart);
	}
}
