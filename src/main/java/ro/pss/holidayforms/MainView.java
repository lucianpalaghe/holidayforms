package ro.pss.holidayforms;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//@Route("/concedii")
@Route
public class MainView extends VerticalLayout{
	private final HolidayRequestView holidayRequestView;

	public MainView(HolidayRequestView holidayRequestView) {
		this.holidayRequestView = holidayRequestView;
		this.setWidth("75%");
		setHorizontalComponentAlignment(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);
		add(this.holidayRequestView);
	}
}
