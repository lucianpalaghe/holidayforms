package ro.pss.holidayforms.gui.deprecated;

import com.vaadin.flow.component.html.Div;
import ro.pss.holidayforms.gui.request.HolidayRequestView;

//@Route("/concedii")
//@Route(layout = MainLayout.class)
public class MainView extends Div {
	private final HolidayRequestView holidayRequestView;

	public MainView(HolidayRequestView holidayRequestView) {
		this.holidayRequestView = holidayRequestView;
		this.add(this.holidayRequestView);
	}
}

