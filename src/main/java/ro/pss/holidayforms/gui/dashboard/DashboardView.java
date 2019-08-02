package ro.pss.holidayforms.gui.dashboard;

import be.ceau.chart.LineChart;
import be.ceau.chart.color.Color;
import be.ceau.chart.data.LineData;
import be.ceau.chart.dataset.LineDataset;
import be.ceau.chart.options.Legend;
import be.ceau.chart.options.LineOptions;
import com.syndybat.chartjs.ChartJs;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import ro.pss.holidayforms.gui.HolidayAppLayout;

@Route(value = "", layout = HolidayAppLayout.class)
public class DashboardView extends VerticalLayout {
	public DashboardView() {
		add(new Label("mai ai x zile de concediu de odihna"));
		ChartJs barChartJs = new ChartJs(getBarChart());
		Div div2 = new Div();
		div2.setHeightFull();
		div2.setWidthFull();
		add(div2);
		div2.add(barChartJs);
	}

	private String getBarChart() {
		LineDataset planned = new LineDataset()
				.setLabel("Planificat")
				.setData(0, 1, 0, 3, 2, 5, 5, 0, 0, 1, 0, 6)
				.setBackgroundColor(Color.TRANSPARENT)
				.setBorderColor(Color.LIGHT_BLUE)
				.addPointBackgroundColor(Color.LIGHT_BLUE)
				.setLineTension(0f);
		LineDataset done = new LineDataset()
				.setLabel("Concediu luat")
				.setData(0, 1, 0, 5, 5, 1, 1, 0, 0, 1, 3, 6)
				.setBackgroundColor(Color.TRANSPARENT)
				.setBorderColor(Color.CRIMSON)
				.addPointBackgroundColor(Color.CRIMSON)
				.setLineTension(0f);

		LineData data = new LineData()
				.addLabels("Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie")
				.addDataset(done)
				.addDataset(planned);

//		JavaScriptFunction label = new JavaScriptFunction("\"function(chart) {console.log('test legend');}\"");

		LineOptions lineOptions = new LineOptions()
				.setResponsive(true)
				.setLegend(new Legend().setDisplay(true));//.setOnClick(label));

		return new LineChart(data, lineOptions).toJson();
	}

}
