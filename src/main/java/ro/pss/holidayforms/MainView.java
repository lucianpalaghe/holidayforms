package ro.pss.holidayforms;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

//@Route("/concedii")
@Route(layout = MainLayout.class)
public class MainView extends Div {
	private final HolidayRequestView holidayRequestView;

	public MainView(HolidayRequestView holidayRequestView) {
		this.holidayRequestView = holidayRequestView;
		this.add(this.holidayRequestView);
	}
}

